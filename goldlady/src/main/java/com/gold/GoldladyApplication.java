package com.gold;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages={"com.gold"})// 扫描该包路径下的所有spring组件
@EntityScan("com.gold.model")// 扫描实体类
@EnableScheduling
public class GoldladyApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoldladyApplication.class, args);
	}
}
