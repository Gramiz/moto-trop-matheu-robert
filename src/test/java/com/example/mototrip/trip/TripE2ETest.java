package com.example.mototrip.trip;

import com.example.mototrip.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TripE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        tripRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createUserThenCreateTripThenJoinThenStartThenVerifyState() {
        String baseUrl = "http://localhost:" + port + "/api";
        RestClient restClient = RestClient.builder().baseUrl(baseUrl).build();

        Map<String, Object> userResponse = restClient.method(HttpMethod.POST)
                .uri("/users")
                .body(Map.of("name", "Mathieu", "premium", true))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        Map<String, Object> tripResponse = restClient.method(HttpMethod.POST)
                .uri("/trips")
                .body(Map.of("name", "Alps Ride", "maxParticipants", 2, "premiumOnly", true))
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        Long userId = ((Number) userResponse.get("id")).longValue();
        Long tripId = ((Number) tripResponse.get("id")).longValue();

        Map<String, Object> joinResponse = restClient.method(HttpMethod.POST)
                .uri("/trips/{id}/join?userId={userId}", tripId, userId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        Map<String, Object> startResponse = restClient.method(HttpMethod.POST)
                .uri("/trips/{id}/start", tripId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        List<Map<String, Object>> listResponse = restClient.method(HttpMethod.GET)
                .uri("/trips")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        assertThat(userResponse.get("id")).isNotNull();
        assertThat(tripResponse.get("id")).isNotNull();
        assertThat(((List<?>) joinResponse.get("participants"))).hasSize(1);
        assertThat(startResponse.get("started")).isEqualTo(true);
        assertThat(listResponse).hasSize(1);
        assertThat(listResponse.getFirst().get("started")).isEqualTo(true);
        assertThat(((List<?>) listResponse.getFirst().get("participants"))).hasSize(1);
    }
}
