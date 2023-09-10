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
@TableName("monitorAlarm")
public class MonitorAlarm {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("host_id")
    private Integer hostId;
    @TableField("monitorType_id")
    private Integer monitorTypeId;
    @TableField("monitorItem_id")
    private Integer monitorItemId;
    @TableField("data")
    private String data;
    @TableField("alarmTime")
    private String alarmTime;
}
