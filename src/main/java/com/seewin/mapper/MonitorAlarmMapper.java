package com.seewin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seewin.entity.MonitorAlarm;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MonitorAlarmMapper extends BaseMapper<MonitorAlarm> {
    List<MonitorAlarm> selectMonitorAlarmByQuery(List<Integer> host_ids,String queryStartDateTime,String queryEndDateTime,Integer pageSize,Integer offSet,String startTime,String endTime,String startDate,String endDate);
    Integer selectMonitorAlarmOfCount(List<Integer> host_ids,String queryStartDateTime,String queryEndDateTime,String startTime,String endTime,String startDate,String endDate);
}
