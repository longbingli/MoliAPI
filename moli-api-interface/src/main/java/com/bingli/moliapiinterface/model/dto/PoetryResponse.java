package com.bingli.moliapiinterface.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class PoetryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String author;

    private String dynasty;

    private String content;

    private String tags;

    private LocalDateTime createTime;
}
