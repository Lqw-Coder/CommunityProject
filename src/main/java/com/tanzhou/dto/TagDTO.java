package com.tanzhou.dto;

import lombok.Data;

import java.util.List;

@Data
public class TagDTO {
    /**
     * 发布页面中tag的一级标题
     * */
    private String categoryName;
    /**
     * 发布页面中tag的二级内容
     * */
    private List<String> tags;
}
