package com.mall.wallet.chain.model.entity;

import com.mall.common.db.entity.BaseEntity;
import com.mall.common.db.listener.CustomInsertListener;
import com.mall.common.db.listener.CustomUpdateListener;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 用户充值地址。每用户每链一个收款地址，由 trident 即时生成密钥对（Option D）。
 * <p>
 * 安全约定：私钥不以明文存储，仅以 AES-GCM 加密后存入 {@code encPrivKey}（主密钥走配置/环境变量）。
 * 阶段2迁移 KMS 后本字段改存密文引用，DepositAddressService/SecretCipher 边界不变。
 */
@Data
@Table(value = "user_deposit_address", onInsert = CustomInsertListener.class, onUpdate = CustomUpdateListener.class)
public class UserDepositAddress extends BaseEntity<Long> {

    private Long userId;

    private String chainCode;

    private String address;

    /** 加密私钥（AES-GCM, base64）。严禁明文落库 / 打日志。 */
    private String encPrivKey;
}