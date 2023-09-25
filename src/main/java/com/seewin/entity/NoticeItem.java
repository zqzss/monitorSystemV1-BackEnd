package com.seewin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("noticeItem")
public class NoticeItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("noticeType_id")
    private Integer noticeTypeId;
    @TableField("user_id")
    private Integer userId;
    @TableField("createTime")
    private LocalDateTime createTime;
    @TableField("lastNoticeTime")
    private LocalDateTime lastNoticeTime;
}
