package com.tanzhou.controller;

import com.tanzhou.dto.QuestionDTO;
import com.tanzhou.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/question/{id}")
    public String question(@PathVariable(name="id") String id, Model model){
        Long questionId = null;
        questionId = Long.parseLong(id);
        //增加阅读数量
        questionService.incView(questionId);
        QuestionDTO questionDTO = questionService.getById(questionId);
        model.addAttribute("question",questionDTO);
        return "question";
    }
}
