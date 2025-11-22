package inbop._group.path_api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Service
public class PathService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${MAP_API_KEY:}")
    private String apiKey;

    @Value("${MAP_API_ID:}")
    private String apiId;

    public PathService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        HttpClient httpClient = HttpClient.create();

        this.webClient = WebClient.builder()
                .baseUrl("https://maps.apigw.ntruss.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * 네이버 길찾기 API 호출 (리액티브)
     */
    public Mono<String> getDrivingPath(String start, String goal, String option, String waypoint) {
        log.info("Call Naver Directions: start={}, goal={}, option={}, waypoint={}", start, goal, option, waypoint);
        log.info("API_ID={}, API_KEY length={}", apiId, apiKey != null ? apiKey.length() : null);

        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/map-direction/v1/driving")
                            .queryParam("start", start)
                            .queryParam("goal", goal)
                            .queryParam("option", option);
                    
                    // waypoint가 비어있지 않을 때만 추가
                    if (waypoint != null && !waypoint.trim().isEmpty()) {
                        uriBuilder.queryParam("waypoint", waypoint);
                    }
                    
                    return uriBuilder.build();
                })
                .header("X-NCP-APIGW-API-KEY-ID", apiId)
                .header("X-NCP-APIGW-API-KEY", apiKey)
                .retrieve()
                // HTTP 에러일 때 네이버가 준 body 같이 로깅
                .onStatus(status -> status.isError(), response ->
                        response.bodyToMono(String.class)
                                .flatMap(body -> {
                                    log.error("NAVER API ERROR: status={}, body={}",
                                            response.statusCode(), body);
                                    return Mono.error(new RuntimeException("NAVER API ERROR: " + body));
                                })
                )
                .bodyToMono(String.class)
                .doOnError(ex -> {
                    if (ex instanceof WebClientResponseException wex) {
                        log.error("NAVER API HTTP ERROR: status={}, body={}",
                                wex.getStatusCode(), wex.getResponseBodyAsString(), wex);
                    } else {
                        log.error("NAVER API CALL FAILED", ex);
                    }
                });
    }

    public Mono<String> getPathOnly(String start, String goal, String option, String waypoint) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path("/map-direction/v1/driving")
                            .queryParam("start", start)
                            .queryParam("goal", goal)
                            .queryParam("option", option);
                    
                    // waypoint가 비어있지 않을 때만 추가
                    if (waypoint != null && !waypoint.trim().isEmpty()) {
                        uriBuilder.queryParam("waypoint", waypoint);
                    }
                    
                    return uriBuilder.build();
                })
                .header("X-NCP-APIGW-API-KEY-ID", apiId)
                .header("X-NCP-APIGW-API-KEY", apiKey)
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        JsonNode root = objectMapper.readTree(json);
                        JsonNode pathNode = root
                                .path("route")
                                .path("trafast")
                                .get(0)
                                .path("path");  // ← 이게 네가 보고 있는 그 배열

                        return pathNode.toString(); // [[126.84,37.54], ...] 만 내려줌
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse Naver Directions JSON", e);
                    }
                });
    }

    public Mono<String> getSamplePath() {
        String start = "126.844856,37.5407361";
        String goal  = "126.8980711,37.5763214";
        String waypoint = "126.871464,37.558529";
        String option = "trafast";
        return getDrivingPath(start, goal, option, waypoint);
    }

    public Mono<String> getSamplePath2() {
        String start = "126.844856,37.5407361";
        String goal  = "126.8980711,37.5763214";
        String waypoint = "126.871464,37.558529";
        String option = "trafast";
        return getPathOnly(start, goal, option, waypoint);
    }
}