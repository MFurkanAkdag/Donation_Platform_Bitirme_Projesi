package com.seffafbagis.api.e2e;

import com.seffafbagis.api.dto.response.auth.AuthResponse;
import com.seffafbagis.api.dto.request.auth.LoginRequest;
import com.seffafbagis.api.dto.request.auth.RegisterRequest;
import com.seffafbagis.api.integration.BaseIntegrationTest;
import com.seffafbagis.api.enums.UserRole;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class E2EApiTest extends BaseIntegrationTest {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();
        }

        @Test
        void testNewUserJourney() {
                RegisterRequest register = new RegisterRequest();
                register.setEmail("journey@example.com");
                register.setPassword("JourneyPass1!");
                register.setConfirmPassword("JourneyPass1!");
                register.setFirstName("Journey"); // Sets on RegisterRequest but not User directly
                register.setLastName("Man");
                register.setAcceptTerms(true);
                register.setAcceptKvkk(true);
                register.setRole(UserRole.DONOR);

                ResponseEntity<String> regRes = restTemplate.postForEntity("/api/v1/auth/register", register,
                                String.class);
                assertThat(regRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                User user = userRepository.findByEmail("journey@example.com").orElseThrow();
                user.activate();
                userRepository.save(user);

                LoginRequest login = new LoginRequest();
                login.setEmail("journey@example.com");
                login.setPassword("JourneyPass1!");
                ResponseEntity<AuthResponse> loginRes = restTemplate.postForEntity("/api/v1/auth/login", login,
                                AuthResponse.class);
                assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(loginRes.getBody()).isNotNull();
                String token = loginRes.getBody().getAccessToken();

                Map<String, String> update = Map.of("firstName", "UpdatedJourney");
                restTemplate.exchange("/api/v1/users/me/profile", HttpMethod.PUT,
                                new HttpEntity<>(update, authHeaders(token)), String.class);

                Map<String, String> sensitive = Map.of("tcKimlik", "12345678901");
                restTemplate.exchange("/api/v1/users/me/sensitive-data", HttpMethod.PUT,
                                new HttpEntity<>(sensitive, authHeaders(token)), String.class);

                ResponseEntity<String> publicRes = restTemplate.getForEntity("/actuator/health", String.class);
                assertThat(publicRes.getStatusCode()).isEqualTo(HttpStatus.OK);

                User finalUser = userRepository.findById(user.getId()).orElseThrow();
                assertThat(finalUser.getProfile().getFirstName()).isEqualTo("UpdatedJourney");
        }

        @Test
        void testAdminJourney() {
                User admin = new User();
                admin.setEmail("admin@e2e.com");
                admin.setPasswordHash(passwordEncoder.encode("AdminE2E1!"));
                admin.setRole(UserRole.ADMIN);
                admin.activate();

                UserProfile adminProfile = new UserProfile();
                adminProfile.setFirstName("Admin");
                adminProfile.setLastName("E2E");
                admin.setProfile(adminProfile);

                userRepository.save(admin);

                User u1 = new User();
                u1.setEmail("u1@e2e.com");
                u1.setPasswordHash("hash");
                u1.setRole(UserRole.DONOR);
                u1.activate();
                userRepository.save(u1);

                LoginRequest login = new LoginRequest();
                login.setEmail("admin@e2e.com");
                login.setPassword("AdminE2E1!");
                ResponseEntity<AuthResponse> loginRes = restTemplate.postForEntity("/api/v1/auth/login", login,
                                AuthResponse.class);
                assertThat(loginRes.getBody()).isNotNull();
                String token = loginRes.getBody().getAccessToken();

                ResponseEntity<String> dashRes = restTemplate.exchange("/api/v1/admin/dashboard", HttpMethod.GET,
                                new HttpEntity<>(authHeaders(token)), String.class);
                assertThat(dashRes.getStatusCode()).isEqualTo(HttpStatus.OK);

                ResponseEntity<String> listRes = restTemplate.exchange("/api/v1/admin/users", HttpMethod.GET,
                                new HttpEntity<>(authHeaders(token)), String.class);
                assertThat(listRes.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(listRes.getBody()).contains("u1@e2e.com");

                Map<String, Boolean> suspend = Map.of("enabled", false);
                restTemplate.exchange("/api/v1/admin/users/" + u1.getId() + "/status", HttpMethod.PUT,
                                new HttpEntity<>(suspend, authHeaders(token)), Void.class);

                User suspended = userRepository.findById(u1.getId()).orElseThrow();
                assertThat(suspended.isActive()).isFalse();
        }
}
