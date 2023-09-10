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
//value(默认)：设置数据库表名称
@TableName("monitorItem")
public class MonitorItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("host_id")
    private Integer hostId;
    @TableField("monitorType_id")
    private Integer monitorTypeId;
    @TableField("detail")
    private String detail;
    @TableField("warnValue")
    private Double warnValue;
}
