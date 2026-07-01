package com.mall.admin.service.content.impl;

import com.mall.admin.enums.AdminRespCodeEnum;
import com.mall.admin.enums.ContentItemType;
import com.mall.admin.mapper.AdminContentItemMapper;
import com.mall.admin.model.dto.ContentItemDTO;
import com.mall.admin.model.dto.ContentItemQueryDTO;
import com.mall.admin.model.entity.AdminContentItem;
import com.mall.admin.model.vo.ContentItemVO;
import com.mall.admin.service.content.ContentItemService;
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
 * 内容配置服务：type 是运营内容的领域边界；单页内容按(type, language)覆盖保存，公告按列表维护。 */
@Service
public class ContentItemServiceImpl implements ContentItemService {

    private static final String DEFAULT_LANGUAGE = "zh-CN";

    @Resource
    private AdminContentItemMapper contentItemMapper;

    @Override
    public List<ContentItemVO> list(ContentItemQueryDTO query) throws BizException {
        String type = StringUtils.hasText(query.getType()) ? normalizeType(query.getType()) : null;
        String language = normalizeLanguage(query.getLanguageCode());
        QueryWrapper wrapper = QueryWrapper.create()
                .from(AdminContentItem.class)
                .eq(AdminContentItem::getType, type, StringUtils.hasText(type))
                .eq(AdminContentItem::getLanguageCode, language, StringUtils.hasText(language))
                .eq(AdminContentItem::getStatus, query.getStatus(), query.getStatus() != null)
                .orderBy(AdminContentItem::getSortOrder, true)
                .orderBy(AdminContentItem::getId, false);
        return contentItemMapper.selectListByQuery(wrapper).stream().map(this::toVo).toList();
    }

    @Override
    public ContentItemVO detail(Long id) throws BizException {
        AdminContentItem item = contentItemMapper.selectOneById(id);
        Preconditions.notNull(item, AdminRespCodeEnum.DATA_NOT_FOUND, id);
        return toVo(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ContentItemVO save(ContentItemDTO dto) throws BizException {
        String type = normalizeType(dto.getType());
        String language = normalizeLanguage(dto.getLanguageCode());
        if (ContentItemType.singleton(type)) {
            AdminContentItem item = findSingleton(type, language);
            if (item == null) {
                item = new AdminContentItem();
            }
            fill(item, dto, type, language);
            upsert(item);
            return toVo(item);
        }
        AdminContentItem item = new AdminContentItem();
        fill(item, dto, type, language);
        contentItemMapper.insert(item);
        return toVo(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ContentItemVO update(ContentItemDTO dto) throws BizException {
        AdminContentItem item = contentItemMapper.selectOneById(dto.getId());
        Preconditions.notNull(item, AdminRespCodeEnum.DATA_NOT_FOUND, dto.getId());
        fill(item, dto, normalizeType(dto.getType()), normalizeLanguage(dto.getLanguageCode()));
        contentItemMapper.update(item);
        return toVo(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) throws BizException {
        AdminContentItem item = contentItemMapper.selectOneById(id);
        Preconditions.notNull(item, AdminRespCodeEnum.DATA_NOT_FOUND, id);
        contentItemMapper.deleteById(id);
    }

    private void upsert(AdminContentItem item) {
        if (item.getId() == null) {
            contentItemMapper.insert(item);
        } else {
            contentItemMapper.update(item);
        }
    }

    private AdminContentItem findSingleton(String type, String language) {
        return contentItemMapper.selectOneByQuery(QueryWrapper.create()
                .from(AdminContentItem.class)
                .eq(AdminContentItem::getType, type)
                .eq(AdminContentItem::getLanguageCode, language)
                .limit(1));
    }

    private void fill(AdminContentItem item, ContentItemDTO dto, String type, String language) {
        item.setType(type);
        item.setLanguageCode(language);
        item.setTitle(dto.getTitle().trim());
        item.setSummary(trim(dto.getSummary()));
        item.setContent(dto.getContent().trim());
        item.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        item.setStatus(dto.getStatus() == null ? CommonStatusEnum.STATUS_YES.getCode() : dto.getStatus());
    }

    private String normalizeType(String type) throws BizException {
        String value = type == null ? null : type.trim().toUpperCase();
        Preconditions.needTrue(ContentItemType.valid(value), AdminRespCodeEnum.CONTENT_TYPE_INVALID, type);
        return value;
    }

    private String normalizeLanguage(String languageCode) {
        if (!StringUtils.hasText(languageCode)) {
            return DEFAULT_LANGUAGE;
        }
        return languageCode.trim();
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private ContentItemVO toVo(AdminContentItem item) {
        ContentItemVO vo = new ContentItemVO();
        vo.setId(item.getId());
        vo.setType(item.getType());
        vo.setLanguageCode(item.getLanguageCode());
        vo.setTitle(item.getTitle());
        vo.setSummary(item.getSummary());
        vo.setContent(item.getContent());
        vo.setSortOrder(item.getSortOrder());
        vo.setStatus(item.getStatus());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());
        return vo;
    }
}