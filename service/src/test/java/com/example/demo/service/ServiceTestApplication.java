package com.example.demo.service;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.example.demo.service",
        "com.example.demo.core"
})
@MapperScan("com.example.demo.repository.mapper")
public class ServiceTestApplication {
}
