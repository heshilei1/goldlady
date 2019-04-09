package com.gold.service;

import com.gold.common.BizException;
import com.gold.common.BizReturnCode;
import com.gold.common.Utility;
import com.gold.pst.ProductPst;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hsl on 2017/11/26.
 */
@Service("productService")
@Scope("prototype")
public class ProductService {

    @Autowired
    UserService userService;
    @Autowired
    ProductPst productPst;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void add(String userId, String pluName, String price, String firstRate, String secondRate, String remark) throws Exception {
        //操作用户权限
        if (!userService.checkIsAdmin(userId)) {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }
        //新增商品
        String pluId = Utility.generateId();
        productPst.add(pluId, pluName, new BigDecimal(price), firstRate, secondRate, remark);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getAll() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("list", productPst.getAll());
        return map;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void change(String userId, String pluId, String pluName, String price, String firstRate, String secondRate, String remark) throws Exception {
        //操作用户权限
        if (!userService.checkIsAdmin(userId)) {
            throw new BizException(BizReturnCode.UserLevelLow, "用户权限不足");
        }
        //修改商品
        productPst.change(pluId, pluName, new BigDecimal(price), firstRate, secondRate, remark);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> findById(String pluid) {
        return productPst.findById(pluid);
    }
}
