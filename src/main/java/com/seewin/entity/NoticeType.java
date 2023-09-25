package com.seewin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("noticeType")
public class NoticeType {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    @TableField("intervalMinute")
    private Integer intervalMinute;
}
