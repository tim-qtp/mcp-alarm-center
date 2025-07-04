package tianpan.mcpalarmcenter.dto;

import lombok.Data;
import tianpan.mcpalarmcenter.entity.AlarmInfo;

import java.util.List;

@Data
public class AlarmResponse {
    private List<AlarmInfo> alarms;
    private int totalCount;
    private boolean hasMore;
    private String message;
    
    public AlarmResponse(List<AlarmInfo> alarms, int totalCount, boolean hasMore) {
        this.alarms = alarms;
        this.totalCount = totalCount;
        this.hasMore = hasMore;
    }
    
    public AlarmResponse(String message) {
        this.message = message;
    }
} 