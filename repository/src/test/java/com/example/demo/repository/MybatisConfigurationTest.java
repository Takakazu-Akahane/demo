package com.example.demo.repository;

import com.example.demo.repository.mapper.TestSqlMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class MybatisConfigurationTest {

    @Autowired
    private TestSqlMapper mapper;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    void registersMapperAndLoadsAnnotationAndXmlStatementsWithoutConnectingToDatabase() {
        assertNotNull(mapper);
        var configuration = sqlSessionFactory.getConfiguration();
        assertTrue(configuration.hasStatement(TestSqlMapper.class.getName() + ".selectNumber"));
        assertTrue(configuration.hasStatement(TestSqlMapper.class.getName() + ".selectText"));
    }
}
