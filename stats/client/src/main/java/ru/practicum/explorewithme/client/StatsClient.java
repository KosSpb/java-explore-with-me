package ru.practicum.explorewithme.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.explorewithme.StatsRequestDto;
import ru.practicum.explorewithme.StatsResponseDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatsClient {
    private final RestTemplate rest;

    @Autowired
    public StatsClient(@Value("${ewm-stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.rest = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                .build();
    }

    public StatsResponseDto registerEndpointHit(StatsRequestDto statsRequestDto) {
        return makeAndSendRequest(HttpMethod.POST, "/hit", null, statsRequestDto,
                new ParameterizedTypeReference<>() {});
    }

    public List<StatsResponseDto> getStats(String start, String end, List<String> uris, boolean unique) {
        String path;
        String uriRequestParam = null;

        if (uris != null) {
            StringBuilder uriRequestParamBuilder = new StringBuilder();
            for (String uri : uris) {
                uriRequestParamBuilder.append(uri).append(",");
            }
            uriRequestParamBuilder.deleteCharAt(uriRequestParamBuilder.length() - 1);
            uriRequestParam = uriRequestParamBuilder.toString();

            path = "/stats?start={start}&end={end}&uris={uris}&unique={unique}";
        } else {
            path = "/stats?start={start}&end={end}&unique={unique}";
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start);
        parameters.put("end", end);
        parameters.put("uris", uriRequestParam);
        parameters.put("unique", unique);

        return makeAndSendRequest(HttpMethod.GET, path, parameters, null, new ParameterizedTypeReference<>() {});
    }

    private <T, R> R makeAndSendRequest(HttpMethod method, String path,
                                        @Nullable Map<String, Object> parameters, @Nullable T body,
                                        ParameterizedTypeReference<R> responseType) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());

        ResponseEntity<R> serverResponse;

        try {
            if (parameters != null) {
                serverResponse = rest.exchange(path, method, requestEntity, responseType, parameters);
            } else {
                serverResponse = rest.exchange(path, method, requestEntity, responseType);
            }
        } catch (HttpStatusCodeException e) {
            String errorMessage = String.format("При выполнении запроса к сервису статистики произошла ошибка: " +
                    "код состояния %d, тело ответа: \"%s\".", e.getRawStatusCode(), e.getResponseBodyAsString());
            log.warn(errorMessage);
            throw new RestClientException(errorMessage);
        }
        return prepareGatewayResponse(serverResponse).getBody();
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private <T> ResponseEntity<T> prepareGatewayResponse(ResponseEntity<T> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        } else {
            String errorMessage = String.format("Сервис статистики вернул ответ с кодом состояния %d.",
                    response.getStatusCodeValue());
            log.warn(errorMessage);
            throw new RestClientException(errorMessage);
        }
    }
}