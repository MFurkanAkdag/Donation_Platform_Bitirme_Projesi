package com.seffafbagis.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Kaynak bulunamadı exception sınıfı.
 * 
 * Veritabanında aranan kayıt bulunamadığında fırlatılır.
 * HTTP 404 Not Found döner.
 * 
 * @author Furkan
 * @version 1.0
 */
public class ResourceNotFoundException extends BusinessException {

    /**
     * Kaynak tipi.
     * Örn: "User", "Campaign", "Organization"
     */
    private final String resourceType;

    /**
     * Kaynak tanımlayıcısı.
     * Örn: ID, email, slug
     */
    private final String identifier;

    /**
     * Temel constructor.
     * 
     * @param resourceType Kaynak tipi
     * @param identifier Kaynak tanımlayıcısı
     */
    public ResourceNotFoundException(String resourceType, String identifier) {
        super(
            resourceType + " bulunamadı: " + identifier,
            HttpStatus.NOT_FOUND,
            resourceType.toUpperCase() + "_NOT_FOUND"
        );
        this.resourceType = resourceType;
        this.identifier = identifier;
    }

    /**
     * Özel mesaj ile constructor.
     * 
     * @param resourceType Kaynak tipi
     * @param identifier Kaynak tanımlayıcısı
     * @param message Özel mesaj
     */
    public ResourceNotFoundException(String resourceType, String identifier, String message) {
        super(message, HttpStatus.NOT_FOUND, resourceType.toUpperCase() + "_NOT_FOUND");
        this.resourceType = resourceType;
        this.identifier = identifier;
    }

    /**
     * Sadece mesaj ile constructor.
     * 
     * @param message Hata mesajı
     */
    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
        this.resourceType = "Resource";
        this.identifier = "unknown";
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getIdentifier() {
        return identifier;
    }

    // ==================== FACTORY METODLAR ====================

    /**
     * Kullanıcı bulunamadı exception'ı oluşturur.
     * 
     * @param identifier Kullanıcı tanımlayıcısı (ID veya email)
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException userNotFound(String identifier) {
        return new ResourceNotFoundException("Kullanıcı", identifier);
    }

    /**
     * Kampanya bulunamadı exception'ı oluşturur.
     * 
     * @param identifier Kampanya tanımlayıcısı
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException campaignNotFound(String identifier) {
        return new ResourceNotFoundException("Kampanya", identifier);
    }

    /**
     * Organizasyon bulunamadı exception'ı oluşturur.
     * 
     * @param identifier Organizasyon tanımlayıcısı
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException organizationNotFound(String identifier) {
        return new ResourceNotFoundException("Organizasyon", identifier);
    }

    /**
     * Bağış bulunamadı exception'ı oluşturur.
     * 
     * @param identifier Bağış tanımlayıcısı
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException donationNotFound(String identifier) {
        return new ResourceNotFoundException("Bağış", identifier);
    }
}
