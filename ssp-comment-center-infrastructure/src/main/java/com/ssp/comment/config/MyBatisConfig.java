package com.ssp.comment.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.ssp.comment.dao.mapper")
public class MyBatisConfig {
}
