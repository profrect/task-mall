//package com.mall.admin.service.sys.impl;
//
//import com.mall.admin.mapper.ManageApiInfoMapper;
//import com.mall.admin.model.dto.ManageApiInfoDTO;
//import com.mall.admin.model.entity.ManageApiInfo;
//import com.mall.admin.model.vo.ManageApiInfoVO;
//import com.mall.admin.model.vo.ServerApiListVO;
//import com.mall.admin.service.sys.ManageApiInfoService;
//import com.mybatisflex.core.paginate.Page;
//import com.mybatisflex.spring.service.impl.ServiceImpl;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.Collections;
//import java.util.List;
//
///**
// * 系统接口资源信息表(ManageApiInfo)表服务接口实现类
// *
// * @author gmxu
// */
//@Service
//public class ManageApiInfoServiceImpl extends ServiceImpl<ManageApiInfoMapper, ManageApiInfo> implements ManageApiInfoService {
//
//    @Override
//    public Page<ManageApiInfoVO> pageList(ManageApiInfoDTO dto) {
//        // TODO
//        return new Page<>();
//    }
//
//    @Override
//    public void add(ManageApiInfoDTO dto) {
//        // TODO
//    }
//
//    @Override
//    public void update(ManageApiInfoDTO dto) {
//        // TODO
//    }
//
//    @Override
//    public void delete(Long id) {
//        // TODO
//    }
//
//    @Override
//    public List<ServerApiListVO> getAllTypeApis() {
//        return Collections.emptyList();
//    }
//
//    @Override
//    public void importServerApi(MultipartFile file) {
//
//    }
//
//    @Override
//    public void exportServerApi(HttpServletResponse response) {
//
//    }
//}
