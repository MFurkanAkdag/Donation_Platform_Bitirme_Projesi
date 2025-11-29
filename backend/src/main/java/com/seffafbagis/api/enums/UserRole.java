package com.seffafbagis.api.enums;

/**
 * Kullanıcı rolleri.
 * 
 * Sistemdeki kullanıcı tiplerini tanımlar.
 * Her rolün farklı yetkileri vardır.
 * 
 * @author Furkan
 * @version 1.0
 */
public enum UserRole {

    /**
     * Bağışçı.
     * - Kampanyalara bağış yapabilir
     * - Bağış geçmişini görebilir
     * - Profil yönetimi yapabilir
     */
    DONOR("Bağışçı"),

    /**
     * Vakıf/Dernek.
     * - Kampanya oluşturabilir
     * - Bağışları yönetebilir
     * - Kanıt yükleyebilir
     * - Raporları görebilir
     */
    FOUNDATION("Vakıf/Dernek"),

    /**
     * Faydalanıcı.
     * - Yardım başvurusu yapabilir
     * - Başvuru durumunu takip edebilir
     */
    BENEFICIARY("Faydalanıcı"),

    /**
     * Yönetici.
     * - Tüm yetkilere sahip
     * - Vakıf onaylama
     * - Kampanya onaylama
     * - Kullanıcı yönetimi
     * - Sistem ayarları
     */
    ADMIN("Yönetici");

    /**
     * Türkçe görünen ad.
     */
    private final String displayName;

    /**
     * Constructor.
     * 
     * @param displayName Türkçe görünen ad
     */
    UserRole(String displayName) {
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
     * String değerden enum oluşturur.
     * Case-insensitive arama yapar.
     * 
     * @param value String değer
     * @return UserRole enum
     * @throws IllegalArgumentException Geçersiz değer durumunda
     */
    public static UserRole fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Role değeri boş olamaz");
        }

        String normalizedValue = value.trim().toUpperCase();

        for (UserRole role : UserRole.values()) {
            if (role.name().equals(normalizedValue)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Geçersiz role değeri: " + value);
    }

    /**
     * Değerin geçerli bir rol olup olmadığını kontrol eder.
     * 
     * @param value Kontrol edilecek değer
     * @return Geçerli ise true
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String normalizedValue = value.trim().toUpperCase();

        for (UserRole role : UserRole.values()) {
            if (role.name().equals(normalizedValue)) {
                return true;
            }
        }

        return false;
    }
}
