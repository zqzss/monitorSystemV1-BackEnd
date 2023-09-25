package com.seewin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//value(默认)：设置数据库表名称
@TableName("host")
public class Host {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @TableField("hostname")
    private String hostName;
    @TableField("IP")
    private String ip;
    private Integer port;
    @TableField("username")
    private String userName;
    private String password;
    private String description;
    private String reason;
    private Integer enable;
    @TableField("reConnectNumber")
    private Integer reConnectNumber;
    //    逻辑删除:为数据设置是否可用状态字段，删除时设置状态字段为不可用状态，数据保留在数据库中，执行的是update操作
//    @TableLogic(value="0",delval="1")
//value为正常数据的值，delval为删除数据的值
//    private Integer deleted;

    //    乐观锁
    @Version
    private Integer version;
}
