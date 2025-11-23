package inbop._group.heart_rate_alerts_api;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HeartRateAlertRepository extends JpaRepository<HeartRateAlert, Long> {

    // 날짜별 카운트 (startDate는 LocalDateTime)
    @Query("SELECT DATE(a.alertedAt) as date, COUNT(a) as count " +
            "FROM HeartRateAlert a " +
            "WHERE a.userId = :userId " +
            "AND a.alertedAt >= :startDate " +
            "GROUP BY DATE(a.alertedAt) " +
            "ORDER BY date")
    List<AlertCountByDate> countAlertsByDate(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate
    );

    // 사용자의 최근 경고 1개
    HeartRateAlert findFirstByUserIdOrderByAlertedAtDesc(Long userId);

    // 최근 5분 이내 경고가 있는 사용자 ID 목록
    @Query("SELECT a.userId " +
            "FROM HeartRateAlert a " +
            "WHERE a.alertedAt >= :since " +
            "GROUP BY a.userId " +
            "HAVING MAX(a.alertedAt) >= :since")
    List<Long> findUsersWithRecentAlerts(@Param("since") LocalDateTime since);

    // 특정 사용자의 활성 경고 목록 (최근 5분)
    @Query("SELECT a FROM HeartRateAlert a " +
            "WHERE a.userId = :userId " +
            "AND a.alertedAt >= :since " +
            "ORDER BY a.alertedAt DESC")
    List<HeartRateAlert> findActiveAlertsByUser(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since
    );

    // 인터페이스 프로젝션
    interface AlertCountByDate {
        java.time.LocalDate getDate();
        Long getCount();
    }
}
