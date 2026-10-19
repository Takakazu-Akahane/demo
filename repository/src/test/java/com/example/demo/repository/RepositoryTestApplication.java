package com.example.demo.repository;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@SpringBootConfiguration
@EnableAutoConfiguration
@MapperScan("com.example.demo.repository.mapper")
public class RepositoryTestApplication {
}
