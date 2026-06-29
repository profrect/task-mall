package com.mall.admin.service.sys;

import com.mall.admin.model.dto.ManageUserInfoDTO;
import com.mall.admin.model.dto.ManageUserPageDTO;
import com.mall.admin.model.dto.ManageUserPswdDTO;
import com.mall.admin.model.entity.ManageUserInfo;
import com.mall.admin.model.vo.ManageUserInfoVO;
import com.mall.common.core.exception.BizException;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 * 用户信息表(ManageUserInfo)表服务接口
 *
 * @author gmxu
 */
public interface ManageUserInfoService extends IService<ManageUserInfo> {

    Page<ManageUserInfoVO> pageList(ManageUserPageDTO dto);

    void add(ManageUserInfoDTO dto) throws BizException;

    void update(ManageUserInfoDTO dto) throws BizException;

    void delete(Long id) throws BizException;

    ManageUserInfoVO userDetail() throws BizException;

    void updatePswd(ManageUserPswdDTO dto) throws BizException;

    void resetPswd(String username) throws BizException;
}
