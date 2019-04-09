package com.gold.controller;

import com.gold.service.StandingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RequestMapping("gold/standing")
@RestController
/**
 * 查看美丽基金台帐
 * @param userid 用户id
 */
public class StandingController extends DsbBaseController {
    @Autowired
    private StandingService standingService;

    @CrossOrigin
    @RequestMapping("get")
    public Map<String,Object> getStandingRecords(@RequestParam(required = false) String phone,@RequestParam(required = false) String date) throws Exception{
        return standingService.getStandingRecord(phone,date);
    }

}
