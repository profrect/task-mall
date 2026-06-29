//package com.mall.admin.service.sys;
//
//import com.mall.admin.model.dto.ManageApiInfoDTO;
//import com.mall.admin.model.entity.ManageApiInfo;
//import com.mall.admin.model.vo.ManageApiInfoVO;
//import com.mall.admin.model.vo.ServerApiListVO;
//import com.mybatisflex.core.paginate.Page;
//import com.mybatisflex.core.service.IService;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
///**
// * 系统接口资源信息表(ManageApiInfo)表服务接口
// *
// * @author gmxu
// */
//public interface ManageApiInfoService extends IService<ManageApiInfo> {
//
//    Page<ManageApiInfoVO> pageList(ManageApiInfoDTO dto);
//
//    void add(ManageApiInfoDTO dto);
//
//    void update(ManageApiInfoDTO dto);
//
//    void delete(Long id);
//
//    List<ServerApiListVO> getAllTypeApis();
//
//    void importServerApi(MultipartFile file);
//
//    void exportServerApi(HttpServletResponse response);
//}
