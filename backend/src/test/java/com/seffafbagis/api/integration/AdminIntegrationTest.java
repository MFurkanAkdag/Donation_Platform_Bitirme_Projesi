package com.seffafbagis.api.integration;

import com.seffafbagis.api.dto.response.user.UserResponse;
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

public class AdminIntegrationTest extends BaseIntegrationTest {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private String adminToken;
        private User adminUser;
        private User targetUser;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();

                adminUser = new User();
                adminUser.setEmail("admin@example.com");
                adminUser.setPasswordHash(passwordEncoder.encode("AdminPass123!"));
                adminUser.activate();
                adminUser.setRole(UserRole.ADMIN);

                UserProfile adminProfile = new UserProfile();
                adminProfile.setFirstName("Admin");
                adminProfile.setLastName("User");
                adminUser.setProfile(adminProfile);

                adminUser = userRepository.save(adminUser);

                targetUser = new User();
                targetUser.setEmail("target@example.com");
                targetUser.setPasswordHash(passwordEncoder.encode("UserPass123!"));
                targetUser.activate();
                targetUser.setRole(UserRole.DONOR);

                UserProfile targetProfile = new UserProfile();
                targetProfile.setFirstName("Target");
                targetProfile.setLastName("User");
                targetUser.setProfile(targetProfile);

                targetUser = userRepository.save(targetUser);

                adminToken = loginAndGetToken("admin@example.com", "AdminPass123!");
        }

        private String loginAndGetToken(String email, String password) {
                Map<String, String> loginRequest = Map.of("email", email, "password", password);
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.postForObject("/api/v1/auth/login", loginRequest,
                                Map.class);
                assertThat(response).isNotNull();
                return (String) response.get("accessToken");
        }

        @Test
        void testAdminUserManagement() {
                ResponseEntity<String> listRes = restTemplate.exchange("/api/v1/admin/users", HttpMethod.GET,
                                new HttpEntity<>(authHeaders(adminToken)), String.class);
                assertThat(listRes.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(listRes.getBody()).contains("totalElements");

                ResponseEntity<UserResponse> userRes = restTemplate.exchange(
                                "/api/v1/admin/users/" + targetUser.getId(), HttpMethod.GET,
                                new HttpEntity<>(authHeaders(adminToken)), UserResponse.class);
                assertThat(userRes.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(userRes.getBody().getEmail()).isEqualTo("target@example.com");

                Map<String, Boolean> statusUpdate = Map.of("enabled", false);
                ResponseEntity<Void> statusRes = restTemplate.exchange(
                                "/api/v1/admin/users/" + targetUser.getId() + "/status", HttpMethod.PUT,
                                new HttpEntity<>(statusUpdate, authHeaders(adminToken)), Void.class);
                assertThat(statusRes.getStatusCode()).isEqualTo(HttpStatus.OK);

                Map<String, String> loginRequest = Map.of("email", "target@example.com", "password", "UserPass123!");
                ResponseEntity<String> loginRes = restTemplate.postForEntity("/api/v1/auth/login", loginRequest,
                                String.class);
                assertThat(loginRes.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);

                statusUpdate = Map.of("enabled", true);
                restTemplate.exchange("/api/v1/admin/users/" + targetUser.getId() + "/status", HttpMethod.PUT,
                                new HttpEntity<>(statusUpdate, authHeaders(adminToken)), Void.class);

                loginRes = restTemplate.postForEntity("/api/v1/auth/login", loginRequest, String.class);
                assertThat(loginRes.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void testAdminCannotModifySelf() {
                Map<String, Boolean> statusUpdate = Map.of("enabled", false);
                ResponseEntity<String> res = restTemplate.exchange(
                                "/api/v1/admin/users/" + adminUser.getId() + "/status", HttpMethod.PUT,
                                new HttpEntity<>(statusUpdate, authHeaders(adminToken)), String.class);
                assertThat(res.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN);

                Map<String, String> roleUpdate = Map.of("role", "DONOR");
                res = restTemplate.exchange("/api/v1/admin/users/" + adminUser.getId() + "/role", HttpMethod.PUT,
                                new HttpEntity<>(roleUpdate, authHeaders(adminToken)), String.class);
                assertThat(res.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN);
        }

        @Test
        void testLastAdminProtection() {
                ResponseEntity<String> delRes = restTemplate.exchange("/api/v1/admin/users/" + adminUser.getId(),
                                HttpMethod.DELETE, new HttpEntity<>(authHeaders(adminToken)), String.class);
                assertThat(delRes.getStatusCode()).isIn(HttpStatus.BAD_REQUEST, HttpStatus.FORBIDDEN);
        }

        @Test
        void testNonAdminCannotAccessAdminEndpoints() {
                String userToken = loginAndGetToken("target@example.com", "UserPass123!");

                ResponseEntity<String> res = restTemplate.exchange("/api/v1/admin/users", HttpMethod.GET,
                                new HttpEntity<>(authHeaders(userToken)), String.class);
                assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

                res = restTemplate.exchange("/api/v1/admin/dashboard", HttpMethod.GET,
                                new HttpEntity<>(authHeaders(userToken)), String.class);
                assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
}
