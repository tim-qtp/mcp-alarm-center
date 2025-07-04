package tianpan.mcpalarmcenter.dto;

import lombok.Data;

@Data
public class AlarmCountResponse {
    private long count;
    private String timeRange;
    private String message;
    
    public AlarmCountResponse(long count, String timeRange) {
        this.count = count;
        this.timeRange = timeRange;
    }
    
    public AlarmCountResponse(String message) {
        this.message = message;
    }
} 