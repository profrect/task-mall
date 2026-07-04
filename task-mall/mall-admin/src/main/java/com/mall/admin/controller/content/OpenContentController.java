package com.mall.admin.controller.content;

import com.mall.admin.model.dto.BotConfigQueryDTO;
import com.mall.admin.model.dto.ContentItemQueryDTO;
import com.mall.admin.model.dto.SystemParamQueryDTO;
import com.mall.admin.model.vo.BotConfigVO;
import com.mall.admin.model.vo.ContentItemVO;
import com.mall.admin.model.vo.OpenServiceConfigVO;
import com.mall.admin.model.vo.SystemParamVO;
import com.mall.admin.service.bot.BotConfigService;
import com.mall.admin.service.content.ContentItemService;
import com.mall.admin.service.setting.SystemParamService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.util.StringUtils;

import java.util.List;

/** 前台内容只读接口，只暴露已启用内容。 */
@RestController
@RequestMapping("/api/open/content")
public class OpenContentController {

    private static final String DEFAULT_LANGUAGE = "zh-CN";

    @Resource
    private ContentItemService contentItemService;

    @Resource
    private SystemParamService systemParamService;

    @Resource
    private BotConfigService botConfigService;

    @GetMapping("/notices")
    public Result<List<ContentItemVO>> notices(
            @RequestParam(value = "languageCode", required = false) String languageCode) throws BizException {
        return Result.ok(contentList("NOTICE", languageCode));
    }

    @GetMapping("/list")
    public Result<List<ContentItemVO>> list(
            @RequestParam("type") String type,
            @RequestParam(value = "languageCode", required = false) String languageCode) throws BizException {
        return Result.ok(contentList(type, languageCode));
    }

    @GetMapping("/service")
    public Result<OpenServiceConfigVO> serviceConfig() throws BizException {
        OpenServiceConfigVO vo = new OpenServiceConfigVO();
        vo.setTitle("在线客服");
        vo.setMessage(resolveCustomerServiceMessage());
        vo.setBots(enabledBots().stream().map(this::toOpenBot).toList());
        return Result.ok(vo);
    }

    private List<ContentItemVO> contentList(String type, String languageCode) throws BizException {
        List<ContentItemVO> rows = queryContent(type, languageCode);
        if (rows.isEmpty() && StringUtils.hasText(languageCode) && !DEFAULT_LANGUAGE.equals(languageCode)) {
            rows = queryContent(type, DEFAULT_LANGUAGE);
        }
        return rows;
    }

    private List<ContentItemVO> queryContent(String type, String languageCode) throws BizException {
        ContentItemQueryDTO query = new ContentItemQueryDTO();
        query.setType(type);
        query.setLanguageCode(languageCode);
        query.setStatus(1);
        return contentItemService.list(query);
    }

    private String resolveCustomerServiceMessage() throws BizException {
        SystemParamQueryDTO query = new SystemParamQueryDTO();
        query.setKeyword("customer_service");
        query.setStatus(1);
        return systemParamService.list(query).stream()
                .map(SystemParamVO::getParamValue)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("客服配置待开放，请先查看公告或帮助中心。");
    }

    private List<BotConfigVO> enabledBots() throws BizException {
        BotConfigQueryDTO query = new BotConfigQueryDTO();
        query.setStatus(1);
        return botConfigService.list(query);
    }

    private OpenServiceConfigVO.Bot toOpenBot(BotConfigVO item) {
        OpenServiceConfigVO.Bot bot = new OpenServiceConfigVO.Bot();
        bot.setBotName(item.getBotName());
        bot.setDescription(item.getDescription());
        return bot;
    }
}