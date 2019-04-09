package com.gold.controller;

import com.gold.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by hsl on 2017/11/25.
 */
@Scope("prototype")
@Controller
@RequestMapping("/gold/permission")
public class PermissionController extends DsbBaseController {

    @Autowired
    PermissionService permissionService;

    /**
     * 获取角色权限
     *
     * @param roleId
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/getbyroleid", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> getByRoleId(@RequestParam String roleId) throws Exception {
        return permissionService.getByRoleId(roleId);
    }
}
