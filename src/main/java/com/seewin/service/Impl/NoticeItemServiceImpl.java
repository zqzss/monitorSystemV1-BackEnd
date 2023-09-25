package com.seewin.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seewin.entity.NoticeItem;
import com.seewin.entity.NoticeType;
import com.seewin.entity.Result;
import com.seewin.entity.User;
import com.seewin.mapper.HostMapper;
import com.seewin.mapper.NoticeItemMapper;
import com.seewin.mapper.NoticeTypeMapper;
import com.seewin.mapper.UserMapper;
import com.seewin.service.NoticeItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NoticeItemServiceImpl implements NoticeItemService {
    @Autowired
    private NoticeTypeMapper noticeTypeMapper;
    @Autowired
    private NoticeItemMapper noticeItemMapper;
    @Autowired
    private HostMapper hostMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public Result addNoticeItem(HashMap postData) {
//        获取前端传来的数据
        List<String> usernames = (List<String>) postData.get("users");
        Integer intervalMinute = null;
        if (postData.get("intervalMinute") instanceof Integer){
            intervalMinute = (Integer) postData.get("intervalMinute");
        }
        else {
            intervalMinute = Integer.valueOf((String) postData.get("intervalMinute"));
        }
        String name = (String) postData.get("name");
        log.info(postData.toString());
//        用通知类型名字找通知类型id,再找通知项的userId
        LambdaQueryWrapper<NoticeType> noticeTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        noticeTypeLambdaQueryWrapper.eq(name!=null,NoticeType::getName,name);
        NoticeType noticeType = noticeTypeMapper.selectOne(noticeTypeLambdaQueryWrapper);
        if (intervalMinute != noticeType.getIntervalMinute()){
            noticeType.setIntervalMinute(intervalMinute);
            noticeTypeMapper.updateById(noticeType);
        }
        Integer noticeTypeId = noticeType.getId();
        LambdaQueryWrapper<NoticeItem> noticeItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        noticeItemLambdaQueryWrapper.eq(noticeType!=null,NoticeItem::getNoticeTypeId,noticeTypeId);
        List<NoticeItem> noticeItems = noticeItemMapper.selectList(noticeItemLambdaQueryWrapper);
        List<Integer> noticeItemUserIds = new ArrayList<>();
        for (NoticeItem noticeItem: noticeItems){
            noticeItemUserIds.add(noticeItem.getUserId());
        }
//        用前端上传的用户名查用户id
        List<Integer> userIds = new ArrayList<>();
        List<User> users = new ArrayList<>();
        if (usernames!=null&&usernames.size()!=0){
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.in(User::getUsername,usernames);
            users = userMapper.selectList(userLambdaQueryWrapper);
        }

        for (User user : users){
            userIds.add(Math.toIntExact(user.getId()));
        }
//        通知项的userId和前端上传的用户Id做对比，通知项的有前端上传的没有，则删除对应的通知项。通知项的没有前端上传的有，则新添对应的通知项
        List<Integer> userIds2 = userIds.stream().collect(Collectors.toList());
        userIds2.removeAll(noticeItemUserIds);
        for (Integer userId : userIds2){
            NoticeItem noticeItem = new NoticeItem();
            noticeItem.setUserId(userId);
            noticeItem.setNoticeTypeId(noticeTypeId);
            LocalDateTime now = LocalDateTime.now();
            noticeItem.setCreateTime(now);
            log.info("添加通知项: "+noticeItem);
            noticeItemMapper.insert(noticeItem);
        }
        noticeItemUserIds.removeAll(userIds);
        for (Integer userId:noticeItemUserIds){
            LambdaQueryWrapper<NoticeItem> noticeItemLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
            noticeItemLambdaQueryWrapper1.eq(NoticeItem::getUserId,userId);
            log.info("通过userId: " + userId + "删除通知项");
            noticeItemMapper.delete(noticeItemLambdaQueryWrapper1);
        }
        return new Result<>(200,null,"新添成功！");
    }

    @Override
    public Result getUserNameByNoticeTypeName(String name) {
//        用通知类型名称找通知类型Id
        LambdaQueryWrapper<NoticeType> noticeTypeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        noticeTypeLambdaQueryWrapper.eq(name!=null,NoticeType::getName,name);
        NoticeType noticeType = noticeTypeMapper.selectOne(noticeTypeLambdaQueryWrapper);
        Integer noticeTypeId = noticeType.getId();
//        用通知类型Id找监控项
        LambdaQueryWrapper<NoticeItem> noticeItemLambdaQueryWrapper = new LambdaQueryWrapper<>();
        noticeItemLambdaQueryWrapper.eq(noticeTypeId!=null,NoticeItem::getNoticeTypeId,noticeTypeId);
        List<NoticeItem> noticeItems = noticeItemMapper.selectList(noticeItemLambdaQueryWrapper);
//        用监控项的userId找username
        List<String> usernames = new ArrayList<>();
        for (NoticeItem noticeItem : noticeItems){
            User user = userMapper.selectById(noticeItem.getUserId());
            usernames.add(user.getUsername());
        }
        log.info("通过通知监控类型名称: "+name+" 查询用户名: "+usernames);
        return new Result<>(200,usernames,"查询成功！");
    }
}
