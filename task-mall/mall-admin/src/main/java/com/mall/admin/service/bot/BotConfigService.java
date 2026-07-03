package com.mall.admin.service.bot;

import com.mall.admin.model.dto.BotAutoReplyDTO;
import com.mall.admin.model.dto.BotAutoReplyQueryDTO;
import com.mall.admin.model.dto.BotConfigDTO;
import com.mall.admin.model.dto.BotConfigQueryDTO;
import com.mall.admin.model.vo.BotAutoReplyVO;
import com.mall.admin.model.vo.BotConfigVO;
import com.mall.common.core.exception.BizException;

import java.util.List;

/** 机器人配置服务。 */
public interface BotConfigService {

    List<BotConfigVO> list(BotConfigQueryDTO query) throws BizException;

    BotConfigVO save(BotConfigDTO dto) throws BizException;

    BotConfigVO update(BotConfigDTO dto) throws BizException;

    void delete(Long id) throws BizException;

    List<BotAutoReplyVO> replyList(BotAutoReplyQueryDTO query) throws BizException;

    BotAutoReplyVO saveReply(BotAutoReplyDTO dto) throws BizException;

    BotAutoReplyVO updateReply(BotAutoReplyDTO dto) throws BizException;

    void deleteReply(Long id) throws BizException;
}