package com.bingli.moliapiinterface.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bingli.moliapiinterface.model.entity.AnswerBook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AnswerBookMapper extends BaseMapper<AnswerBook> {

    @Select("SELECT * FROM answer_book WHERE is_delete = 0 ORDER BY RAND() LIMIT 1")
    AnswerBook selectRandomOne();
}
