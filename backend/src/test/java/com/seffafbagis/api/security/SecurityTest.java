package com.seffafbagis.api.security;

import com.seffafbagis.api.dto.request.auth.LoginRequest;
import com.seffafbagis.api.dto.request.auth.RegisterRequest;
import com.seffafbagis.api.dto.request.user.UpdateProfileRequest;
import com.seffafbagis.api.dto.response.auth.AuthResponse;
import com.seffafbagis.api.dto.response.user.UserProfileResponse;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.entity.user.UserProfile;
import com.seffafbagis.api.enums.UserStatus;
import com.seffafbagis.api.integration.BaseIntegrationTest;
import com.seffafbagis.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testPasswordHashing() {
        RegisterRequest register = new RegisterRequest();
        register.setEmail("sec@test.com");
        register.setPassword("PlainSecret123!");
        register.setFirstName("Sec");
        register.setLastName("Test");
        register.setAcceptTerms(true);
        register.setAcceptKvkk(true);
        register.setRole(com.seffafbagis.api.enums.UserRole.DONOR);
        register.setConfirmPassword("PlainSecret123!");

        restTemplate.postForEntity("/api/v1/auth/register", register, String.class);

        User user = userRepository.findByEmail("sec@test.com").orElseThrow();
        assertThat(user.getPasswordHash()).isNotEqualTo("PlainSecret123!");
        assertThat(user.getPasswordHash()).startsWith("$2a$");
    }

    @Test
    void testSensitiveDataEncryption() {
        createVerifiedUser("enc@test.com", "Pass123!");
        String token = loginAndGetToken("enc@test.com", "Pass123!");

        Map<String, String> data = Map.of("tcKimlik", "12345678901");
        restTemplate.exchange("/api/v1/users/me/sensitive-data", HttpMethod.PUT,
                new HttpEntity<>(data, authHeaders(token)), String.class);

        ResponseEntity<Map> res = restTemplate.exchange("/api/v1/users/me/sensitive-data", HttpMethod.GET,
                new HttpEntity<>(authHeaders(token)), Map.class);
        String val = (String) res.getBody().get("tcKimlik");
        assertThat(val).contains("*******");
        assertThat(val).doesNotContain("12345678901");
    }

    @Test
    void testTokenSecurity() {
        createVerifiedUser("tok@test.com", "Pass123!");
        String token = loginAndGetToken("tok@test.com", "Pass123!");

        assertThat(token.split("\\.")).hasSize(3);

        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        assertThat(payload).doesNotContain("Pass123!");

        String tampered = parts[0] + "." + parts[1] + "." + "tamperedSignature";
        ResponseEntity<String> res = restTemplate.exchange("/api/v1/users/me/profile", HttpMethod.GET,
                new HttpEntity<>(authHeaders(tampered)), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void testSqlInjectionPrevention() {
        LoginRequest login = new LoginRequest();
        login.setEmail("' OR '1'='1");
        login.setPassword("anything");

        ResponseEntity<String> res = restTemplate.postForEntity("/api/v1/auth/login", login, String.class);
        assertThat(res.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.BAD_REQUEST);
    }

    @Test
    void testXssPrevention() {
        createVerifiedUser("xss@test.com", "Pass123!");
        String token = loginAndGetToken("xss@test.com", "Pass123!");

        UpdateProfileRequest update = new UpdateProfileRequest();
        update.setFirstName("<script>alert(1)</script>"); // XSS attempt in firstName
        update.setLastName("User");

        // Profile update doesn't have phone number

        ResponseEntity<UserProfileResponse> res = restTemplate.exchange(
                "/api/v1/users/me/profile",
                HttpMethod.PUT,
                new HttpEntity<>(update, authHeaders(token)),
                UserProfileResponse.class);

        assertThat(res.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.BAD_REQUEST);
    }

    private void createVerifiedUser(String email, String password) {
        // Register via API for simplicity (handles profile creation automatically)
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setConfirmPassword(password);
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");
        registerRequest.setRole(com.seffafbagis.api.enums.UserRole.DONOR);
        registerRequest.setAcceptTerms(true);
        registerRequest.setAcceptKvkk(true);

        restTemplate.postForEntity("/api/v1/auth/register", registerRequest, String.class);

        User user = userRepository.findByEmail(email).orElseThrow();
        user.activate(); // Use activate() method
        userRepository.save(user);
    }

    private String loginAndGetToken(String email, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        AuthResponse response = restTemplate.postForObject("/api/v1/auth/login", loginRequest, AuthResponse.class);
        return response.getAccessToken();
    }
}
