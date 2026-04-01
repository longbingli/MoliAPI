package com.bingli.moliapiinterface.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AnswerBookResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String question;

    private String answer;
}
