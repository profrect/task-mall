package com.mall.admin.controller.content;

import com.mall.admin.model.dto.ContentItemDTO;
import com.mall.admin.model.dto.ContentItemQueryDTO;
import com.mall.admin.model.vo.ContentItemVO;
import com.mall.admin.service.content.ContentItemService;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.result.Result;
import com.mall.common.core.valid.ValidGroups;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 内容配置接口。 */
@RestController
@RequestMapping("/api/admin/content")
public class ContentItemController {

    @Resource
    private ContentItemService contentItemService;

    @GetMapping("/list")
    public Result<List<ContentItemVO>> list(ContentItemQueryDTO query) throws BizException {
        return Result.ok(contentItemService.list(query));
    }

    @GetMapping("/{id}")
    public Result<ContentItemVO> detail(@PathVariable("id") @NotNull Long id) throws BizException {
        return Result.ok(contentItemService.detail(id));
    }

    @PostMapping
    public Result<ContentItemVO> save(
            @RequestBody @Validated({ValidGroups.Insert.class}) ContentItemDTO dto) throws BizException {
        return Result.ok(contentItemService.save(dto));
    }

    @PutMapping
    public Result<ContentItemVO> update(
            @RequestBody @Validated({ValidGroups.Update.class}) ContentItemDTO dto) throws BizException {
        return Result.ok(contentItemService.update(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") @NotNull Long id) throws BizException {
        contentItemService.delete(id);
        return Result.ok();
    }
}