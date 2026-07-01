package com.mall.wallet.chain.address;

import com.mall.common.core.exception.BizException;
import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.chain.adapter.ChainAdapterRegistry;
import com.mall.wallet.chain.adapter.KeyMaterial;
import com.mall.wallet.chain.mapper.UserDepositAddressMapper;
import com.mall.wallet.chain.model.entity.UserDepositAddress;
import com.mall.wallet.security.SecretCipher;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 充值地址分配。把"每用户每链唯一收款地址 + 私钥加密落库"收敛于一处。
 * <p>
 * 不变式：
 * - 同一 (userId, chainCode) 永远对应同一地址（uk_user_chain 兜底并发首建）。
 * - 私钥只在分配瞬间于内存出现，立即经 {@link SecretCipher} 加密后落库；明文不留存、不打日志。
 * - 地址产生方式委托给 {@link ChainAdapterRegistry} 的具体适配器，本服务不感知链差异。
 */
@Slf4j
@Service
public class DepositAddressService {

    @Resource
    private UserDepositAddressMapper addressMapper;

    @Resource
    private ChainAdapterRegistry adapterRegistry;

    @Resource
    private SecretCipher secretCipher;

    /**
     * 取得用户在某链的收款地址，没有则即时分配一个。
     */
    public String getOrAssign(long userId, ChainCode chainCode) throws BizException {
        UserDepositAddress existing = findByUserChain(userId, chainCode);
        if (existing != null) {
            return existing.getAddress();
        }

        KeyMaterial material = adapterRegistry.get(chainCode).newDepositAddress();

        UserDepositAddress entity = new UserDepositAddress();
        entity.setUserId(userId);
        entity.setChainCode(chainCode.name());
        entity.setAddress(material.address());
        // 私钥加密后落库；阶段2迁 KMS 时仅替换 SecretCipher 实现
        entity.setEncPrivKey(material.hasPrivateKey() ? secretCipher.encrypt(material.privateKeyHex()) : null);

        try {
            addressMapper.insert(entity);
            return entity.getAddress();
        } catch (DuplicateKeyException e) {
            // 并发首建：另一个请求已抢先为同一 (userId, chain) 分配，回查既有地址
            UserDepositAddress current = findByUserChain(userId, chainCode);
            return current != null ? current.getAddress() : material.address();
        }
    }

    /**
     * 某链下所有充值地址（扫块监听集合）。
     */
    public List<String> listWatchAddresses(ChainCode chainCode) {
        return addressMapper.selectListByQuery(QueryWrapper.create()
                        .from(UserDepositAddress.class)
                        .eq(UserDepositAddress::getChainCode, chainCode.name()))
                .stream()
                .map(UserDepositAddress::getAddress)
                .toList();
    }

    /**
     * 某链下所有充值地址实体（归集扫描用）。
     * <p>
     * 较 {@link #listWatchAddresses} 多带 userId（建归集单需归属用户）；私钥密文不在此处使用，
     * 归集广播前再经 {@link #findByChainAndAddress} 单地址取出，最小化密文在内存的暴露面。
     */
    public List<UserDepositAddress> listByChain(ChainCode chainCode) {
        return addressMapper.selectListByQuery(QueryWrapper.create()
                .from(UserDepositAddress.class)
                .eq(UserDepositAddress::getChainCode, chainCode.name()));
    }

    /**
     * 按 (链, 地址) 反查归属用户（扫块入账时把链上收款地址映射回 userId）。
     */
    public UserDepositAddress findByChainAndAddress(ChainCode chainCode, String address) {
        return addressMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserDepositAddress.class)
                .eq(UserDepositAddress::getChainCode, chainCode.name())
                .eq(UserDepositAddress::getAddress, address));
    }

    private UserDepositAddress findByUserChain(long userId, ChainCode chainCode) {
        return addressMapper.selectOneByQuery(QueryWrapper.create()
                .from(UserDepositAddress.class)
                .eq(UserDepositAddress::getUserId, userId)
                .eq(UserDepositAddress::getChainCode, chainCode.name()));
    }
}