package tianpan.mcpalarmcenter.mcp.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import tianpan.mcpalarmcenter.dto.AlarmCountResponse;
import tianpan.mcpalarmcenter.dto.AlarmResponse;
import tianpan.mcpalarmcenter.service.AlarmService;
import java.time.LocalDateTime;

@Slf4j
@Component
public class AlarmTools {
    
    private final AlarmService alarmService;
    
    public AlarmTools(AlarmService alarmService) {
        this.alarmService = alarmService;
        log.info("=== AlarmTools初始化完成 ===");
    }
    
    @Tool(name = "get_latest_alarms", description = "获取最新的告警信息，最多返回10条")
    public AlarmResponse getLatestAlarms(
            @ToolParam(description = "限制返回的告警数量，取值范围1-10，超过10条时只返回10条") int limit) {
        log.info("=== 调用get_latest_alarms工具 ===");
        log.info("参数limit: {}", limit);
        
        try {
            AlarmResponse response = alarmService.getLatestAlarms(limit);
            log.info("工具调用成功，返回告警数量: {}", response.getAlarms().size());
            return response;
        } catch (Exception e) {
            log.error("工具调用失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Tool(name = "get_alarm_count", description = "获取指定时间范围的告警数量")
    public AlarmCountResponse getAlarmCount(
            @ToolParam(description = "时间范围，支持：today(今天), yesterday(昨天), last7days(最近7天), last30days(最近30天), lastmonth(最近一个月), last3months(最近三个月)") String timeRange) {
        log.info("=== 调用get_alarm_count工具 ===");
        log.info("参数timeRange: {}", timeRange);
        
        try {
            AlarmCountResponse response = alarmService.getAlarmCount(timeRange);
            log.info("工具调用成功，告警数量: {}", response.getCount());
            return response;
        } catch (Exception e) {
            log.error("工具调用失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Tool(name = "ignore_alarm", description = "忽略指定的告警，将其标记为已忽略状态")
    public String ignoreAlarm(
            @ToolParam(description = "告警的唯一标识符(alertId)，例如：ALM001") String alertId) {
        log.info("=== 调用ignore_alarm工具 ===");
        log.info("参数alertId: {}", alertId);
        
        try {
            String result = alarmService.ignoreAlarm(alertId);
            log.info("工具调用成功，结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("工具调用失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Tool(name = "get_alarm_count_between", description = "获取指定时间区间的告警数量")
    public AlarmCountResponse getAlarmCountBetween(
            @ToolParam(description = "区间起始时间，格式如 2025-07-01T00:00:00") String start,
            @ToolParam(description = "区间结束时间，格式如 2025-07-04T23:59:59") String end) {
        log.info("=== 调用get_alarm_count_between工具 ===");
        log.info("参数start: {}, end: {}", start, end);
        
        try {
            LocalDateTime startTime = LocalDateTime.parse(start);
            LocalDateTime endTime = LocalDateTime.parse(end);
            log.info("解析时间成功: {} -> {}", startTime, endTime);
            
            AlarmCountResponse response = alarmService.getAlarmCountBetween(startTime, endTime);
            log.info("工具调用成功，告警数量: {}", response.getCount());
            return response;
        } catch (Exception e) {
            log.error("工具调用失败: {}", e.getMessage(), e);
            throw e;
        }
    }
} 