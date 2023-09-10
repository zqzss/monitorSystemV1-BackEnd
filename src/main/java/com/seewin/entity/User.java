package com.seewin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//value(默认)：设置数据库表名称
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String fullname;
    private Integer age;
    private String password;
    private String tel;
    private String email;
//    value(默认)：设置数据库表字段名称
//    exist:设置属性在数据库表字段中是否存在，默认为true，此属性不能与value合并使用
//select:设置属性是否参与查询，此属性与select()映射配置不冲突
    @TableField(exist=false)
    private Integer online;

//    逻辑删除:为数据设置是否可用状态字段，删除时设置状态字段为不可用状态，数据保留在数据库中，执行的是update操作
    @TableLogic(value="0",delval="1")
//value为正常数据的值，delval为删除数据的值
    private Integer deleted;

//    乐观锁
    @Version
    private Integer version;
}
