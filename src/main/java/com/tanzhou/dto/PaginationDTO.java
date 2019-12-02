package com.tanzhou.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PaginationDTO {
    private List<QuestionDTO> questions;
    private boolean showPrevious;
    private boolean showFirstPage;
    private boolean showNext;
    private boolean showEndPage;
    private Integer totalPage;
    private Integer page;
    private List<Integer> pages = new ArrayList<>();
    public void setPagination(Integer totalCount,Integer page,Integer size){
        //当不足为一页时，以一页计算
        totalPage = (totalCount%size==0?totalCount/size:totalCount/size+1);
        if(page<1){
            page = 1;
        }
        if(page > totalPage){
            page = totalPage;
        }
        this.page = page;
        //分页部署（当前页前后分别最多有3页，总页数最多为7页）
        // 1 将当前页加入到list集合中
        pages.add(page);
        // 2 以3为分界点进行判断,当前页到首页的页数小于3时（不包括当前页）
        for (int i=1;i<=3;i++){
            // 3 将当前页即之前页码加入到集合中
            if(page-i>0){
                pages.add(0,page-i);
            }
            //4 将当前页及以后页码加入到list中
            if(page+i<=totalPage){
                pages.add(page+i);
            }
        }
        //是否展示上一页
        showPrevious = (page==1?false:true);
        //是否展示下一页
        showNext = (page==totalPage?false:true);
        //是否展示第一页
        showFirstPage = pages.contains(1)?false:true;
        //是否展示最后一页
        showEndPage = pages.contains(totalPage)?false:true;
    }
}
