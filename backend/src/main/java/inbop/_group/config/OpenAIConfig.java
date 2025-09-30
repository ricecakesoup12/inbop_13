package inbop._group.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

//OpenAI API와 통신하기 위한 메소드
@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}") //application.properties에서 값을 읽어와 필드에 주입
    private String openAIKey;

    @Bean
    public RestTemplate openAiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // 모든 요청에 공통 헤더 추가하는 인터셉터
        ClientHttpRequestInterceptor authInterceptor = (request, body, execution) -> {
            request.getHeaders().setBearerAuth(openAIKey); // Authorization: Bearer {key}
            request.getHeaders().set("Content-Type", "application/json");
            request.getHeaders().set("Accept", "application/json");
            return execution.execute(request, body);
        };

        restTemplate.getInterceptors().add(authInterceptor);
        return restTemplate;
    }
}
