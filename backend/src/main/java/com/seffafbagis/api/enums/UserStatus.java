package com.seffafbagis.api.enums;

/**
 * Kullanıcı hesap durumları.
 * 
 * Kullanıcı hesabının mevcut durumunu belirtir.
 * 
 * @author Furkan
 * @version 1.0
 */
public enum UserStatus {

    /**
     * Aktif hesap.
     * - Normal kullanım
     * - Tüm özellikler erişilebilir
     */
    ACTIVE("Aktif"),

    /**
     * Pasif hesap.
     * - Kullanıcı tarafından devre dışı bırakılmış
     * - Giriş yapılamaz
     * - Yeniden aktif edilebilir
     */
    INACTIVE("Pasif"),

    /**
     * Askıya alınmış hesap.
     * - Admin tarafından askıya alınmış
     * - Kural ihlali durumunda
     * - Giriş yapılamaz
     */
    SUSPENDED("Askıya Alınmış"),

    /**
     * Doğrulama bekliyor.
     * - Yeni kayıt olmuş kullanıcı
     * - E-posta doğrulaması bekleniyor
     * - Sınırlı erişim
     */
    PENDING_VERIFICATION("Doğrulama Bekliyor");

    /**
     * Türkçe görünen ad.
     */
    private final String displayName;

    /**
     * Constructor.
     * 
     * @param displayName Türkçe görünen ad
     */
    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Türkçe görünen adı döndürür.
     * 
     * @return Görünen ad
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Kullanıcı giriş yapabilir mi kontrol eder.
     * 
     * @return Giriş yapabiliyorsa true
     */
    public boolean canLogin() {
        // Sadece ACTIVE ve PENDING_VERIFICATION giriş yapabilir
        if (this == ACTIVE) {
            return true;
        }
        if (this == PENDING_VERIFICATION) {
            return true;
        }
        return false;
    }

    /**
     * Kullanıcı bağış yapabilir mi kontrol eder.
     * 
     * @return Bağış yapabiliyorsa true
     */
    public boolean canDonate() {
        // Sadece ACTIVE kullanıcılar bağış yapabilir
        return this == ACTIVE;
    }

    /**
     * String değerden enum oluşturur.
     * 
     * @param value String değer
     * @return UserStatus enum
     * @throws IllegalArgumentException Geçersiz değer durumunda
     */
    public static UserStatus fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Status değeri boş olamaz");
        }

        String normalizedValue = value.trim().toUpperCase().replace(" ", "_");

        for (UserStatus status : UserStatus.values()) {
            if (status.name().equals(normalizedValue)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Geçersiz status değeri: " + value);
    }
}
