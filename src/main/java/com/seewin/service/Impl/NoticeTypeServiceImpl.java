package com.seewin.service.Impl;

import com.seewin.entity.NoticeType;
import com.seewin.entity.Result;
import com.seewin.mapper.NoticeTypeMapper;
import com.seewin.service.NoticeTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NoticeTypeServiceImpl implements NoticeTypeService {
    @Autowired
    private NoticeTypeMapper noticeTypeMapper;
    @Override
    public Result getNoticeType() {
        List<NoticeType> noticeTypes = noticeTypeMapper.selectList(null);
        log.info("获取所有的通知类型: "+noticeTypes);
        return new Result<>(200,noticeTypes,"查询成功！");
    }

    @Override
    public Result getNoticeTypeById(Integer id) {
        NoticeType noticeType = noticeTypeMapper.selectById(id);
        log.info("通过noticeTypeId获取通知类型: "+noticeType);
        return new Result<>(200,noticeType,"查询成功！");
    }
}
