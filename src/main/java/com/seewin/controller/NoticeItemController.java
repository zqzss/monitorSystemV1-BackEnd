package com.seewin.controller;

import com.seewin.entity.Result;
import com.seewin.service.NoticeItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@Slf4j
@RequestMapping("/noticeItem")
public class NoticeItemController {
    @Autowired
    private NoticeItemService noticeItemService;
    @PostMapping()
    public Result addNoticeItem(@RequestBody HashMap postData){
        return noticeItemService.addNoticeItem(postData);
    }
    @GetMapping("/username")
    public Result getUserNameByNoticeTypeName(@RequestParam String name){
        return noticeItemService.getUserNameByNoticeTypeName(name);
    }
}
