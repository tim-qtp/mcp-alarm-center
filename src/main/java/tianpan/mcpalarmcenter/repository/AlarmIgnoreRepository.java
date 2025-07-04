package tianpan.mcpalarmcenter.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tianpan.mcpalarmcenter.entity.AlarmIgnore;

@Repository
public interface AlarmIgnoreRepository extends MongoRepository<AlarmIgnore, String> {
    
    /**
     * 根据alertId查询忽略记录
     */
    AlarmIgnore findByAlertId(String alertId);
    
    /**
     * 检查是否存在忽略记录
     */
    boolean existsByAlertId(String alertId);
} 