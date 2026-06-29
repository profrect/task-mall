package com.mall.admin.mapper;

import com.mall.admin.model.entity.ManageMenuInfo;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统菜单资源信息表(ManageMenuInfo)表数据库访问层
 *
 * @author gmxu
 */
@Mapper
public interface ManageMenuInfoMapper extends BaseMapper<ManageMenuInfo> {

    @Delete("delete from manage_menu_info")
    int deleteAll();

    @Insert("<script>" +
            "insert into manage_menu_info (" +
            "id, menu_key, `name`, `path`, `type`, component, hide_in_menu, icon, pno, menu_level, `order`, tag, status" +
            ") values " +
            "<foreach collection='poList' item='item' separator=','>" +
            "(#{item.id}, #{item.menuKey}, #{item.name}, #{item.path}, #{item.type}, #{item.component}, #{item.hideInMenu}, #{item.icon}, #{item.pno}, #{item.menuLevel}, #{item.order}, #{item.tag}, #{item.status})" +
            "</foreach>" +
            "</script>")
    int insertAll(@Param("poList") List<ManageMenuInfo> poList);
}
