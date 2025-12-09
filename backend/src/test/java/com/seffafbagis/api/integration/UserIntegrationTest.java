package com.seffafbagis.api.integration;

import com.seffafbagis.api.dto.request.user.UpdateProfileRequest;
import com.seffafbagis.api.dto.response.user.UserProfileResponse;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.repository.AuditLogRepository;
import com.seffafbagis.api.enums.UserRole;
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

public class UserIntegrationTest extends BaseIntegrationTest {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private AuditLogRepository auditLogRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private String userToken;
        private User testUser;

        @BeforeEach
        void setUp() {
                auditLogRepository.deleteAll();
                userRepository.deleteAll();

                testUser = new User();
                testUser.setEmail("user@example.com");
                testUser.setPasswordHash(passwordEncoder.encode("Password123!"));
                testUser.activate(); // Set active
                testUser.setRole(UserRole.DONOR);

                UserProfile profile = new UserProfile();
                profile.setFirstName("John");
                profile.setLastName("Doe");
                testUser.setProfile(profile);

                testUser = userRepository.save(testUser);

                userToken = loginAndGetToken("user@example.com", "Password123!");
        }

        private String loginAndGetToken(String email, String password) {
                Map<String, String> loginRequest = Map.of(
                                "email", email,
                                "password", password);
                @SuppressWarnings("unchecked")
                Map<String, Object> response = restTemplate.postForObject("/api/v1/auth/login", loginRequest,
                                Map.class);
                assertThat(response).isNotNull();
                return (String) response.get("accessToken");
        }

        @Test
        void testProfileManagement() {
                HttpEntity<?> getRequest = new HttpEntity<>(authHeaders(userToken));
                ResponseEntity<UserProfileResponse> profileRes = restTemplate.exchange("/api/v1/users/me/profile",
                                HttpMethod.GET, getRequest, UserProfileResponse.class);

                assertThat(profileRes.getStatusCode()).isEqualTo(HttpStatus.OK);
                // Email is on User entity, not UserProfileResponse
                assertThat(profileRes.getBody().getFirstName()).isEqualTo("John");

                UpdateProfileRequest updateReq = new UpdateProfileRequest();
                updateReq.setFirstName("Jane");
                updateReq.setLastName("Smith");

                HttpEntity<UpdateProfileRequest> putRequest = new HttpEntity<>(updateReq, authHeaders(userToken));
                ResponseEntity<UserProfileResponse> updateRes = restTemplate.exchange("/api/v1/users/me/profile",
                                HttpMethod.PUT, putRequest, UserProfileResponse.class);

                assertThat(updateRes.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(updateRes.getBody().getFirstName()).isEqualTo("Jane");
                assertThat(updateRes.getBody().getLastName()).isEqualTo("Smith");

                User updatedUser = userRepository.findByEmail("user@example.com").orElseThrow();
                assertThat(updatedUser.getProfile().getFirstName()).isEqualTo("Jane");
        }

        @Test
        void testSensitiveDataWithEncryption() {
                Map<String, String> request = Map.of("tcKimlik", "11122233344");
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, authHeaders(userToken));

                ResponseEntity<String> response = restTemplate.exchange("/api/v1/users/me/sensitive-data",
                                HttpMethod.PUT, entity, String.class);
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

                ResponseEntity<Map> getResponse = restTemplate.exchange("/api/v1/users/me/sensitive-data",
                                HttpMethod.GET, new HttpEntity<>(authHeaders(userToken)), Map.class);
                assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                String masked = (String) getResponse.getBody().get("tcKimlik");
                assertThat(masked).contains("*******");

                ResponseEntity<Void> deleteRes = restTemplate.exchange("/api/v1/users/me/sensitive-data",
                                HttpMethod.DELETE, new HttpEntity<>(authHeaders(userToken)), Void.class);
                assertThat(deleteRes.getStatusCode()).isEqualTo(HttpStatus.OK);

                User finalUser = userRepository.findById(testUser.getId()).orElseThrow();
                assertThat(finalUser.getSensitiveData() == null
                                || finalUser.getSensitiveData().getTcKimlikEncrypted() == null)
                                .isTrue();
        }

        @Test
        void testKvkkDataExport() {
                Map<String, String> request = Map.of("tcKimlik", "11122233344");
                restTemplate.exchange("/api/v1/users/me/sensitive-data", HttpMethod.PUT,
                                new HttpEntity<>(request, authHeaders(userToken)), String.class);

                ResponseEntity<String> exportRes = restTemplate.exchange("/api/v1/users/me/sensitive-data/export",
                                HttpMethod.GET, new HttpEntity<>(authHeaders(userToken)), String.class);
                assertThat(exportRes.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(exportRes.getBody()).contains("11122233344");

                assertThat(auditLogRepository.count()).isGreaterThan(0);
        }

        @Test
        void testAccountDeletion() {
                Map<String, String> deleteReq = Map.of("password", "Password123!");
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(deleteReq, authHeaders(userToken));
                ResponseEntity<Void> deleteRes = restTemplate.exchange("/api/v1/users/me", HttpMethod.DELETE, entity,
                                Void.class);

                assertThat(deleteRes.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.NO_CONTENT);
        }
}
