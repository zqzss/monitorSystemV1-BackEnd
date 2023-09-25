package com.seewin.service;

import com.seewin.entity.Result;

import java.util.HashMap;

public interface NoticeItemService {
    public Result addNoticeItem(HashMap postData);
    public Result getUserNameByNoticeTypeName(String name);
}
