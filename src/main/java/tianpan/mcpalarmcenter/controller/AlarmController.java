package tianpan.mcpalarmcenter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tianpan.mcpalarmcenter.dto.AlarmCountResponse;
import tianpan.mcpalarmcenter.dto.AlarmResponse;
import tianpan.mcpalarmcenter.service.AlarmService;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {
    
    private final AlarmService alarmService;
    
    @GetMapping("/latest")
    public AlarmResponse getLatestAlarms(@RequestParam(defaultValue = "5") int limit) {
        return alarmService.getLatestAlarms(limit);
    }
    
    @GetMapping("/count")
    public AlarmCountResponse getAlarmCount(@RequestParam String timeRange) {
        return alarmService.getAlarmCount(timeRange);
    }
    
    @GetMapping("/count/range")
    public AlarmCountResponse getAlarmCountBetween(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return alarmService.getAlarmCountBetween(start, end);
    }
    
    @PostMapping("/{alertId}/ignore")
    public String ignoreAlarm(@PathVariable String alertId) {
        return alarmService.ignoreAlarm(alertId);
    }
    
    @GetMapping("/health")
    public String health() {
        return "MCP Alarm Center is running!";
    }
} 