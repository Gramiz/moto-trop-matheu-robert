package com.example.mototrip.trip;

import com.example.mototrip.user.User;
import com.example.mototrip.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TripService tripService;

    @Test
    void createTripShouldPersistValidTrip() {
        Trip savedTrip = new Trip("Mercantour", 3, true);
        when(tripRepository.save(any(Trip.class))).thenReturn(savedTrip);

        Trip result = tripService.createTrip("Mercantour", 3, true);

        verify(tripRepository).save(any(Trip.class));
        assertThat(result).isSameAs(savedTrip);
        assertThat(result.getName()).isEqualTo("Mercantour");
        assertThat(result.getMaxParticipants()).isEqualTo(3);
        assertThat(result.isPremiumOnly()).isTrue();
    }

    @Test
    void createTripShouldRejectInvalidCapacity() {
        assertThatThrownBy(() -> tripService.createTrip("Broken", 0, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid capacity");

        verify(tripRepository, never()).save(any(Trip.class));
    }

    @Test
    void createUserShouldPersistNewUser() {
        User savedUser = new User("Mathieu", true);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = tripService.createUser("Mathieu", true);

        verify(userRepository).save(any(User.class));
        assertThat(result).isSameAs(savedUser);
        assertThat(result.getName()).isEqualTo("Mathieu");
        assertThat(result.isPremium()).isTrue();
    }

    @Test
    void joinTripShouldLoadEntitiesApplyBusinessRulesAndSaveTrip() {
        Trip trip = new Trip("Vercors", 2, false);
        User user = new User("Mathieu", true);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(tripRepository.save(trip)).thenReturn(trip);

        Trip result = tripService.joinTrip(1L, 2L);

        assertThat(result.getParticipants()).containsExactly(user);
        assertThat(user.getPoints()).isEqualTo(10);
        verify(tripRepository).save(trip);
    }

    @Test
    void joinTripShouldFailWhenTripDoesNotExist() {
        when(tripRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.joinTrip(99L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trip not found");
    }

    @Test
    void joinTripShouldFailWhenUserDoesNotExist() {
        Trip trip = new Trip("Cevennes", 2, false);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.joinTrip(1L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found");
    }

    @Test
    void startTripShouldLoadStartAndSaveTrip() {
        Trip trip = new Trip("Jura", 2, false);
        trip.join(new User("Mathieu", false));
        when(tripRepository.findById(7L)).thenReturn(Optional.of(trip));
        when(tripRepository.save(trip)).thenReturn(trip);

        Trip result = tripService.startTrip(7L);

        assertThat(result.isStarted()).isTrue();
        verify(tripRepository).save(trip);
    }

    @Test
    void startTripShouldFailWhenTripDoesNotExist() {
        when(tripRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.startTrip(77L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trip not found");
    }

    @Test
    void allTripsShouldReturnRepositoryContent() {
        List<Trip> trips = List.of(
                new Trip("A", 1, false),
                new Trip("B", 2, true)
        );
        when(tripRepository.findAll()).thenReturn(trips);

        List<Trip> result = tripService.allTrips();

        assertThat(result).containsExactlyElementsOf(trips);
        verify(tripRepository).findAll();
    }
}
