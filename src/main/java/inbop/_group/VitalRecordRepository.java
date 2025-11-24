package inbop._group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VitalRecordRepository extends JpaRepository<VitalRecord, Long> {
    
    // 사용자의 최신 바이탈 기록 조회
    VitalRecord findFirstByUserIdOrderByRecordedAtDesc(Long userId);
    
    // 사용자의 특정 기간 바이탈 기록 조회
    List<VitalRecord> findByUserIdAndRecordedAtBetweenOrderByRecordedAtDesc(
        Long userId, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    // 사용자의 모든 기록 조회 (최신순)
    List<VitalRecord> findByUserIdOrderByRecordedAtDesc(Long userId);
    
    // 날짜별 평균 심박수 조회 (날짜 프로젝션)
    @Query("SELECT DATE(v.recordedAt) as date, AVG(v.hr) as avgHr " +
           "FROM VitalRecord v " +
           "WHERE v.userId = :userId " +
           "AND v.recordedAt >= :startDate " +
           "GROUP BY DATE(v.recordedAt) " +
           "ORDER BY date")
    List<DailyAvgHeartRate> findDailyAvgHeartRate(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate
    );
    
    // 인터페이스 프로젝션
    interface DailyAvgHeartRate {
        java.time.LocalDate getDate();
        Double getAvgHr();
    }
}









