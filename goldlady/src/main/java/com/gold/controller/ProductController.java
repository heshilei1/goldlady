package com.gold.controller;

import com.gold.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by hsl on 2017/11/26.
 */
@Scope("prototype")
@Controller
@RequestMapping("/gold/product")
public class ProductController extends DsbBaseController {

    @Autowired
    ProductService productService;

    /**
     * 新增商品
     *
     * @param userId
     * @param pluName
     * @param price
     * @param firstRate
     * @param secondRate
     * @param remark
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> add(@RequestParam String userId, @RequestParam String pluName, @RequestParam String price, @RequestParam String firstRate, @RequestParam String secondRate, @RequestParam String remark) throws Exception {
        productService.add(userId, pluName, price, firstRate, secondRate, remark);
        return getNoDataSuccessMap();
    }

    /**
     * 获取所有商品列表
     *
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/getall", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> getAll() throws Exception {
        return productService.getAll();
    }

    /**
     * 修改商品信息
     *
     * @param userId
     * @param pluId
     * @param pluName
     * @param price
     * @param firstRate
     * @param secondRate
     * @param remark
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unckecked")
    @RequestMapping(value = "/change", method = RequestMethod.POST)
    public
    @ResponseBody
    Map<String, Object> change(@RequestParam String userId, @RequestParam String pluId, @RequestParam String pluName, @RequestParam String price, @RequestParam String firstRate, @RequestParam String secondRate, @RequestParam String remark) throws Exception {
        productService.change(userId, pluId, pluName, price, firstRate, secondRate, remark);
        return getNoDataSuccessMap();
    }
}
