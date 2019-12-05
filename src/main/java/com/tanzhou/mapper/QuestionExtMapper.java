package com.tanzhou.mapper;

import com.tanzhou.model.Question;

public interface QuestionExtMapper {
    int incView(Question question);
    int incCommentCount(Question question);
}
