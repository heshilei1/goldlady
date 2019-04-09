package com.gold.service;

import com.gold.pst.StandingPst;
import com.sun.prism.impl.Disposer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("standingService")
@Scope("prototype")
public class StandingService {
    @Autowired
    private StandingPst standingPst;
    @Autowired
    private UserService userService;

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Map<String, Object> getStandingRecord(String phone,String date) throws Exception {
        Map<String, Object> returnData = new HashMap<>();
        String start="";
        String end="";
        if(StringUtils.isNotEmpty(date)){
            if(date.length()>13){
                start = date.split(" ")[0];
                end = date.split(" ")[2];
                date = "";
            }
        }
        List standingList = standingPst.findStandingRecord(phone,date,start,end);
        returnData.put("standingRecord", standingList);
        return returnData;
    }
}
