package tianpan.mcpalarmcenter.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tianpan.mcpalarmcenter.dto.AlarmCountResponse;
import tianpan.mcpalarmcenter.dto.AlarmResponse;
import tianpan.mcpalarmcenter.entity.AlarmInfo;
import tianpan.mcpalarmcenter.entity.AlarmIgnore;
import tianpan.mcpalarmcenter.repository.AlarmIgnoreRepository;
import tianpan.mcpalarmcenter.repository.AlarmInfoRepository;
import tianpan.mcpalarmcenter.service.AlarmService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmServiceImpl implements AlarmService {
    
    private final AlarmInfoRepository alarmInfoRepository;
    private final AlarmIgnoreRepository alarmIgnoreRepository;
    
    @Override
    public AlarmResponse getLatestAlarms(int limit) {
        try {
            // 限制最大数量为10
            int actualLimit = Math.min(limit, 10);
            
            Pageable pageable = PageRequest.of(0, actualLimit);
            Page<AlarmInfo> alarmPage = alarmInfoRepository.findLatestAlarms(pageable);
            
            List<AlarmInfo> alarms = alarmPage.getContent();
            boolean hasMore = alarmPage.getTotalElements() > actualLimit;
            
            return new AlarmResponse(alarms, (int) alarmPage.getTotalElements(), hasMore);
        } catch (Exception e) {
            log.error("获取最新告警失败", e);
            return new AlarmResponse("获取最新告警失败: " + e.getMessage());
        }
    }
    
    @Override
    public AlarmCountResponse getAlarmCount(String timeRange) {
        try {
            LocalDateTime startTime;
            LocalDateTime endTime;
            String rangeDescription;
            
            LocalDate today = LocalDate.now();
            
            switch (timeRange.toLowerCase()) {
                case "today":
                    startTime = today.atStartOfDay();
                    endTime = today.atTime(LocalTime.MAX);
                    rangeDescription = "今天";
                    break;
                case "yesterday":
                    startTime = today.minusDays(1).atStartOfDay();
                    endTime = today.minusDays(1).atTime(LocalTime.MAX);
                    rangeDescription = "昨天";
                    break;
                case "last7days":
                    startTime = today.minusDays(7).atStartOfDay();
                    endTime = today.atTime(LocalTime.MAX);
                    rangeDescription = "最近7天";
                    break;
                case "last30days":
                    startTime = today.minusDays(30).atStartOfDay();
                    endTime = today.atTime(LocalTime.MAX);
                    rangeDescription = "最近30天";
                    break;
                case "lastmonth":
                    startTime = today.minusMonths(1).atStartOfDay();
                    endTime = today.atTime(LocalTime.MAX);
                    rangeDescription = "最近一个月";
                    break;
                case "last3months":
                    startTime = today.minusMonths(3).atStartOfDay();
                    endTime = today.atTime(LocalTime.MAX);
                    rangeDescription = "最近三个月";
                    break;
                default:
                    return new AlarmCountResponse("不支持的时间范围: " + timeRange);
            }
            
            long count = alarmInfoRepository.countByEndTimeBetween(startTime, endTime);
            return new AlarmCountResponse(count, rangeDescription);
            
        } catch (Exception e) {
            log.error("获取告警数量失败", e);
            return new AlarmCountResponse("获取告警数量失败: " + e.getMessage());
        }
    }

    @Override
    public String ignoreAlarm(String alertId) {
        try {
            // 检查告警是否存在
            AlarmInfo alarmInfo = alarmInfoRepository.findByAlertId(alertId);
            if (alarmInfo == null) {
                return "告警不存在: " + alertId;
            }
            
            // 检查是否已经被忽略
            if (alarmIgnoreRepository.existsByAlertId(alertId)) {
                return "告警已经被忽略: " + alertId;
            }
            
            // 创建忽略记录
            AlarmIgnore alarmIgnore = new AlarmIgnore();
            alarmIgnore.setAlertId(alertId);
            alarmIgnore.setIsIgnore(true);
            alarmIgnore.setReaction("用户手动忽略");
            
            alarmIgnoreRepository.save(alarmIgnore);
            
            return "告警已成功忽略: " + alertId;
            
        } catch (Exception e) {
            log.error("忽略告警失败", e);
            return "忽略告警失败: " + e.getMessage();
        }
    }

    @Override
    public AlarmCountResponse getAlarmCountBetween(LocalDateTime start, LocalDateTime end) {
        try {
            long count = alarmInfoRepository.countByEndTimeBetween(start, end);
            String range = start + " ~ " + end;
            return new AlarmCountResponse(count, range);
        } catch (Exception e) {
            log.error("区间统计告警数量失败", e);
            return new AlarmCountResponse("区间统计告警数量失败: " + e.getMessage());
        }
    }
} 