package com.mall.admin.service.sys;

import com.mall.admin.model.dto.ManageRoleInfoDTO;
import com.mall.admin.model.entity.ManageRoleInfo;
import com.mall.admin.model.vo.ManageRoleInfoVO;
import com.mall.common.core.exception.BizException;
import com.mall.common.core.model.Option;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 角色信息表(ManageRoleInfo)表服务接口
 *
 * @author gmxu
 */
public interface ManageRoleInfoService extends IService<ManageRoleInfo> {

    Page<ManageRoleInfoVO> pageList(ManageRoleInfoDTO dto);

    void add(ManageRoleInfoDTO dto) throws BizException;

    void update(ManageRoleInfoDTO dto) throws BizException;

    void delete(Long id) throws BizException;

    List<Option<String, String>> optionList();

    void assignRoleMenu(String roleCode, List<Long> idList) throws BizException;

    void assignRoleApi(String roleCode, List<Long> idList) throws BizException;
}
