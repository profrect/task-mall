package com.mall.wallet.config;

import com.mall.wallet.chain.ChainCode;
import com.mall.wallet.chain.evm.EvmAdapter;
import com.mall.wallet.chain.evm.EvmProperties;
import com.mall.wallet.chain.tron.TronProperties;
import com.mall.wallet.collect.CollectProperties;
import com.mall.wallet.payout.GasStationProperties;
import com.mall.wallet.payout.HotWalletProperties;
import com.mall.wallet.security.WalletSecurityProperties;
import com.mall.wallet.withdraw.WithdrawProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 钱包链上 / 安全相关配置装配。集中开启外部化配置绑定，
 * 把"密钥、TronGrid 端点、热钱包、提现规则"等敏感/环境相关参数收敛到配置项，代码零硬编码。
 * <p>
 * EVM 适配器装配：ETH / BSC / Polygon 共用 {@link EvmAdapter} 一份逻辑，此处按链注册为三个 bean，
 * 各自携带 {@link ChainCode} 与共享的 {@link EvmProperties}（按链名取配置）。
 * {@code ChainAdapterRegistry} 自动把它们与 TronAdapter 一起装配为 {@code ChainCode -> ChainAdapter} 查找表，
 * 编排层无需感知具体链。
 */
@Configuration
@EnableConfigurationProperties({
        WalletSecurityProperties.class,
        TronProperties.class,
        HotWalletProperties.class,
        WithdrawProperties.class,
        EvmProperties.class,
        GasStationProperties.class,
        CollectProperties.class
})
public class WalletChainConfiguration {

    @Bean
    public EvmAdapter ethAdapter(EvmProperties evmProperties) {
        return new EvmAdapter(ChainCode.ETH, evmProperties);
    }

    @Bean
    public EvmAdapter bscAdapter(EvmProperties evmProperties) {
        return new EvmAdapter(ChainCode.BSC, evmProperties);
    }

    @Bean
    public EvmAdapter polygonAdapter(EvmProperties evmProperties) {
        return new EvmAdapter(ChainCode.POLYGON, evmProperties);
    }
}