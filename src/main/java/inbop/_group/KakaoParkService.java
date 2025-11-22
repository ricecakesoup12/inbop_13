package inbop._group;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Service
public class KakaoParkService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> getNearbyParks(double lat, double lon) {
        try {
            String url = "https://dapi.kakao.com/v2/local/search/keyword.json";

            URI uri = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("query", "근린공원")
                    //그냥 "공원"으로 하면 주차장도 나옴
                    .queryParam("x", lon)
                    .queryParam("y", lat)
                    .queryParam("radius", 2000)
                    .queryParam("sort", "distance")
                    .queryParam("size", 2)
                    .build()
                    .encode()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);

            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> res = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);

            List<Map<String, Object>> documents = (List<Map<String, Object>>) res.getBody().get("documents");
            List<Map<String, Object>> result = new ArrayList<>();

            if (documents != null) {
                for (Map<String, Object> doc : documents) {
                    Map<String, Object> park = new HashMap<>();
                    park.put("title", doc.get("place_name"));

                    // 도로명 주소 없으면 지번 주소 사용
                    String addr = (String) doc.get("road_address_name");
                    if (addr == null || addr.isBlank()) {
                        addr = (String) doc.get("address_name");
                    }
                    park.put("address", addr);

                    park.put("distance", doc.get("distance") + "m");
                    park.put("mapLink", doc.get("place_url"));
                    result.add(park);
                }
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}