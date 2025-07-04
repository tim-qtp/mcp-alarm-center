package tianpan.mcpalarmcenter.mcp.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import tianpan.mcpalarmcenter.dto.AlarmCountResponse;
import tianpan.mcpalarmcenter.dto.AlarmResponse;
import tianpan.mcpalarmcenter.service.AlarmService;
import java.time.LocalDateTime;

@Component
public class AlarmTools {
    
    private final AlarmService alarmService;
    
    public AlarmTools(AlarmService alarmService) {
        this.alarmService = alarmService;
    }
    
    @Tool(name = "get_latest_alarms", description = "获取最新的告警信息，最多返回10条")
    public AlarmResponse getLatestAlarms(
            @ToolParam(description = "限制返回的告警数量，取值范围1-10，超过10条时只返回10条") int limit) {
        return alarmService.getLatestAlarms(limit);
    }
    
    @Tool(name = "get_alarm_count", description = "获取指定时间范围的告警数量")
    public AlarmCountResponse getAlarmCount(
            @ToolParam(description = "时间范围，支持：today(今天), yesterday(昨天), last7days(最近7天), last30days(最近30天), lastmonth(最近一个月), last3months(最近三个月)") String timeRange) {
        return alarmService.getAlarmCount(timeRange);
    }
    
    @Tool(name = "ignore_alarm", description = "忽略指定的告警，将其标记为已忽略状态")
    public String ignoreAlarm(
            @ToolParam(description = "告警的唯一标识符(alertId)，例如：ALM001") String alertId) {
        return alarmService.ignoreAlarm(alertId);
    }
    
    @Tool(name = "get_alarm_count_between", description = "获取指定时间区间的告警数量")
    public AlarmCountResponse getAlarmCountBetween(
            @ToolParam(description = "区间起始时间，格式如 2025-07-01T00:00:00") String start,
            @ToolParam(description = "区间结束时间，格式如 2025-07-04T23:59:59") String end) {
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);
        return alarmService.getAlarmCountBetween(startTime, endTime);
    }
} 