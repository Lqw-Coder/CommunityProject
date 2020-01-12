package com.tanzhou.service;

import com.sun.org.apache.regexp.internal.RE;
import com.tanzhou.dto.CommentDTO;
import com.tanzhou.enums.CommentTypeEnum;
import com.tanzhou.enums.NotificationStatusEnum;
import com.tanzhou.enums.NotificationTypeEnum;
import com.tanzhou.exception.CustomizeErrorCode;
import com.tanzhou.exception.CustomizeException;
import com.tanzhou.mapper.*;
import com.tanzhou.model.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private QuestionExtMapper questionExtMapper;

    @Autowired
    private CommentExtMapper commentExtMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    public void insert(Comment comment,User commentator) {
        if (comment.getParentId()==null || comment.getParentId() == 0){
            throw new CustomizeException(CustomizeErrorCode.TARGET_PARAM_NOT_FOUND);
        }
        if(comment.getType() == null || !CommentTypeEnum.isExist(comment.getType())){
            throw new CustomizeException(CustomizeErrorCode.TYPE_PARAM_WRONG);
        }
        if(comment.getType() == CommentTypeEnum.COMMENT.getType()){
            //回复评论
            Comment dbComment = commentMapper.selectByPrimaryKey(comment.getParentId());
            if(dbComment == null){
                throw new CustomizeException(CustomizeErrorCode.COMMENT_NOT_FOUND);
            }
            //回复问题
            Question question = questionMapper.selectByPrimaryKey(dbComment.getParentId());
            commentMapper.insert(comment);
            createNotify(comment,dbComment.getCommentator(),commentator.getName(),question.getTitle(),NotificationTypeEnum.REPLY_COMMENT,question.getId());
            //插入回复评论成功后增加回复数
            Comment parentComment = new Comment();
            parentComment.setId(comment.getParentId());
            parentComment.setCommentCount(1);
            commentExtMapper.incCommentCount(parentComment);
        }else {
            //回复问题
            Question question = questionMapper.selectByPrimaryKey(comment.getParentId());
            if(question == null){
                throw new CustomizeException(CustomizeErrorCode.QUESTION_NOT_FOUND);
            }
            comment.setCommentCount(0);
            commentMapper.insert(comment);
            //通知
            createNotify(comment,question.getCreator(),commentator.getName(),question.getTitle(),NotificationTypeEnum.REPLY_QUESTION,question.getId());
            question.setCommentCount(1);
            questionExtMapper.incCommentCount(question);
        }
    }
    /**
     *  根据评论以及接收者的id，评论本身的类型（即回复问题还是回复评论）创建通知
     *
     * */
    private void createNotify(Comment comment, Long receiver, String notifierName,String outerTitle,NotificationTypeEnum notificationType,Long outerId){
        if (comment.getCommentator() == receiver) {
            return;
        }
        Notification notification = new Notification();
        notification.setGmtCreate(System.currentTimeMillis());
        notification.setType(notificationType.getType());//其中type代表回复的是问题还是评论
        notification.setNotifier(comment.getCommentator());//评论人
        notification.setReceiver(receiver);//问题或评论的user对象
        notification.setStatus(NotificationStatusEnum.UNREAD.getStatus());//状态代表已读还是未读
        notification.setOuterid(outerId);//问题id
        notification.setNotifierName(notifierName);
        notification.setOuterTitle(outerTitle);
        notificationMapper.insert(notification);
    }
    public List<CommentDTO> listByTargetId(Long id,CommentTypeEnum typeEnum){
        CommentExample commentExample = new CommentExample();
        Integer type = typeEnum.getType();
        commentExample.createCriteria().andParentIdEqualTo(id).andTypeEqualTo(type);
        commentExample.setOrderByClause("gmt_create desc");
        List<Comment> comments = commentMapper.selectByExample(commentExample);
        if (comments.size() == 0){
            return new ArrayList<>();
        }
        //获取去重的评论人
        Set<Long> commentators = comments.stream().map(comment -> comment.getCommentator()).collect(Collectors.toSet());
        List<Long>userIds = new ArrayList<>();
        userIds.addAll(commentators);
        //获取评论人并转换为Map
        UserExample userExample = new UserExample();
        userExample.createCriteria().andIdIn(userIds);
        List<User> users = userMapper.selectByExample(userExample);
        Map<Long,User> userMap = users.stream().collect(Collectors.toMap(user -> user.getId(),user -> user));
        //转换Comment为CommentDTO对象
        List<CommentDTO> commentDTOS = comments.stream().map(comment -> {
            CommentDTO commentDTO = new CommentDTO();
            BeanUtils.copyProperties(comment,commentDTO);
            commentDTO.setUser(userMap.get(comment.getCommentator()));
            return commentDTO;
        }).collect(Collectors.toList());
        return commentDTOS;
    }
}
