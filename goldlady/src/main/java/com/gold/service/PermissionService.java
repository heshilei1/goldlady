package com.gold.service;

import com.gold.common.BizException;
import com.gold.common.BizReturnCode;
import com.gold.pst.PermissionPst;
import com.gold.pst.UserPst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by hsl on 2017/11/25.
 */
@Service("permissionService")
@Scope("prototype")
public class PermissionService {

    @Autowired
    PermissionPst permissionPst;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getByRoleId(String roleId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> plist = permissionPst.getByRoleId(roleId);
        map.put("list", plist);
        return map;
    }
}
