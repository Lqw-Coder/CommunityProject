package com.tanzhou.service;

import com.tanzhou.dto.NotificationDTO;
import com.tanzhou.dto.PaginationDTO;
import com.tanzhou.dto.QuestionDTO;
import com.tanzhou.enums.NotificationStatusEnum;
import com.tanzhou.enums.NotificationTypeEnum;
import com.tanzhou.exception.CustomizeErrorCode;
import com.tanzhou.exception.CustomizeException;
import com.tanzhou.mapper.NotificationMapper;
import com.tanzhou.model.*;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class NotificationService {

    @Autowired
    NotificationMapper notificationMapper;

    public PaginationDTO list(Long userId, Integer page, Integer size) {
        //创建封装对象
        PaginationDTO<NotificationDTO> paginationDTO = new PaginationDTO<>();
        //查询通知的数目
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andReceiverEqualTo(userId);
        Integer totalCount = (int)notificationMapper.countByExample(notificationExample);
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
        NotificationExample notificationExample1 = new NotificationExample();
        notificationExample1.createCriteria().andReceiverEqualTo(userId);
        notificationExample1.setOrderByClause("gmt_create desc");
        List<Notification> notifications = notificationMapper.selectByExampleWithRowbounds(notificationExample1,new RowBounds(offset,size));
        List<NotificationDTO> notificationDTOS = new ArrayList<>();
        //将question疯转为DTO对象，并将他传入到PagnationDTO
        for (Notification notification : notifications) {
            NotificationDTO notificationDTO = new NotificationDTO();
            BeanUtils.copyProperties(notification, notificationDTO);
            notificationDTO.setTypeName(NotificationTypeEnum.nameOfType(notification.getType()));
            notificationDTOS.add(notificationDTO);
        }
        paginationDTO.setData(notificationDTOS);
        return paginationDTO;
    }
    public Long unreadCount(Long userId){
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria().andReceiverEqualTo(userId).andStatusEqualTo(NotificationStatusEnum.UNREAD.getStatus());
        return notificationMapper.countByExample(notificationExample);
    }

    public NotificationDTO read(Long id, User user) {
        Notification notification = notificationMapper.selectByPrimaryKey(id);
        if (notification == null) {
            throw new CustomizeException(CustomizeErrorCode.NOTIFICATION_NOT_FOUND);
        }
        if (!Objects.equals(notification.getReceiver(), user.getId())) {
            throw new CustomizeException(CustomizeErrorCode.READ_NOTIFICATION_FAIL);
        }
        //修改状态
        notification.setStatus(NotificationStatusEnum.READ.getStatus());
        notificationMapper.updateByPrimaryKey(notification);
        NotificationDTO notificationDTO = new NotificationDTO();
        BeanUtils.copyProperties(notification,notificationDTO);
        notificationDTO.setTypeName(NotificationTypeEnum.nameOfType(notification.getType()));
        return notificationDTO;
    }
}
