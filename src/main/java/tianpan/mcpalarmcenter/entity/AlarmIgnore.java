package tianpan.mcpalarmcenter.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "alarm_ignore")
public class AlarmIgnore {
    
    @Id
    private String id;
    
    @Field("alert_id")
    private String alertId;
    
    private String failed;
    
    private String reaction;
    
    @Field("is_ignore")
    private Boolean isIgnore;
} 