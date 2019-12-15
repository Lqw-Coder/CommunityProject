package com.tanzhou.controller;

import com.tanzhou.dto.CommentDTO;
import com.tanzhou.dto.QuestionDTO;
import com.tanzhou.enums.CommentTypeEnum;
import com.tanzhou.service.CommentService;
import com.tanzhou.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CommentService commentService;

    @GetMapping("/question/{id}")
    public String question(@PathVariable(name="id") String id, Model model){
        Long questionId = null;
        questionId = Long.parseLong(id);
        //增加阅读数量
        questionService.incView(questionId);
        QuestionDTO questionDTO = questionService.getById(questionId);
        List<CommentDTO> comments = commentService.listByTargetId(questionId, CommentTypeEnum.QUESTION);
        List<QuestionDTO> relatedQuestions = questionService.selectRelated(questionDTO);
        model.addAttribute("comments",comments);
        model.addAttribute("question",questionDTO);
        model.addAttribute("relatedQuestions",relatedQuestions);
        return "question";
    }

}
