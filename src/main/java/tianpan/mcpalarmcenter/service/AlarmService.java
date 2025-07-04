package tianpan.mcpalarmcenter.service;

import tianpan.mcpalarmcenter.dto.AlarmCountResponse;
import tianpan.mcpalarmcenter.dto.AlarmResponse;

import java.time.LocalDateTime;

public interface AlarmService {
    
    /**
     * 获取最新的告警信息
     * @param limit 限制数量，最大10条
     * @return 告警响应
     */
    AlarmResponse getLatestAlarms(int limit);
    
    /**
     * 获取指定时间范围的告警数量
     * @param timeRange 时间范围（如：today, yesterday, last7days, last30days）
     * @return 告警数量响应
     */
    AlarmCountResponse getAlarmCount(String timeRange);
    
    /**
     * 忽略指定告警
     * @param alertId 告警ID
     * @return 操作结果
     */
    String ignoreAlarm(String alertId);

    // 新增：区间统计
    AlarmCountResponse getAlarmCountBetween(LocalDateTime start, LocalDateTime end);
} 