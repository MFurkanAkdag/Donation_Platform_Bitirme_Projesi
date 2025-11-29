package com.seffafbagis.api.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Tüm entity'lerin temel sınıfı.
 * 
 * Bu sınıf şu ortak alanları sağlar:
 * - id: Benzersiz tanımlayıcı (UUID)
 * - createdAt: Oluşturulma tarihi
 * - updatedAt: Son güncelleme tarihi
 * 
 * @MappedSuperclass: Bu sınıf için ayrı tablo oluşturulmaz,
 * alanları alt sınıflara miras kalır.
 * 
 * @author Furkan
 * @version 1.0
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Benzersiz tanımlayıcı.
     * 
     * UUID kullanıyoruz çünkü:
     * - Dağıtık sistemlerde çakışma riski yok
     * - Tahmin edilemez (güvenlik)
     * - Veritabanı bağımsız
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Oluşturulma tarihi.
     * 
     * @CreationTimestamp: Kayıt ilk oluşturulduğunda otomatik set edilir.
     * updatable = false: Sonradan değiştirilemez.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Son güncelleme tarihi.
     * 
     * @UpdateTimestamp: Her güncelleme işleminde otomatik set edilir.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // ==================== GETTER METODLARI ====================

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // ==================== SETTER METODLARI ====================

    /**
     * ID setter'ı protected çünkü:
     * - ID genellikle otomatik üretilir
     * - Dışarıdan değiştirilmemeli
     * - Sadece test veya özel durumlar için
     */
    protected void setId(UUID id) {
        this.id = id;
    }

    /**
     * CreatedAt setter'ı protected.
     * Normalde Hibernate tarafından otomatik set edilir.
     */
    protected void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * UpdatedAt setter'ı protected.
     * Normalde Hibernate tarafından otomatik set edilir.
     */
    protected void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ==================== EQUALS VE HASHCODE ====================

    /**
     * İki entity'nin eşit olup olmadığını kontrol eder.
     * 
     * ID üzerinden karşılaştırma yapar.
     * ID null ise referans eşitliği kontrol edilir.
     */
    @Override
    public boolean equals(Object obj) {
        // Aynı referans mı?
        if (this == obj) {
            return true;
        }

        // Null veya farklı sınıf mı?
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        BaseEntity other = (BaseEntity) obj;

        // ID null ise eşit değildir (yeni, kaydedilmemiş entity)
        if (id == null || other.id == null) {
            return false;
        }

        // ID'leri karşılaştır
        return id.equals(other.id);
    }

    /**
     * Hash code hesaplar.
     * 
     * ID bazlı hash code kullanılır.
     * ID null ise sabit bir değer döner.
     */
    @Override
    public int hashCode() {
        // ID null ise sabit değer döndür
        // Bu, yeni entity'lerin Set'e eklenebilmesini sağlar
        if (id == null) {
            return getClass().hashCode();
        }
        return Objects.hash(id);
    }

    /**
     * String temsilini döndürür.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
