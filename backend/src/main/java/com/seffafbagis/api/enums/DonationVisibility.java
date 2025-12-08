package com.seffafbagis.api.enums;

/**
 * Bağış görünürlük tercihleri.
 * 
 * Kullanıcının bağış bilgilerinin nasıl gösterileceğini belirler.
 * 
 * @author Furkan
 * @version 1.0
 */
public enum DonationVisibility {

    /**
     * Herkese açık.
     * - İsim ve miktar görünür
     * - Bağışçı listesinde görünür
     */
    PUBLIC("Herkese Açık"),

    /**
     * Anonim.
     * - İsim gizli ("Hayırsever" olarak görünür)
     * - Miktar görünür
     * - Bağışçı listesinde "Hayırsever" olarak görünür
     */
    ANONYMOUS("Anonim"),

    /**
     * Gizli.
     * - İsim gizli
     * - Miktar gizli
     * - Bağışçı listesinde görünmez
     */
    PRIVATE("Gizli");

    /**
     * Türkçe görünen ad.
     */
    private final String displayName;

    /**
     * Constructor.
     * 
     * @param displayName Türkçe görünen ad
     */
    DonationVisibility(String displayName) {
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
     * İsim görünür mü kontrol eder.
     * 
     * @return İsim görünürse true
     */
    public boolean isNameVisible() {
        return this == PUBLIC;
    }

    /**
     * Miktar görünür mü kontrol eder.
     * 
     * @return Miktar görünürse true
     */
    public boolean isAmountVisible() {
        if (this == PUBLIC) {
            return true;
        }
        if (this == ANONYMOUS) {
            return true;
        }
        return false;
    }

    /**
     * Bağışçı listesinde görünür mü kontrol eder.
     * 
     * @return Listede görünürse true
     */
    public boolean isVisibleInDonorList() {
        if (this == PUBLIC) {
            return true;
        }
        if (this == ANONYMOUS) {
            return true;
        }
        return false;
    }
}
