package com.kwp.chat.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kwp.chat.model.auth.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门Mapper
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 根据部门编码查询
     */
    @Select("SELECT * FROM sys_department WHERE dept_code = #{deptCode} AND deleted = 0")
    Department selectByDeptCode(@Param("deptCode") String deptCode);

    /**
     * 查询子部门列表
     */
    @Select("SELECT * FROM sys_department WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order")
    List<Department> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询部门树
     */
    @Select("SELECT * FROM sys_department WHERE deleted = 0 ORDER BY sort_order")
    List<Department> selectAll();
}