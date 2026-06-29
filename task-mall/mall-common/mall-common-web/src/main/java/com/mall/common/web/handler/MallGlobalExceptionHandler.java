package com.mall.common.web.handler;

import com.mall.common.core.exception.BizException;
import com.mall.common.core.resp.CommonRespCode;
import com.mall.common.core.resp.RespCode;
import com.mall.common.core.result.Result;
import com.mall.common.i18n.configuration.I18nMessage;
import com.mall.common.i18n.key.CommonMessageKey;
import lombok.extern.slf4j.Slf4j;

import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class MallGlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException ex) {
        log.warn("--->> 业务异常：", ex);
        RespCode respCode = ex.getRespCode();
        if (respCode == null) {
            respCode = CommonRespCode.SYSTEM_ERROR;
        }

        int code = respCode.getCode();
        String msg = I18nMessage.get(respCode.getMsg(), ex.getArgs());
        return Result.fail(code, msg, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("--->> 参数检查异常：", ex);
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        FieldError fieldError = fieldErrors.stream().filter(f -> StringUtils.hasText(f.getDefaultMessage())).findFirst().orElse(fieldErrors.getFirst());
        String defaultMessage = fieldError.getDefaultMessage();

        String msg = I18nMessage.get(CommonRespCode.PARAM_INVALID.getMsg(), defaultMessage);
        return Result.fail(CommonRespCode.PARAM_INVALID.getCode(), msg, null);
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.warn("", ex);
        String msg = I18nMessage.get(CommonRespCode.SYSTEM_ERROR.getMsg());
        return Result.fail(CommonRespCode.SYSTEM_ERROR.getCode(), msg, null);
    }
}
