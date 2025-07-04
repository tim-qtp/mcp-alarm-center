package tianpan.mcpalarmcenter.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "alarm_info")
public class AlarmInfo {
    
    @Id
    private String id;
    
    @Field("case_exec_id")
    private String caseExecId;
    
    @Field("alarm_level")
    private String alarmLevel;
    
    @Field("alarm_type")
    private String alarmType;
    
    @Field("alert_id")
    private String alertId;
    
    private String company;
    
    @Field("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @Field("fail_reason")
    private String failReason;
    
    private Boolean isRecover;
    
    @Field("layer_name")
    private String layerName;
    
    @Field("region_name")
    private String regionName;
    
    private Integer status;
    
    @Field("system_name")
    private String systemName;
    
    @Field("task_name")
    private String taskName;
    
    private Integer type;
    
    @Field("recover_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recoverTime;
    
    @Field("ave_time")
    private String aveTime;
    
    @Field("begin_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;
    
    private String host;
    
    private String response;
    
    @Field("actual_value")
    private String actualValue;
    
    @Field("is_reply")
    private Integer isReply;
} 