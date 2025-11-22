package inbop._group.path_api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/path")
@RequiredArgsConstructor
public class PathController {

    private final PathService pathService;

    @GetMapping("/driving")
    public Mono<ResponseEntity<String>> getDrivingPath(
            @RequestParam String start,
            @RequestParam String goal,
            @RequestParam(required = false, defaultValue = "") String waypoint,
            @RequestParam(defaultValue = "trafast") String option
    ) {
        return pathService.getDrivingPath(start, goal, option, waypoint)
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // 네이버 API 에러 응답을 그대로 전달
                    String errorBody = ex.getResponseBodyAsString();
                    System.err.println("❌ 네이버 API 에러 응답: " + errorBody);
                    return Mono.just(ResponseEntity
                            .status(ex.getStatusCode())
                            .body(errorBody != null ? errorBody : "NAVER API ERROR: " + ex.getMessage()));
                })
                .onErrorResume(ex -> {
                    // 기타 에러
                    System.err.println("❌ 경로 API 에러: " + ex.getMessage());
                    ex.printStackTrace();
                    return Mono.just(ResponseEntity
                            .status(500)
                            .body("NAVER API call failed: " + ex.getMessage()));
                });
    }

    @GetMapping("/test")
    public Mono<ResponseEntity<String>> getSamplePath() {
        return pathService.getSamplePath()
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex ->
                        Mono.just(ResponseEntity
                                .status(ex.getStatusCode())
                                .body(ex.getResponseBodyAsString()))
                )
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity
                                .internalServerError()
                                .body("NAVER API call failed: " + ex.getMessage()))
                );
    }

    @GetMapping("/test2")
    public Mono<ResponseEntity<String>> getSamplePath2() {
        return pathService.getSamplePath2()
                .map(ResponseEntity::ok)
                .onErrorResume(WebClientResponseException.class, ex ->
                        Mono.just(ResponseEntity
                                .status(ex.getStatusCode())
                                .body(ex.getResponseBodyAsString()))
                )
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity
                                .internalServerError()
                                .body("NAVER API call failed: " + ex.getMessage()))
                );
    }
}