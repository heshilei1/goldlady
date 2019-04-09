package com.gold.pst;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/11/25.
 */
@Repository("permissionPst")
@Scope("prototype")
public class PermissionPst {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getByRoleId(String roleId) throws Exception {
        String sql = "SELECT rolepermission.permissionid,permission.rank,permission.permissionname FROM rolepermission,permission WHERE rolepermission.permissionid=permission.permissionid AND rolepermission.roleid=? ORDER BY permission.rank";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, roleId);
        return list;
    }
}
