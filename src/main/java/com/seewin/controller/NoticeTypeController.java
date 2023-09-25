package com.seewin.controller;

import com.seewin.entity.Result;
import com.seewin.service.NoticeTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NoticeTypeController {
    @Autowired
    private NoticeTypeService noticeTypeService;
    @GetMapping("/noticeType")
    public Result getNoticeType(){
        return noticeTypeService.getNoticeType();
    }
    @GetMapping("/noticeType/{id}")
    public Result getNoticeTypeById(@PathVariable Integer id){
        return noticeTypeService.getNoticeTypeById(id);
    }
}
