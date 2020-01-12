package com.tanzhou.controller;

import com.tanzhou.dto.PaginationDTO;
import com.tanzhou.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {

    @Autowired
    private QuestionService questionService;

    @RequestMapping("/")
    public String index(HttpServletRequest request, Model model,
                        @RequestParam(value = "page",defaultValue = "1") Integer page,
                        @RequestParam(value = "size",defaultValue = "3") Integer size,
                        @RequestParam(value = "search",required = false) String search){
        PaginationDTO pagination = questionService.list(search,page,size);
        model.addAttribute("search",search);
        model.addAttribute("pagination",pagination);
        return "index";
    }
}
