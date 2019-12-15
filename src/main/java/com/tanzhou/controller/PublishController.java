package com.tanzhou.controller;

import com.tanzhou.cache.TagCache;
import com.tanzhou.dto.QuestionDTO;
import com.tanzhou.dto.TagDTO;
import com.tanzhou.model.Question;
import com.tanzhou.model.User;
import com.tanzhou.service.QuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class PublishController {

    @Autowired
    private QuestionService questionService;

    /**
     * 发布问题中的编辑功能
     * */
    @GetMapping("/publish/{id}")
    public String edit(@PathVariable(name="id")Long id,Model model){
        QuestionDTO question = questionService.getById(id);
        model.addAttribute("title",question.getTitle());
        model.addAttribute("description",question.getDescription());
        model.addAttribute("tag",question.getTag());
        model.addAttribute("id",question.getId());
        List<TagDTO> tagDTOS = TagCache.get();
        model.addAttribute("tagDTOs",tagDTOS);
        return "publish";
    }
    /**
     * 首页中的提问功能
     * */
    @GetMapping("/publish")
    public String publish(Model model){
        List<TagDTO> tagDTOS = TagCache.get();
        model.addAttribute("tagDTOs",tagDTOS);
        return "publish";
    }
    /**
     * 发布页面中的问题提交功能
     * */
    @PostMapping("/publish")
    public String doPublish(@RequestParam(value = "title",required = false)String title,
                            @RequestParam(value = "description",required = false)String description,
                            @RequestParam(value = "tag",required = false)String tag,
                            @RequestParam("id") Long id, HttpServletRequest request,
                            Model model){
        model.addAttribute("title",title);
        model.addAttribute("description",description);
        model.addAttribute("tag",tag);
        List<TagDTO> tagDTOS = TagCache.get();
        model.addAttribute("tagDTOs",tagDTOS);
        if(title == null || title.equals("")){
            model.addAttribute("error","标题不能为空");
            return "publish";
        }
        if (description ==null ||description.equals("")){
            model.addAttribute("error","问题补充不能为空");
            return "publish";
        }
        if (tag ==null ||tag.equals("")){
            model.addAttribute("error","标签不能为空");
            return "publish";
        }
        //服务端中对选中的标签进行校验
        String invalid = TagCache.filterInvalid(tag);
        if (StringUtils.isNotBlank(invalid)) {
            model.addAttribute("error", "输入非法标签:" + invalid);
            return "publish";
        }

        User user = (User) request.getSession().getAttribute("user");
        if (user==null) {
            model.addAttribute("error","用户未登录");
            return "publish";
        }
        Question question = new Question();
        question.setTag(tag);
        question.setTitle(title);
        question.setDescription(description);
        question.setCreator(user.getId());
        question.setId(id);
        questionService.createOrUpdate(question);
        return "redirect:/";
    }
}
