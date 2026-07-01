package com.mall.wallet.chain.adapter;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.enums.WalletRespCodeEnum;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 链适配器注册表：把 Spring 容器内所有 {@link ChainAdapter} 按 {@link ChainCode} 装配为查找表。
 * <p>
 * 编排层只依赖本注册表 + ChainCode，新增一条链 = 新增一个适配器 Bean，无需改动编排代码。
 */
@Component
public class ChainAdapterRegistry {

    private final Map<ChainCode, ChainAdapter> registry;

    public ChainAdapterRegistry(List<ChainAdapter> adapters) {
        this.registry = adapters.stream()
                .collect(Collectors.toUnmodifiableMap(ChainAdapter::chainCode, Function.identity()));
    }

    public ChainAdapter get(ChainCode chainCode) throws BizException {
        ChainAdapter adapter = registry.get(chainCode);
        Preconditions.notNull(adapter, WalletRespCodeEnum.CHAIN_NOT_SUPPORTED, chainCode);
        return adapter;
    }

    public boolean supports(ChainCode chainCode) {
        return registry.containsKey(chainCode);
    }

    public Set<ChainCode> supportedChains() {
        return registry.keySet();
    }
}