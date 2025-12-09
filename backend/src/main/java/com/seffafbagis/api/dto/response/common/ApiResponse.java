package com.seffafbagis.api.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Standart API response wrapper sınıfı.
 * 
 * Tüm API endpoint'leri bu formatta response döner.
 * Bu sayede frontend tutarlı bir yapı bekleyebilir.
 * 
 * Başarılı response örneği:
 * {
 * "success": true,
 * "message": "İşlem başarılı",
 * "data": { ... },
 * "timestamp": "2024-01-15T10:30:00Z"
 * }
 * 
 * Hata response örneği:
 * {
 * "success": false,
 * "message": "Kullanıcı bulunamadı",
 * "errorCode": "USER_NOT_FOUND",
 * "timestamp": "2024-01-15T10:30:00Z"
 * }
 * 
 * @param <T> Response data tipi
 * @author Furkan
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * İşlem başarılı mı?
     */
    private boolean success;

    /**
     * Kullanıcıya gösterilecek mesaj.
     */
    private String message;

    /**
     * Response data.
     * Başarılı işlemlerde döndürülür.
     */
    private T data;

    /**
     * Hata kodu.
     * Hata durumlarında döndürülür.
     * Frontend'de hata yönetimi için kullanılır.
     */
    private String errorCode;

    /**
     * Response timestamp.
     * ISO-8601 formatında.
     */
    private Instant timestamp;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     */
    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    /**
     * Temel constructor.
     * 
     * @param success Başarılı mı
     * @param message Mesaj
     */
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = Instant.now();
    }

    /**
     * Data ile constructor.
     * 
     * @param success Başarılı mı
     * @param message Mesaj
     * @param data    Response data
     */
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    // ==================== FACTORY METODLAR ====================

    /**
     * Başarılı response oluşturur (sadece mesaj).
     * 
     * @param message Başarı mesajı
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message);
    }

    /**
     * Başarılı response oluşturur (data ile).
     * 
     * @param message Başarı mesajı
     * @param data    Response data
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    /**
     * Başarılı response oluşturur (sadece data).
     * 
     * @param data Response data
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "İşlem başarılı", data);
    }

    /**
     * Hata response oluşturur.
     * 
     * @param message Hata mesajı
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>(false, message);
        return response;
    }

    /**
     * Hata response oluşturur (hata kodu ile).
     * 
     * @param message   Hata mesajı
     * @param errorCode Hata kodu
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        ApiResponse<T> response = new ApiResponse<>(false, message);
        response.setErrorCode(errorCode);
        return response;
    }

    // ==================== GETTER METODLARI ====================

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    // ==================== SETTER METODLARI ====================

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
