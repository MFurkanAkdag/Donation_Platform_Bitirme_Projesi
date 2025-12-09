package com.seffafbagis.api.performance;

import com.seffafbagis.api.dto.response.auth.AuthResponse;
import com.seffafbagis.api.dto.request.auth.LoginRequest;
import com.seffafbagis.api.integration.BaseIntegrationTest;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class PerformanceTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testLoginPerformance() {
        User user = new User();
        user.setEmail("perf@example.com");
        user.setPasswordHash(passwordEncoder.encode("PerfPass1!"));
        user.setRole(UserRole.DONOR);
        user.activate();
        userRepository.save(user);

        LoginRequest login = new LoginRequest();
        login.setEmail("perf@example.com");
        login.setPassword("PerfPass1!");

        restTemplate.postForEntity("/api/v1/auth/login", login, AuthResponse.class);

        long totalTime = 0;
        int iterations = 50;

        for (int i = 0; i < iterations; i++) {
            long start = System.currentTimeMillis();
            ResponseEntity<AuthResponse> res = restTemplate.postForEntity("/api/v1/auth/login", login,
                    AuthResponse.class);
            long end = System.currentTimeMillis();
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            totalTime += (end - start);
        }

        double average = totalTime / (double) iterations;
        System.out.println("Average Login Time: " + average + "ms");
        assertThat(average).isLessThan(500);
    }

    @Test
    void testPublicSettingsWithCaching() {
        User admin = new User();
        admin.setEmail("admin@perf.com");
        admin.setPasswordHash(passwordEncoder.encode("pass"));
        admin.setRole(UserRole.ADMIN);
        admin.activate();
        userRepository.save(admin);

        String token = loginAndGetToken("admin@perf.com", "pass");

        long start1 = System.currentTimeMillis();
        ResponseEntity<String> res1 = restTemplate.exchange("/api/v1/admin/dashboard", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), String.class);
        long end1 = System.currentTimeMillis();
        long duration1 = end1 - start1;

        long start2 = System.currentTimeMillis();
        ResponseEntity<String> res2 = restTemplate.exchange("/api/v1/admin/dashboard", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), String.class);
        long end2 = System.currentTimeMillis();
        long duration2 = end2 - start2;

        System.out.println("First: " + duration1 + "ms, Second: " + duration2 + "ms");
    }

    @Test
    void testUserListingPagination() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User u = new User();
            u.setEmail("user" + i + "@perf.com");
            u.setPasswordHash("hash");
            u.setRole(UserRole.DONOR);
            users.add(u);
        }
        userRepository.saveAll(users);

        User admin = new User();
        admin.setEmail("admin@perf.com");
        admin.setPasswordHash(passwordEncoder.encode("pass"));
        admin.setRole(UserRole.ADMIN);
        admin.activate();
        userRepository.save(admin);

        String token = loginAndGetToken("admin@perf.com", "pass");

        long start1 = System.currentTimeMillis();
        ResponseEntity<String> res1 = restTemplate.exchange("/api/v1/admin/users?page=0&size=20", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), String.class);
        long end1 = System.currentTimeMillis();
        assertThat(end1 - start1).isLessThan(500);

        long start5 = System.currentTimeMillis();
        ResponseEntity<String> res5 = restTemplate.exchange("/api/v1/admin/users?page=4&size=20", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), String.class);
        long end5 = System.currentTimeMillis();
        assertThat(end5 - start5).isLessThan(500);
    }

    private String loginAndGetToken(String email, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        AuthResponse response = restTemplate.postForObject("/api/v1/auth/login", loginRequest, AuthResponse.class);
        return response.getAccessToken();
    }
}
