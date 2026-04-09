package com.example.mototrip.trip;

import com.example.mototrip.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TripTest {

    @Test
    void joinShouldAddParticipantAndAwardPoints() {
        Trip trip = new Trip("Alpes", 2, false);
        User user = new User("Mathieu", false);

        trip.join(user);

        assertThat(trip.getParticipants()).containsExactly(user);
        assertThat(trip.remainingPlaces()).isEqualTo(1);
        assertThat(user.getPoints()).isEqualTo(10);
    }

    @Test
    void remainingPlacesShouldReturnAvailableCapacity() {
        Trip trip = new Trip("Lozere", 3, false);

        trip.join(new User("One", false));

        assertThat(trip.remainingPlaces()).isEqualTo(2);
    }

    @ParameterizedTest
    @CsvSource({
            "3, 0, 3",
            "3, 1, 2",
            "3, 2, 1"
    })
    void remainingPlacesShouldMatchCurrentParticipants(int maxParticipants, int joinedUsers, int expectedRemainingPlaces) {
        Trip trip = new Trip("Bonus", maxParticipants, false);

        for (int i = 0; i < joinedUsers; i++) {
            trip.join(new User("User" + i, false));
        }

        assertThat(trip.remainingPlaces()).isEqualTo(expectedRemainingPlaces);
    }

    @Test
    void joinShouldRefuseNonPremiumUserOnPremiumTrip() {
        Trip trip = new Trip("Premium Alps", 2, true);
        User user = new User("Mathieu", false);

        assertThatThrownBy(() -> trip.join(user))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Premium required");
    }

    @Test
    void joinShouldRefuseWhenTripIsFull() {
        Trip trip = new Trip("Ardeche", 1, false);

        trip.join(new User("First", false));

        assertThatThrownBy(() -> trip.join(new User("Second", false)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trip full");
    }

    @Test
    void joinShouldRefuseWhenTripAlreadyStarted() {
        Trip trip = new Trip("Pyrenees", 2, false);
        trip.join(new User("Starter", false));
        trip.start();

        assertThatThrownBy(() -> trip.join(new User("Late", false)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Trip already started");
    }

    @Test
    void startShouldSetStartedToTrueWhenTripHasParticipants() {
        Trip trip = new Trip("Vosges", 2, false);
        trip.join(new User("Mathieu", true));

        trip.start();

        assertThat(trip.isStarted()).isTrue();
    }

    @Test
    void startShouldFailWithoutParticipants() {
        Trip trip = new Trip("Empty", 2, false);

        assertThatThrownBy(trip::start)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("No participants");
    }
}
