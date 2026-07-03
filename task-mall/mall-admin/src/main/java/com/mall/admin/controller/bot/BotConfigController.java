package com.mall.admin.controller.bot;

import com.mall.admin.model.dto.BotAutoReplyDTO;
import com.mall.admin.model.dto.BotAutoReplyQueryDTO;
import com.mall.admin.model.dto.BotConfigDTO;
import com.mall.admin.model.dto.BotConfigQueryDTO;
import com.mall.admin.model.vo.BotAutoReplyVO;
import com.mall.admin.model.vo.BotConfigVO;
import com.mall.admin.service.bot.BotConfigService;
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

/** 机器人配置接口。 */
@RestController
@RequestMapping("/api/admin/bot")
public class BotConfigController {

    @Resource
    private BotConfigService botConfigService;

    @GetMapping("/config/list")
    public Result<List<BotConfigVO>> list(BotConfigQueryDTO query) throws BizException {
        return Result.ok(botConfigService.list(query));
    }

    @PostMapping("/config")
    public Result<BotConfigVO> save(
            @RequestBody @Validated({ValidGroups.Insert.class}) BotConfigDTO dto) throws BizException {
        return Result.ok(botConfigService.save(dto));
    }

    @PutMapping("/config")
    public Result<BotConfigVO> update(
            @RequestBody @Validated({ValidGroups.Update.class}) BotConfigDTO dto) throws BizException {
        return Result.ok(botConfigService.update(dto));
    }

    @DeleteMapping("/config/{id}")
    public Result<Void> delete(@PathVariable("id") @NotNull Long id) throws BizException {
        botConfigService.delete(id);
        return Result.ok();
    }

    @GetMapping("/reply/list")
    public Result<List<BotAutoReplyVO>> replyList(BotAutoReplyQueryDTO query) throws BizException {
        return Result.ok(botConfigService.replyList(query));
    }

    @PostMapping("/reply")
    public Result<BotAutoReplyVO> saveReply(
            @RequestBody @Validated({ValidGroups.Insert.class}) BotAutoReplyDTO dto) throws BizException {
        return Result.ok(botConfigService.saveReply(dto));
    }

    @PutMapping("/reply")
    public Result<BotAutoReplyVO> updateReply(
            @RequestBody @Validated({ValidGroups.Update.class}) BotAutoReplyDTO dto) throws BizException {
        return Result.ok(botConfigService.updateReply(dto));
    }

    @DeleteMapping("/reply/{id}")
    public Result<Void> deleteReply(@PathVariable("id") @NotNull Long id) throws BizException {
        botConfigService.deleteReply(id);
        return Result.ok();
    }
}