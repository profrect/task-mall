package com.mall.admin.service.bot.impl;

import com.mall.admin.enums.AdminRespCodeEnum;
import com.mall.admin.mapper.AdminBotAutoReplyMapper;
import com.mall.admin.mapper.AdminBotConfigMapper;
import com.mall.admin.model.dto.BotAutoReplyDTO;
import com.mall.admin.model.dto.BotAutoReplyQueryDTO;
import com.mall.admin.model.dto.BotConfigDTO;
import com.mall.admin.model.dto.BotConfigQueryDTO;
import com.mall.admin.model.entity.AdminBotAutoReply;
import com.mall.admin.model.entity.AdminBotConfig;
import com.mall.admin.model.vo.BotAutoReplyVO;
import com.mall.admin.model.vo.BotConfigVO;
import com.mall.admin.service.bot.BotConfigService;
import com.mall.common.core.enums.CommonStatusEnum;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 机器人配置服务实现。
 *
 * 不调用 Telegram Bot API，只维护后台配置和自动回复规则。
 */
@Service
public class BotConfigServiceImpl implements BotConfigService {

    @Resource
    private AdminBotConfigMapper botConfigMapper;

    @Resource
    private AdminBotAutoReplyMapper botAutoReplyMapper;

    @Override
    public List<BotConfigVO> list(BotConfigQueryDTO query) throws BizException {
        String keyword = trim(query.getKeyword());
        QueryWrapper wrapper = QueryWrapper.create()
                .from(AdminBotConfig.class)
                .like(AdminBotConfig::getBotName, keyword, StringUtils.hasText(keyword))
                .eq(AdminBotConfig::getStatus, query.getStatus(), query.getStatus() != null)
                .orderBy(AdminBotConfig::getSortOrder, true)
                .orderBy(AdminBotConfig::getId, false);
        return botConfigMapper.selectListByQuery(wrapper).stream().map(this::toConfigVo).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BotConfigVO save(BotConfigDTO dto) throws BizException {
        String botName = trim(dto.getBotName());
        String token = trim(dto.getBotToken());
        Preconditions.needTrue(StringUtils.hasText(token), AdminRespCodeEnum.BOT_TOKEN_REQUIRED);
        Preconditions.needTrue(findByName(botName) == null, AdminRespCodeEnum.BOT_NAME_REPEAT, botName);

        AdminBotConfig item = new AdminBotConfig();
        fillConfig(item, dto, botName);
        item.setBotToken(token);
        botConfigMapper.insert(item);
        return toConfigVo(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BotConfigVO update(BotConfigDTO dto) throws BizException {
        AdminBotConfig item = botConfigMapper.selectOneById(dto.getId());
        Preconditions.notNull(item, AdminRespCodeEnum.BOT_CONFIG_NOT_FOUND, dto.getId());

        String botName = trim(dto.getBotName());
        AdminBotConfig exists = findByName(botName);
        Preconditions.needTrue(exists == null || exists.getId().equals(item.getId()),
                AdminRespCodeEnum.BOT_NAME_REPEAT, botName);

        fillConfig(item, dto, botName);
        String token = trim(dto.getBotToken());
        if (StringUtils.hasText(token)) {
            item.setBotToken(token);
        }
        botConfigMapper.update(item);
        return toConfigVo(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) throws BizException {
        AdminBotConfig item = botConfigMapper.selectOneById(id);
        Preconditions.notNull(item, AdminRespCodeEnum.BOT_CONFIG_NOT_FOUND, id);
        botAutoReplyMapper.deleteByQuery(QueryWrapper.create()
                .from(AdminBotAutoReply.class)
                .eq(AdminBotAutoReply::getBotId, id));
        botConfigMapper.deleteById(id);
    }

    @Override
    public List<BotAutoReplyVO> replyList(BotAutoReplyQueryDTO query) throws BizException {
        String keyword = trim(query.getKeyword());
        QueryWrapper wrapper = QueryWrapper.create()
                .from(AdminBotAutoReply.class)
                .eq(AdminBotAutoReply::getBotId, query.getBotId(), query.getBotId() != null)
                .like(AdminBotAutoReply::getKeyword, keyword, StringUtils.hasText(keyword))
                .eq(AdminBotAutoReply::getStatus, query.getStatus(), query.getStatus() != null)
                .orderBy(AdminBotAutoReply::getSortOrder, true)
                .orderBy(AdminBotAutoReply::getId, false);
        return botAutoReplyMapper.selectListByQuery(wrapper).stream().map(this::toReplyVo).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BotAutoReplyVO saveReply(BotAutoReplyDTO dto) throws BizException {
        assertBotExists(dto.getBotId());
        AdminBotAutoReply item = new AdminBotAutoReply();
        fillReply(item, dto);
        botAutoReplyMapper.insert(item);
        return toReplyVo(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BotAutoReplyVO updateReply(BotAutoReplyDTO dto) throws BizException {
        AdminBotAutoReply item = botAutoReplyMapper.selectOneById(dto.getId());
        Preconditions.notNull(item, AdminRespCodeEnum.DATA_NOT_FOUND, dto.getId());
        assertBotExists(dto.getBotId());
        fillReply(item, dto);
        botAutoReplyMapper.update(item);
        return toReplyVo(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteReply(Long id) throws BizException {
        AdminBotAutoReply item = botAutoReplyMapper.selectOneById(id);
        Preconditions.notNull(item, AdminRespCodeEnum.DATA_NOT_FOUND, id);
        botAutoReplyMapper.deleteById(id);
    }

    private void fillConfig(AdminBotConfig item, BotConfigDTO dto, String botName) {
        item.setBotName(botName);
        item.setWebhookUrl(trim(dto.getWebhookUrl()));
        item.setDescription(trim(dto.getDescription()));
        item.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        item.setStatus(dto.getStatus() == null ? CommonStatusEnum.STATUS_YES.getCode() : dto.getStatus());
    }

    private void fillReply(AdminBotAutoReply item, BotAutoReplyDTO dto) {
        item.setBotId(dto.getBotId());
        item.setKeyword(trim(dto.getKeyword()));
        item.setReplyContent(trim(dto.getReplyContent()));
        item.setMatchType(dto.getMatchType() == null ? 1 : dto.getMatchType());
        item.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        item.setStatus(dto.getStatus() == null ? CommonStatusEnum.STATUS_YES.getCode() : dto.getStatus());
    }

    private AdminBotConfig findByName(String botName) {
        return botConfigMapper.selectOneByQuery(QueryWrapper.create()
                .from(AdminBotConfig.class)
                .eq(AdminBotConfig::getBotName, botName)
                .limit(1));
    }

    private void assertBotExists(Long botId) throws BizException {
        Preconditions.notNull(botId, AdminRespCodeEnum.BOT_CONFIG_NOT_FOUND, botId);
        Preconditions.notNull(botConfigMapper.selectOneById(botId), AdminRespCodeEnum.BOT_CONFIG_NOT_FOUND, botId);
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String maskToken(String token) {
        if (!StringUtils.hasText(token)) {
            return "";
        }
        String value = token.trim();
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }

    private BotConfigVO toConfigVo(AdminBotConfig item) {
        BotConfigVO vo = new BotConfigVO();
        vo.setId(item.getId());
        vo.setBotName(item.getBotName());
        vo.setMaskedToken(maskToken(item.getBotToken()));
        vo.setWebhookUrl(item.getWebhookUrl());
        vo.setDescription(item.getDescription());
        vo.setSortOrder(item.getSortOrder());
        vo.setStatus(item.getStatus());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());
        return vo;
    }

    private BotAutoReplyVO toReplyVo(AdminBotAutoReply item) {
        BotAutoReplyVO vo = new BotAutoReplyVO();
        vo.setId(item.getId());
        vo.setBotId(item.getBotId());
        vo.setKeyword(item.getKeyword());
        vo.setReplyContent(item.getReplyContent());
        vo.setMatchType(item.getMatchType());
        vo.setSortOrder(item.getSortOrder());
        vo.setStatus(item.getStatus());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());
        return vo;
    }
}