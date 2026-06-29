package com.mall.common.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.resp.CommonRespCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
public class ApiRestClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiRestClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public <T> T get(String url, Class<T> clazz, Object... uriVars) throws BizException {

        RawResult result = restClient.get()
                .uri(url, uriVars)
                .retrieve()
                .body(RawResult.class);

        check(result);
        return convert(result.data(), clazz);
    }

    public <T> T post(String url, Object body, Class<T> clazz) throws BizException {
        RawResult result = restClient.post()
                .uri(url)
                .body(body)
                .retrieve()
                .body(RawResult.class);

        check(result);
        return convert(result.data(), clazz);
    }

    public <T> T get(String url, TypeReference<T> typeRef, Object... uriVars) throws BizException {
        RawResult result = restClient.get()
                .uri(url, uriVars)
                .retrieve()
                .body(RawResult.class);

        check(result);
        return objectMapper.convertValue(result.data(), typeRef);
    }

    private void check(RawResult result) throws BizException {
        if (result == null) {
            throw new BizException();
        }
        if(result.code() != CommonRespCode.OK.getCode()) {
            log.error("--->> 远程调用结果出错：code:{}, msg:{}", result.code(), result.msg());
            throw new BizException();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(Object data, Class<T> clazz) {
        if (data == null) {
            return null;
        }
        if (clazz.isInstance(data)) {
            return (T) data;
        }
        return objectMapper.convertValue(data, clazz);
    }

    public record RawResult(int code, String msg, Object data ) {
    }
}
