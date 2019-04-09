package com.gold;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.text.ParseException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoldladyApplicationTests {



    @Test
    public void contextLoads() throws ParseException {
        String eventkey = "qrscene_1_1561052615166666";
        String[] strs = eventkey.split("_");
        String superiorOpenId = strs[2];
        String code = strs[1];
        System.out.println(superiorOpenId);
        System.out.println(code);
    }
}
