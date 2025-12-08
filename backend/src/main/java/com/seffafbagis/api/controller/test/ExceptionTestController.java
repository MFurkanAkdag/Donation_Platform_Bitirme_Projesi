package com.seffafbagis.api.controller.test;

import com.seffafbagis.api.dto.request.auth.LoginRequest;
import com.seffafbagis.api.dto.response.common.ApiResponse;
import com.seffafbagis.api.exception.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Test controller for Phase 3 exception handling and response DTOs.
 * 
 * This controller provides endpoints to test all exception types and verify
 * that the global exception handler and response wrappers work correctly.
 * 
 * This is a temporary controller for testing only and should be removed
 * before deploying to production.
 * 
 * @author Furkan
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/test")
public class ExceptionTestController {

    /**
     * Test ResourceNotFoundException.
     * Returns 404 with NOT_FOUND error code.
     */
    @GetMapping("/exception/not-found")
    public ResponseEntity<ApiResponse<Void>> testNotFoundException() {
        throw new ResourceNotFoundException("User", "id", "123e4567-e89b-12d3-a456-426614174000");
    }

    /**
     * Test BadRequestException.
     * Returns 400 with BAD_REQUEST error code.
     */
    @GetMapping("/exception/bad-request")
    public ResponseEntity<ApiResponse<Void>> testBadRequestException() {
        throw new BadRequestException("Invalid request data", "INVALID_DATA");
    }

    /**
     * Test UnauthorizedException.
     * Returns 401 with UNAUTHORIZED error code.
     */
    @GetMapping("/exception/unauthorized")
    public ResponseEntity<ApiResponse<Void>> testUnauthorizedException() {
        throw new UnauthorizedException("Invalid or expired token");
    }

    /**
     * Test ForbiddenException.
     * Returns 403 with FORBIDDEN error code.
     */
    @GetMapping("/exception/forbidden")
    public ResponseEntity<ApiResponse<Void>> testForbiddenException() {
        throw new ForbiddenException("Only administrators can perform this action");
    }

    /**
     * Test ConflictException.
     * Returns 409 with CONFLICT error code.
     */
    @GetMapping("/exception/conflict")
    public ResponseEntity<ApiResponse<Void>> testConflictException() {
        throw new ConflictException("User", "email", "test@example.com");
    }

    /**
     * Test FileStorageException.
     * Returns 500 with FILE_STORAGE_ERROR error code.
     */
    @GetMapping("/exception/file-storage")
    public ResponseEntity<ApiResponse<Void>> testFileStorageException() {
        throw new FileStorageException("Failed to store file: document.pdf", new RuntimeException("Disk full"));
    }

    /**
     * Test EncryptionException.
     * Returns 500 with generic error (stack trace not exposed).
     */
    @GetMapping("/exception/encryption")
    public ResponseEntity<ApiResponse<Void>> testEncryptionException() {
        throw new EncryptionException("Failed to process sensitive data", new RuntimeException("Decryption failed"));
    }

    /**
     * Test PaymentException.
     * Returns 400 or 500 depending on retryability.
     */
    @GetMapping("/exception/payment")
    public ResponseEntity<ApiResponse<Void>> testPaymentException() {
        throw new PaymentException("Card declined", "CARD_DECLINED", false);
    }

    /**
     * Test generic RuntimeException.
     * Should return 500 with generic error message (stack trace not exposed).
     */
    @GetMapping("/exception/generic")
    public ResponseEntity<ApiResponse<Void>> testGenericException() {
        throw new RuntimeException("Unexpected database error");
    }

    /**
     * Test validation error with invalid request body.
     * Returns 400 with VALIDATION_ERROR.
     */
    @PostMapping("/exception/validate")
    public ResponseEntity<ApiResponse<Void>> testValidationException(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Validation passed"));
    }

    /**
     * Test successful response with data.
     */
    @GetMapping("/success/with-data")
    public ResponseEntity<ApiResponse<String>> testSuccessWithData() {
        return ResponseEntity.ok(ApiResponse.success("Operation successful", "Test data returned"));
    }

    /**
     * Test successful response with message only.
     */
    @GetMapping("/success/message-only")
    public ResponseEntity<ApiResponse<Void>> testSuccessMessageOnly() {
        return ResponseEntity.ok(ApiResponse.successMessage("Operation completed successfully"));
    }

    /**
     * Test successful response with data only (no message).
     */
    @GetMapping("/success/data-only")
    public ResponseEntity<ApiResponse<String>> testSuccessDataOnly() {
        return ResponseEntity.ok(ApiResponse.success("Test data"));
    }

    /**
     * Test error response structure.
     * This endpoint demonstrates proper error response format.
     */
    @GetMapping("/error/format")
    public ResponseEntity<ApiResponse<Void>> testErrorFormat() {
        throw new BadRequestException("Sample error message", "SAMPLE_ERROR");
    }
}
