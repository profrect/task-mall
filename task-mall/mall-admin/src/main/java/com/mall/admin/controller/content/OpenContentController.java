package com.mall.admin.controller.content;

import com.mall.admin.model.dto.ContentItemQueryDTO;
import com.mall.admin.model.vo.ContentItemVO;
import com.mall.admin.service.content.ContentItemService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 前台内容只读接口，只暴露已启用内容。 */
@RestController
@RequestMapping("/api/open/content")
public class OpenContentController {

    @Resource
    private ContentItemService contentItemService;

    @GetMapping("/notices")
    public Result<List<ContentItemVO>> notices(
            @RequestParam(value = "languageCode", required = false) String languageCode) throws BizException {
        List<ContentItemVO> notices = noticeList(languageCode);
        if (notices.isEmpty() && languageCode != null && !"zh-CN".equals(languageCode)) {
            notices = noticeList("zh-CN");
        }
        return Result.ok(notices);
    }

    private List<ContentItemVO> noticeList(String languageCode) throws BizException {
        ContentItemQueryDTO query = new ContentItemQueryDTO();
        query.setType("NOTICE");
        query.setLanguageCode(languageCode);
        query.setStatus(1);
        return contentItemService.list(query);
    }
}