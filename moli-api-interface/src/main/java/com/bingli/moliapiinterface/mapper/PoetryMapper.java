package com.bingli.moliapiinterface.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bingli.moliapiinterface.model.entity.Poetry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PoetryMapper extends BaseMapper<Poetry> {

    @Select("SELECT * FROM poetry WHERE is_delete = 0 ORDER BY RAND() LIMIT 1")
    Poetry selectRandomOne();
}
