package com.tanzhou.service;

import com.tanzhou.dto.PaginationDTO;
import com.tanzhou.dto.QuestionDTO;
import com.tanzhou.dto.QuestionQueryDTO;
import com.tanzhou.exception.CustomizeErrorCode;
import com.tanzhou.exception.CustomizeException;
import com.tanzhou.mapper.QuestionExtMapper;
import com.tanzhou.mapper.QuestionMapper;
import com.tanzhou.mapper.UserMapper;
import com.tanzhou.model.Question;
import com.tanzhou.model.QuestionExample;
import com.tanzhou.model.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private QuestionExtMapper questionExtMapper;

    @Autowired
    private UserMapper userMapper;
    /**
     * 首页查询问题数据
     * */
    public PaginationDTO list(String search,Integer page,Integer size){
        PaginationDTO<QuestionDTO> paginationDTO = new PaginationDTO<>();
        if (StringUtils.isNotBlank(search)) {
            String[]tags = search.split(" ");
            search = Arrays.stream(tags).collect(Collectors.joining("|"));
        }
        QuestionQueryDTO questionQueryDTO = new QuestionQueryDTO();
        questionQueryDTO.setSearch(search);
        Integer totalCount = (int)questionExtMapper.countBySearch(questionQueryDTO);
        int totalPage = (totalCount % size == 0 ? totalCount/size :totalCount/size+1);
        if(page<1){
            page = 1;
        }
        if(page>totalPage){
            page = totalPage;
        }
        if(size < 0){
            size = 3;
        }
        paginationDTO.setPagination(totalPage,page);
        Integer offset = page < 1 ? 0 : size * (page - 1);
        questionQueryDTO.setPage(offset);
        questionQueryDTO.setSize(size);
        List<Question> questions = questionExtMapper.selectBySearch(questionQueryDTO);
        List<QuestionDTO> questionDTOS = new ArrayList<>();
        for (Question question:questions){
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            questionDTO.setUser(user);
            questionDTOS.add(questionDTO);
        }
        paginationDTO.setData(questionDTOS);
        return paginationDTO;
    }
    /**
     * 查询用户自身的数据
     * */
    public PaginationDTO list(Long id, Integer page, Integer size) {
        //创建封装对象
        PaginationDTO<QuestionDTO> paginationDTO = new PaginationDTO<>();
        //查询当前用户自己的提问数目
        QuestionExample questionExample = new QuestionExample();
        questionExample.createCriteria().andCreatorEqualTo(id);
        Integer totalCount = (int)questionMapper.countByExample(questionExample);
        int totalPage = (totalCount % size == 0 ? totalCount/size :totalCount/size+1);
        if(page<1){
            page = 1;
        }
        if(page>totalPage){
            page = totalPage;
        }
        if(size < 0){
            size = 3;
        }
        paginationDTO.setPagination(totalPage,page);
        //查询当前用户自己的提问对象
        Integer offset = size *(page-1);
        QuestionExample questionExample1 = new QuestionExample();
        questionExample1.createCriteria().andCreatorEqualTo(id);
        //将获取的数据进行倒序
        questionExample1.setOrderByClause("gmt_create desc");
        List<Question> questions = questionMapper.selectByExampleWithRowbounds(questionExample1,new RowBounds(offset,size));
        List<QuestionDTO> questionDTOS = new ArrayList<>();
        //将question疯转为DTO对象，并将他传入到PagnationDTO
        for (Question question:questions){
            User user = userMapper.selectByPrimaryKey(question.getCreator());
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(question,questionDTO);
            questionDTO.setUser(user);
            questionDTOS.add(questionDTO);
        }
        paginationDTO.setData(questionDTOS);
        return paginationDTO;
    }

    public QuestionDTO getById(Long id) {
        Question question = questionMapper.selectByPrimaryKey(id);
        if (question == null){
            throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
        }
        User user = userMapper.selectByPrimaryKey(question.getCreator());
        QuestionDTO questionDTO = new QuestionDTO();
        BeanUtils.copyProperties(question,questionDTO);
        questionDTO.setUser(user);
        return questionDTO;
    }

    public void createOrUpdate(Question question) {
        if(question.getId() == null){
            //创建
            question.setGmtCreate(System.currentTimeMillis());
            question.setGmtModified(question.getGmtCreate());
            question.setViewCount(0);
            question.setLikeCount(0);
            question.setCommentCount(0);
            questionMapper.insert(question);
            System.out.println(111);
        }else {
            //更新
            Question updateQuestion = new Question();
            updateQuestion.setGmtModified(System.currentTimeMillis());
            updateQuestion.setTitle(question.getTitle());
            updateQuestion.setDescription(question.getDescription());
            updateQuestion.setTag(question.getTag());
            QuestionExample questionExample  = new QuestionExample();
            questionExample.createCriteria().andIdEqualTo(question.getId());
            int updated = questionMapper.updateByExampleSelective(updateQuestion,questionExample);
            if (updated != 1){
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
        }
    }

    public void incView(Long id) {
        Question question = new Question();
        question.setId(id);
        question.setViewCount(1);
        questionExtMapper.incView(question);
    }

    public List<QuestionDTO> selectRelated(QuestionDTO queryDTO) {
        if (StringUtils.isBlank(queryDTO.getTag())) {
            return new ArrayList<>();
        }
        //将tag值获取并将其拆分组装成tag1|tag2|tag3的形式
        String[]tags = StringUtils.split(queryDTO.getTag(),",");
        String regexpTag = Arrays.stream(tags).filter(StringUtils::isNotBlank).map(t -> t.replace("+", "").replace("*", "").replace("?", ""))
                .filter(StringUtils::isNotBlank).collect(Collectors.joining("|"));
        Question question = new Question();
        question.setId(queryDTO.getId());
        question.setTag(regexpTag);
        List<Question>questions = questionExtMapper.selectRelated(question);
        //将questions转化为DTO对象
        List<QuestionDTO> questionDTOS = questions.stream().map(q->{
            QuestionDTO questionDTO = new QuestionDTO();
            BeanUtils.copyProperties(q,questionDTO);
            return questionDTO;
        }).collect(Collectors.toList());
        return questionDTOS;
    }
}
