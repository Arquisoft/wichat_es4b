package com.uniovi.dto;

import com.uniovi.entities.Question;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


public class QuestionDto extends QuestionBaseDto {

    public QuestionDto (Question question) {
        super(question);
    }

}
