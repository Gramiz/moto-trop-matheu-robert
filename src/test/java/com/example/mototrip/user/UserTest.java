package com.example.mototrip.user;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void addPointsShouldIncreasePoints() {
        User user = new User("Mathieu", false);

        user.addPoints(10);
        user.addPoints(5);

        assertThat(user.getPoints()).isEqualTo(15);
    }

    @Test
    void canJoinPremiumShouldReturnTrueForPremiumUser() {
        User user = new User("Rider", true);

        assertThat(user.canJoinPremium()).isTrue();
    }

    @Test
    void canJoinPremiumShouldReturnFalseForNonPremiumUser() {
        User user = new User("Rider", false);

        assertThat(user.canJoinPremium()).isFalse();
    }
}
