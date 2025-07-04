package tianpan.mcpalarmcenter.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import tianpan.mcpalarmcenter.entity.AlarmInfo;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlarmInfoRepository extends MongoRepository<AlarmInfo, String> {
    
    /**
     * 查询最新的告警信息，按结束时间倒序排列
     */
    @Query(value = "{}", sort = "{'end_time': -1}")
    Page<AlarmInfo> findLatestAlarms(Pageable pageable);
    
    /**
     * 查询指定时间范围内的告警数量
     */
    long countByEndTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据alertId查询告警信息
     */
    AlarmInfo findByAlertId(String alertId);
    
    /**
     * 查询指定时间范围内的告警信息
     */
    @Query("{'end_time': {$gte: ?0, $lte: ?1}}")
    List<AlarmInfo> findByEndTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
} 