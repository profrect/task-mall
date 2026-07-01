package com.mall.wallet.chain.tron;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TronGrid 只读访问客户端：把 TronGrid 的 HTTP/JSON 细节收敛在此处，对上只暴露领域语义方法。
 * <p>
 * 用独立 {@link RestClient}（自带 baseUrl + 可选 TRON-PRO-API-KEY 头 + 超时），
 * 与 mall-common-web 那个带 X-Inner-Token 的内部调用 RestClient 互不干扰。
 * 失败统一抛 {@link TronGridException}（运行时），由 TronAdapter 收敛为 BizException。
 */
@Slf4j
@Component
public class TronGridClient {

    private final TronProperties properties;
    private final RestClient rest;

    public TronGridClient(TronProperties properties) {
        this.properties = properties;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getTimeoutMillis());
        factory.setReadTimeout(properties.getTimeoutMillis());

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getEndpoint())
                .requestFactory(factory);
        if (StringUtils.hasText(properties.getApiKey())) {
            builder.defaultHeader("TRON-PRO-API-KEY", properties.getApiKey().trim());
        }
        this.rest = builder.build();
    }

    /** 当前最新区块高度（用于计算确认数）。 */
    public long getNowBlockNumber() {
        JsonNode node = post("/wallet/getnowblock", Map.of());
        long number = node.path("block_header").path("raw_data").path("number").asLong(0);
        if (number <= 0) {
            throw new TronGridException("getnowblock 返回无效区块高度: " + node);
        }
        return number;
    }

    /** 交易所在区块高度；交易尚未被打包/不存在时返回 0。 */
    public long getTxBlockNumber(String txId) {
        JsonNode node = post("/wallet/gettransactioninfobyid", Map.of("value", txId));
        return node.path("blockNumber").asLong(0);
    }

    /**
     * 拉取某地址作为收款方(only_to)、指定 USDT 合约、已固化(only_confirmed)的最近 TRC20 转入。
     * <p>
     * 只取已固化交易：TRON 固化即不可逆，进入账务管线的都是near-final 事实，规避重组导致的"假入账"。
     */
    public List<Trc20Transfer> getInboundUsdtTransfers(String address) {
        JsonNode root = rest.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/accounts/{addr}/transactions/trc20")
                        .queryParam("only_to", "true")
                        .queryParam("only_confirmed", "true")
                        .queryParam("limit", properties.getScanLimit())
                        .queryParam("contract_address", properties.getUsdtContract())
                        .build(address))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JsonNode.class);

        if (root == null || !root.path("success").asBoolean(false)) {
            throw new TronGridException("trc20 转账查询失败: " + root);
        }

        List<Trc20Transfer> transfers = new ArrayList<>();
        for (JsonNode item : root.path("data")) {
            if (!"Transfer".equals(item.path("type").asText())) {
                continue;
            }
            transfers.add(new Trc20Transfer(
                    item.path("transaction_id").asText(),
                    item.path("from").asText(),
                    item.path("to").asText(),
                    item.path("value").asText("0"),
                    item.path("token_info").path("address").asText()
            ));
        }
        return transfers;
    }

    private JsonNode post(String path, Object body) {
        JsonNode node = rest.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
        if (node == null) {
            throw new TronGridException("TronGrid 空响应: " + path);
        }
        return node;
    }

    /** TRC20 转账观测条目（TronGrid 原样字段，金额为链上原始整数字符串）。 */
    public record Trc20Transfer(String txId, String from, String to, String valueRaw, String contractAddress) {
    }

    public static class TronGridException extends RuntimeException {
        public TronGridException(String message) {
            super(message);
        }
    }
}