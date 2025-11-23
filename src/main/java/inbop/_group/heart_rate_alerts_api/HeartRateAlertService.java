package inbop._group.heart_rate_alerts_api;


import inbop._group.User;
import inbop._group.UserRepository;
import inbop._group.heart_rate_alerts_api.Dto.ActiveAlertResponse;
import inbop._group.heart_rate_alerts_api.Dto.CreateHeartRateAlertRequest;
import inbop._group.heart_rate_alerts_api.Dto.HeartRateAlertResponse;
import inbop._group.heart_rate_alerts_api.Dto.WeeklyAlertCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HeartRateAlertService {

    private final HeartRateAlertRepository alertRepository;
    private final UserRepository userRepository;

    private static final int ALERT_THRESHOLD = 130;
    private static final long DUPLICATE_MINUTES = 1;
    private static final long ACTIVE_MINUTES = 5;

    // 1. 경고 생성
    public HeartRateAlertResponse createAlert(CreateHeartRateAlertRequest request) {

        // 1-1. 심박수 기준 체크 (130 이하일 땐 예외)
        if (request.getHeartRate() <= ALERT_THRESHOLD) {
            throw new IllegalArgumentException("심박수가 경고 기준(> " + ALERT_THRESHOLD + " bpm)을 넘지 않습니다.");
        }

        // 1-2. 사용자 존재 여부 확인
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NoSuchElementException("해당 userId 사용자를 찾을 수 없습니다. id=" + request.getUserId()));

        // 1-3. 마지막 경고 가져와서 1분 중복 체크
        HeartRateAlert lastAlert = alertRepository.findFirstByUserIdOrderByAlertedAtDesc(user.getId());

        LocalDateTime now = LocalDateTime.now();
        if (lastAlert != null) {
            LocalDateTime oneMinuteAgo = now.minusMinutes(DUPLICATE_MINUTES);
            if (lastAlert.getAlertedAt().isAfter(oneMinuteAgo)) {
                // 1분 이내였으면 새로 저장하지 않고 기존 경고 그대로 응답
                return toResponse(lastAlert);
            }
        }

        // 1-4. 새로운 경고 저장
        HeartRateAlert alert = new HeartRateAlert();
        alert.setUserId(user.getId());
        alert.setHeartRate(request.getHeartRate());
        alert.setAlertType("warning");
        alert.setAlertedAt(now);

        HeartRateAlert saved = alertRepository.save(alert);

        // TODO: 여기서 WebSocket/SSE 등으로 실시간 알림 전송 가능

        return toResponse(saved);
    }

    // 2. 사용자별 일주일 경고 횟수
    @Transactional(readOnly = true)
    public List<WeeklyAlertCountResponse> getWeeklyCounts(Long userId) {

        // 사용자 존재 체크 (없으면 404용 예외)
        if (!userRepository.existsById(userId)) {
            throw new NoSuchElementException("해당 userId 사용자를 찾을 수 없습니다. id=" + userId);
        }

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6); // 오늘 포함 7일

        LocalDateTime startDateTime = weekAgo.atStartOfDay();

        List<HeartRateAlertRepository.AlertCountByDate> counts =
                alertRepository.countAlertsByDate(userId, startDateTime);

        Map<LocalDate, Long> countMap = counts.stream()
                .collect(Collectors.toMap(
                        HeartRateAlertRepository.AlertCountByDate::getDate,
                        HeartRateAlertRepository.AlertCountByDate::getCount
                ));

        List<WeeklyAlertCountResponse> result = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekAgo.plusDays(i);
            Long count = countMap.getOrDefault(date, 0L);
            result.add(new WeeklyAlertCountResponse(
                    date.toString(), // "YYYY-MM-DD"
                    count
            ));
        }

        // 과거 → 최근 순 (이미 그런 순서지만 명시적으로 정렬도 가능)
        result.sort(Comparator.comparing(WeeklyAlertCountResponse::getDate));
        return result;
    }

    // 3. 활성 경고 조회 (보호자용)
    @Transactional(readOnly = true)
    public List<ActiveAlertResponse> getActiveAlerts() {

        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(ACTIVE_MINUTES);

        // 1) 최근 5분 이내 경고가 있는 사용자 ID들
        List<Long> userIds = alertRepository.findUsersWithRecentAlerts(fiveMinutesAgo);

        // 2) 각 사용자의 최신 경고 + User 정보 조합
        List<ActiveAlertResponse> list = userIds.stream()
                .map(userId -> {
                    HeartRateAlert latestAlert =
                            alertRepository.findFirstByUserIdOrderByAlertedAtDesc(userId);

                    if (latestAlert == null || latestAlert.getAlertedAt().isBefore(fiveMinutesAgo)) {
                        // 방어 코드: 혹시라도 5분 이전이면 스킵
                        return null;
                    }

                    User user = userRepository.findById(userId).orElse(null);
                    String userName = (user != null ? user.getName() : "알 수 없음");

                    return ActiveAlertResponse.builder()
                            .userId(userId)
                            .userName(userName)
                            .heartRate(latestAlert.getHeartRate())
                            .alertedAt(latestAlert.getAlertedAt())
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted((a, b) -> b.getHeartRate().compareTo(a.getHeartRate())) // 심박수 내림차순
                .collect(Collectors.toList());

        return list;
    }

    // 엔티티 → 응답 DTO 변환
    private HeartRateAlertResponse toResponse(HeartRateAlert alert) {
        return HeartRateAlertResponse.builder()
                .id(alert.getId())
                .userId(alert.getUserId())
                .heartRate(alert.getHeartRate())
                .alertType(alert.getAlertType())
                .alertedAt(alert.getAlertedAt())
                .build();
    }
}
