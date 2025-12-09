package com.seffafbagis.api.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

/**
 * Sayfalanmış veriler için response wrapper sınıfı.
 * 
 * Spring Data Page'i frontend dostu formata dönüştürür.
 * 
 * Response örneği:
 * {
 * "success": true,
 * "message": "Kullanıcılar listelendi",
 * "data": [...],
 * "page": 0,
 * "size": 10,
 * "totalElements": 100,
 * "totalPages": 10,
 * "first": true,
 * "last": false,
 * "timestamp": "2024-01-15T10:30:00Z"
 * }
 * 
 * @param <T> Liste elemanlarının tipi
 * @author Furkan
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {

    /**
     * İşlem başarılı mı?
     */
    private boolean success;

    /**
     * Kullanıcıya gösterilecek mesaj.
     */
    private String message;

    /**
     * Sayfa içeriği.
     */
    private List<T> data;

    /**
     * Mevcut sayfa numarası (0-indexed).
     */
    private int page;

    /**
     * Sayfa başına eleman sayısı.
     */
    private int size;

    /**
     * Toplam eleman sayısı.
     */
    private long totalElements;

    /**
     * Toplam sayfa sayısı.
     */
    private int totalPages;

    /**
     * İlk sayfa mı?
     */
    private boolean first;

    /**
     * Son sayfa mı?
     */
    private boolean last;

    /**
     * Boş mu?
     */
    private boolean empty;

    /**
     * Response timestamp.
     */
    private Instant timestamp;

    // ==================== CONSTRUCTOR ====================

    /**
     * Boş constructor.
     */
    public PagedResponse() {
        this.timestamp = Instant.now();
        this.success = true;
    }

    /**
     * Spring Data Page'den constructor.
     * 
     * @param page Spring Data Page objesi
     */
    public PagedResponse(Page<T> page) {
        this.success = true;
        this.message = "Veriler listelendi";
        this.data = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.empty = page.isEmpty();
        this.timestamp = Instant.now();
    }

    /**
     * Mesaj ile constructor.
     * 
     * @param page    Spring Data Page objesi
     * @param message Mesaj
     */
    public PagedResponse(Page<T> page, String message) {
        this(page);
        this.message = message;
    }

    // ==================== FACTORY METODLAR ====================

    /**
     * Spring Data Page'den PagedResponse oluşturur.
     * 
     * @param page Spring Data Page objesi
     * @return PagedResponse
     */
    public static <T> PagedResponse<T> of(Page<T> page) {
        return new PagedResponse<>(page);
    }

    /**
     * Spring Data Page'den PagedResponse oluşturur (mesaj ile).
     * 
     * @param page    Spring Data Page objesi
     * @param message Mesaj
     * @return PagedResponse
     */
    public static <T> PagedResponse<T> of(Page<T> page, String message) {
        return new PagedResponse<>(page, message);
    }

    /**
     * Dönüştürülmüş içerikle PagedResponse oluşturur.
     * 
     * Entity'den DTO'ya dönüşüm sonrası kullanılır.
     * 
     * @param originalPage     Orijinal Page
     * @param convertedContent Dönüştürülmüş içerik
     * @param message          Mesaj
     * @return PagedResponse
     */
    public static <T, U> PagedResponse<T> of(Page<U> originalPage, List<T> convertedContent, String message) {
        PagedResponse<T> response = new PagedResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(convertedContent);
        response.setPage(originalPage.getNumber());
        response.setSize(originalPage.getSize());
        response.setTotalElements(originalPage.getTotalElements());
        response.setTotalPages(originalPage.getTotalPages());
        response.setFirst(originalPage.isFirst());
        response.setLast(originalPage.isLast());
        response.setEmpty(originalPage.isEmpty());
        return response;
    }

    // ==================== YARDIMCI METODLAR ====================

    /**
     * Sonraki sayfa var mı?
     * 
     * @return Varsa true
     */
    public boolean hasNext() {
        return !last;
    }

    /**
     * Önceki sayfa var mı?
     * 
     * @return Varsa true
     */
    public boolean hasPrevious() {
        return !first;
    }

    // ==================== GETTER METODLARI ====================

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<T> getData() {
        return data;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    public boolean isEmpty() {
        return empty;
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

    public void setData(List<T> data) {
        this.data = data;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
