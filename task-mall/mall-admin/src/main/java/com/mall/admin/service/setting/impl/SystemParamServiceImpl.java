package com.mall.admin.service.setting.impl;

import com.mall.admin.enums.AdminRespCodeEnum;
import com.mall.admin.mapper.AdminSystemParamMapper;
import com.mall.admin.model.dto.SystemParamDTO;
import com.mall.admin.model.dto.SystemParamQueryDTO;
import com.mall.admin.model.entity.AdminSystemParam;
import com.mall.admin.model.vo.SystemParamVO;
import com.mall.admin.service.setting.SystemParamService;
import com.mall.common.core.enums.CommonStatusEnum;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.util.Preconditions;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 系统参数服务实现。 */
@Service
public class SystemParamServiceImpl implements SystemParamService {

    @Resource
    private AdminSystemParamMapper systemParamMapper;

    @Override
    public List<SystemParamVO> list(SystemParamQueryDTO query) throws BizException {
        String keyword = trim(query.getKeyword());
        QueryWrapper wrapper = QueryWrapper.create()
                .from(AdminSystemParam.class)
                .like(AdminSystemParam::getParamKey, keyword, StringUtils.hasText(keyword))
                .eq(AdminSystemParam::getStatus, query.getStatus(), query.getStatus() != null)
                .orderBy(AdminSystemParam::getSortOrder, true)
                .orderBy(AdminSystemParam::getId, false);
        return systemParamMapper.selectListByQuery(wrapper).stream().map(this::toVo).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SystemParamVO save(SystemParamDTO dto) throws BizException {
        String paramKey = normalizeKey(dto.getParamKey());
        Preconditions.needTrue(findByKey(paramKey) == null, AdminRespCodeEnum.SYSTEM_PARAM_KEY_REPEAT, paramKey);
        AdminSystemParam item = new AdminSystemParam();
        fill(item, dto, paramKey);
        systemParamMapper.insert(item);
        return toVo(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SystemParamVO update(SystemParamDTO dto) throws BizException {
        AdminSystemParam item = systemParamMapper.selectOneById(dto.getId());
        Preconditions.notNull(item, AdminRespCodeEnum.DATA_NOT_FOUND, dto.getId());
        String paramKey = normalizeKey(dto.getParamKey());
        AdminSystemParam exists = findByKey(paramKey);
        Preconditions.needTrue(exists == null || exists.getId().equals(item.getId()),
                AdminRespCodeEnum.SYSTEM_PARAM_KEY_REPEAT, paramKey);
        fill(item, dto, paramKey);
        systemParamMapper.update(item);
        return toVo(item);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(Long id) throws BizException {
        AdminSystemParam item = systemParamMapper.selectOneById(id);
        Preconditions.notNull(item, AdminRespCodeEnum.DATA_NOT_FOUND, id);
        systemParamMapper.deleteById(id);
    }

    private AdminSystemParam findByKey(String paramKey) {
        return systemParamMapper.selectOneByQuery(QueryWrapper.create()
                .from(AdminSystemParam.class)
                .eq(AdminSystemParam::getParamKey, paramKey)
                .limit(1));
    }

    private void fill(AdminSystemParam item, SystemParamDTO dto, String paramKey) {
        item.setParamKey(paramKey);
        item.setParamValue(trim(dto.getParamValue()));
        item.setDescription(trim(dto.getDescription()));
        item.setSortOrder(dto.getSortOrder() == null ? 0 : dto.getSortOrder());
        item.setStatus(dto.getStatus() == null ? CommonStatusEnum.STATUS_YES.getCode() : dto.getStatus());
    }

    private String normalizeKey(String paramKey) {
        return paramKey.trim();
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private SystemParamVO toVo(AdminSystemParam item) {
        SystemParamVO vo = new SystemParamVO();
        vo.setId(item.getId());
        vo.setParamKey(item.getParamKey());
        vo.setParamValue(item.getParamValue());
        vo.setDescription(item.getDescription());
        vo.setSortOrder(item.getSortOrder());
        vo.setStatus(item.getStatus());
        vo.setCreateTime(item.getCreateTime());
        vo.setUpdateTime(item.getUpdateTime());
        return vo;
    }
}