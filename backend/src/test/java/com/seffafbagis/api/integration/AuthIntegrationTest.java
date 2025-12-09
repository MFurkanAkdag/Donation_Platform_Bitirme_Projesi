package com.seffafbagis.api.integration;

import com.seffafbagis.api.dto.request.auth.LoginRequest;
import com.seffafbagis.api.dto.request.auth.RegisterRequest;
import com.seffafbagis.api.dto.response.auth.AuthResponse;
import com.seffafbagis.api.dto.request.auth.ResetPasswordRequest;
import com.seffafbagis.api.dto.request.auth.RefreshTokenRequest;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.repository.UserRepository;
import com.seffafbagis.api.repository.EmailVerificationTokenRepository;
import com.seffafbagis.api.repository.PasswordResetTokenRepository;
import com.seffafbagis.api.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class AuthIntegrationTest extends BaseIntegrationTest {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private EmailVerificationTokenRepository emailVerificationTokenRepository;

        @Autowired
        private PasswordResetTokenRepository passwordResetTokenRepository;

        @BeforeEach
        void setUp() {
                passwordResetTokenRepository.deleteAll();
                emailVerificationTokenRepository.deleteAll();
                userRepository.deleteAll();
        }

        @Test
        void testCompleteRegistrationFlow() {
                RegisterRequest registerRequest = new RegisterRequest();
                registerRequest.setEmail("test@example.com");
                registerRequest.setPassword("Password123!");
                registerRequest.setConfirmPassword("Password123!");
                registerRequest.setFirstName("Test");
                registerRequest.setLastName("User");
                registerRequest.setRole(UserRole.DONOR);
                registerRequest.setAcceptTerms(true);
                registerRequest.setAcceptKvkk(true);

                ResponseEntity<String> registerResponse = restTemplate.postForEntity(
                                "/api/v1/auth/register",
                                registerRequest,
                                String.class);
                assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

                Optional<User> userOpt = userRepository.findByEmail("test@example.com");
                assertThat(userOpt).isPresent();
                User user = userOpt.get();
                assertFalse(user.isActive()); // Assuming isActive checks ACTIVE status vs PENDING

                var tokens = emailVerificationTokenRepository.findAll();
                assertThat(tokens).hasSize(1);
                String token = tokens.get(0).getToken();

                ResponseEntity<String> verifyResponse = restTemplate.getForEntity(
                                "/api/v1/auth/verify-email?token=" + token,
                                String.class);
                assertThat(verifyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                user = userRepository.findById(user.getId()).orElseThrow();
                assertTrue(user.isActive()); // Should be active now

                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("test@example.com");
                loginRequest.setPassword("Password123!");

                ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                                "/api/v1/auth/login",
                                loginRequest,
                                AuthResponse.class);
                assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(loginResponse.getBody()).isNotNull();
                assertThat(loginResponse.getBody().getAccessToken()).isNotNull();

                HttpEntity<?> entity = new HttpEntity<>(authHeaders(loginResponse.getBody().getAccessToken()));
                ResponseEntity<String> meResponse = restTemplate.exchange(
                                "/api/v1/users/me/profile",
                                HttpMethod.GET,
                                entity,
                                String.class);
                assertThat(meResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void testLoginWithAccountLockout() {
                createVerifiedUser("lockout@example.com", "Password123!");

                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setEmail("lockout@example.com");
                loginRequest.setPassword("WrongPass");

                for (int i = 0; i < 5; i++) {
                        ResponseEntity<String> response = restTemplate.postForEntity(
                                        "/api/v1/auth/login",
                                        loginRequest,
                                        String.class);
                        assertThat(response.getStatusCode().isError()).isTrue();
                }

                loginRequest.setPassword("Password123!");
                ResponseEntity<String> lockedResponse = restTemplate.postForEntity(
                                "/api/v1/auth/login",
                                loginRequest,
                                String.class);
                assertThat(lockedResponse.getStatusCode().isError()).isTrue();
        }

        @Test
        void testPasswordResetFlow() {
                createVerifiedUser("reset@example.com", "OldPass123!");

                String forgotBody = "{\"email\": \"reset@example.com\"}";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> request = new HttpEntity<>(forgotBody, headers);

                ResponseEntity<String> forgotResponse = restTemplate.postForEntity(
                                "/api/v1/auth/forgot-password",
                                request,
                                String.class);
                assertThat(forgotResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                var tokens = passwordResetTokenRepository.findAll();
                assertThat(tokens).hasSize(1);
                String resetToken = tokens.get(0).getTokenHash();

                ResetPasswordRequest resetRequest = new ResetPasswordRequest();
                resetRequest.setToken(resetToken);
                resetRequest.setNewPassword("NewPass123!");
                resetRequest.setConfirmPassword("NewPass123!");

                ResponseEntity<String> resetResponse = restTemplate.postForEntity(
                                "/api/v1/auth/reset-password",
                                resetRequest,
                                String.class);
                assertThat(resetResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

                LoginRequest loginOld = new LoginRequest();
                loginOld.setEmail("reset@example.com");
                loginOld.setPassword("OldPass123!");
                ResponseEntity<String> oldLogin = restTemplate.postForEntity("/api/v1/auth/login", loginOld,
                                String.class);
                assertThat(oldLogin.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

                LoginRequest loginNew = new LoginRequest();
                loginNew.setEmail("reset@example.com");
                loginNew.setPassword("NewPass123!");
                ResponseEntity<AuthResponse> newLogin = restTemplate.postForEntity("/api/v1/auth/login", loginNew,
                                AuthResponse.class);
                assertThat(newLogin.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void testTokenRefreshFlow() {
                createVerifiedUser("refresh@example.com", "Pass123!");

                LoginRequest login = new LoginRequest();
                login.setEmail("refresh@example.com");
                login.setPassword("Pass123!");
                ResponseEntity<AuthResponse> loginRes = restTemplate.postForEntity("/api/v1/auth/login", login,
                                AuthResponse.class);
                assertThat(loginRes.getBody()).isNotNull();
                String refreshToken = loginRes.getBody().getRefreshToken();

                RefreshTokenRequest refreshReq = new RefreshTokenRequest();
                refreshReq.setRefreshToken(refreshToken);

                ResponseEntity<AuthResponse> refreshRes = restTemplate.postForEntity(
                                "/api/v1/auth/refresh",
                                refreshReq,
                                AuthResponse.class);

                assertThat(refreshRes.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(refreshRes.getBody()).isNotNull();
                assertThat(refreshRes.getBody().getAccessToken()).isNotNull();
                assertThat(refreshRes.getBody().getAccessToken()).isNotEqualTo(loginRes.getBody().getAccessToken());
        }

        private void createVerifiedUser(String email, String password) {
                RegisterRequest registerRequest = new RegisterRequest();
                registerRequest.setEmail(email);
                registerRequest.setPassword(password);
                registerRequest.setConfirmPassword(password);
                registerRequest.setFirstName("Test");
                registerRequest.setLastName("User");
                registerRequest.setRole(UserRole.DONOR);
                registerRequest.setAcceptTerms(true);
                registerRequest.setAcceptKvkk(true);

                restTemplate.postForEntity("/api/v1/auth/register", registerRequest, String.class);

                User user = userRepository.findByEmail(email).orElseThrow();
                user.activate();
                userRepository.save(user);
        }
}
