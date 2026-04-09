package com.example.mototrip.trip;

import com.example.mototrip.user.User;
import com.example.mototrip.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class TripRepositoryIntegrationTest {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistTrip() {
        Trip savedTrip = tripRepository.save(new Trip("Bretagne", 4, false));

        assertThat(savedTrip.getId()).isNotNull();
        assertThat(tripRepository.findById(savedTrip.getId()))
                .get()
                .extracting(Trip::getName, Trip::getMaxParticipants, Trip::isPremiumOnly)
                .containsExactly("Bretagne", 4, false);
    }

    @Test
    void shouldPersistTripParticipantsRelation() {
        User savedUser = userRepository.save(new User("Mathieu", true));
        Trip trip = new Trip("Corsica", 3, true);
        trip.join(savedUser);
        Trip savedTrip = tripRepository.saveAndFlush(trip);

        Trip reloadedTrip = tripRepository.findById(savedTrip.getId()).orElseThrow();

        assertThat(reloadedTrip.getParticipants()).hasSize(1);
        assertThat(reloadedTrip.getParticipants().getFirst().getId()).isEqualTo(savedUser.getId());
        assertThat(reloadedTrip.getParticipants().getFirst().getName()).isEqualTo("Mathieu");
    }
}
