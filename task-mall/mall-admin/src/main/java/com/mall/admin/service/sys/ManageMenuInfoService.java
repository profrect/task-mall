package com.mall.admin.service.sys;

import com.mall.admin.model.dto.ManageMenuInfoDTO;
import com.mall.admin.model.entity.ManageMenuInfo;
import com.mall.admin.model.vo.MenuSampleVO;
import com.mall.admin.model.vo.MenuTagVO;
import com.mall.admin.model.vo.MenuTreeVO;
import com.mall.common.core.exception.BizException;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 系统菜单资源信息表(ManageMenuInfo)表服务接口
 *
 * @author gmxu
 */
public interface ManageMenuInfoService extends IService<ManageMenuInfo> {

    void add(ManageMenuInfoDTO dto);

    void update(ManageMenuInfoDTO dto) throws BizException;

    void delete(Long id) throws BizException;

    List<MenuSampleVO> queryList();

    List<MenuTreeVO> queryTreeList(ManageMenuInfoDTO dto);

    MenuTagVO queryAllMenuTree();

    MenuTagVO queryAllMenuIds(String roleCode);
}
