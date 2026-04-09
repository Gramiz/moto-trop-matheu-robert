package com.example.mototrip.trip;

import com.example.mototrip.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class TripControllerRestTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        tripRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Mathieu",
                                  "premium": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Mathieu"))
                .andExpect(jsonPath("$.premium").value(true))
                .andExpect(jsonPath("$.points").value(0));
    }

    @Test
    void shouldCreateTrip() throws Exception {
        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Route des Grandes Alpes",
                                  "maxParticipants": 5,
                                  "premiumOnly": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Route des Grandes Alpes"))
                .andExpect(jsonPath("$.maxParticipants").value(5))
                .andExpect(jsonPath("$.premiumOnly").value(false))
                .andExpect(jsonPath("$.started").value(false));
    }

    @Test
    void shouldJoinAndStartTripThroughRestApi() throws Exception {
        String userResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Rider",
                                  "premium": true
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String tripResponse = mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Annecy",
                                  "maxParticipants": 2,
                                  "premiumOnly": true
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = JsonTestUtils.readLong(userResponse, "id");
        long tripId = JsonTestUtils.readLong(tripResponse, "id");

        mockMvc.perform(post("/api/trips/{id}/join", tripId).param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants[0].id").value(userId));

        mockMvc.perform(post("/api/trips/{id}/start", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.started").value(true));
    }

    @Test
    void shouldJoinTrip() throws Exception {
        String userResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Joiner",
                                  "premium": false
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String tripResponse = mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Join Only",
                                  "maxParticipants": 2,
                                  "premiumOnly": false
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = JsonTestUtils.readLong(userResponse, "id");
        long tripId = JsonTestUtils.readLong(tripResponse, "id");

        mockMvc.perform(post("/api/trips/{id}/join", tripId).param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants[0].name").value("Joiner"))
                .andExpect(jsonPath("$.participants[0].points").value(10));
    }

    @Test
    void shouldStartTrip() throws Exception {
        String userResponse = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Starter",
                                  "premium": true
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String tripResponse = mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Start Only",
                                  "maxParticipants": 2,
                                  "premiumOnly": false
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long userId = JsonTestUtils.readLong(userResponse, "id");
        long tripId = JsonTestUtils.readLong(tripResponse, "id");

        mockMvc.perform(post("/api/trips/{id}/join", tripId).param("userId", String.valueOf(userId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/trips/{id}/start", tripId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.started").value(true));
    }

    @Test
    void shouldListTrips() throws Exception {
        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Normandie",
                                  "maxParticipants": 3,
                                  "premiumOnly": false
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Normandie"));
    }

    @Test
    void shouldPropagateInvalidCapacityError() throws Exception {
        assertThatThrownBy(() -> mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Broken",
                                  "maxParticipants": 0,
                                  "premiumOnly": false
                                }
                                """)))
                .hasCauseInstanceOf(IllegalArgumentException.class)
                .rootCause()
                .hasMessage("Invalid capacity");
    }

    @Test
    void shouldPropagateUserNotFoundWhenJoiningTrip() throws Exception {
        String tripResponse = mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Missing User",
                                  "maxParticipants": 2,
                                  "premiumOnly": false
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long tripId = JsonTestUtils.readLong(tripResponse, "id");

        assertThatThrownBy(() -> mockMvc.perform(post("/api/trips/{id}/join", tripId).param("userId", "999")))
                .hasCauseInstanceOf(RuntimeException.class)
                .rootCause()
                .hasMessage("User not found");
    }

    @Test
    void shouldPropagateTripNotFoundWhenJoiningTrip() {
        assertThatThrownBy(() -> mockMvc.perform(post("/api/trips/{id}/join", 999L).param("userId", "1")))
                .hasCauseInstanceOf(RuntimeException.class)
                .rootCause()
                .hasMessage("Trip not found");
    }

    @Test
    void shouldPropagateTripNotFoundWhenStartingTrip() {
        assertThatThrownBy(() -> mockMvc.perform(post("/api/trips/{id}/start", 999L)))
                .hasCauseInstanceOf(RuntimeException.class)
                .rootCause()
                .hasMessage("Trip not found");
    }

    @Test
    void shouldPropagateNoParticipantsWhenStartingEmptyTrip() throws Exception {
        String tripResponse = mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Empty Start",
                                  "maxParticipants": 2,
                                  "premiumOnly": false
                                }
                                """))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long tripId = JsonTestUtils.readLong(tripResponse, "id");

        assertThatThrownBy(() -> mockMvc.perform(post("/api/trips/{id}/start", tripId)))
                .hasCauseInstanceOf(RuntimeException.class)
                .rootCause()
                .hasMessage("No participants");
    }
}
