package com.example.demo.repository.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface TestSqlMapper {

    @Select("SELECT #{value}")
    int selectNumber(@Param("value") int value);

    String selectText(@Param("value") String value);
}
