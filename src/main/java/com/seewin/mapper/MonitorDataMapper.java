package com.seewin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seewin.entity.MonitorData;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;

@Mapper
public interface MonitorDataMapper extends BaseMapper<MonitorData> {
    @Delete("DELETE FROM monitorData WHERE createTime < #{oneDayAgo}")
    void deleteOldData(@Param("oneDayAgo") Date oneDayAgo);
}
