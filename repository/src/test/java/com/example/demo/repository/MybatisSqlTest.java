package com.example.demo.repository;

import com.example.demo.repository.mapper.TestSqlMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnabledIfEnvironmentVariable(named = "TEST_DB_URL", matches = ".+")
class MybatisSqlTest {

    @Autowired
    private TestSqlMapper mapper;

    @Test
    void executesAnnotationSql() {
        assertEquals(42, mapper.selectNumber(42));
    }

    @Test
    void executesXmlSql() {
        assertEquals("mybatis-test", mapper.selectText("mybatis-test"));
    }
}
