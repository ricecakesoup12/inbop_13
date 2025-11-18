package inbop._group.path_api;

import com.inboproject.projectstudy.Service.PathService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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
            @RequestParam String waypoint,
            @RequestParam(defaultValue = "trafast") String option
    ) {
        return pathService.getDrivingPath(start, goal, option, waypoint)
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
}
