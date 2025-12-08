package com.seffafbagis.api.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Wrapper for paginated API responses.
 * 
 * This class provides a consistent format for paginated data across
 * the entire application, including pagination metadata.
 * 
 * JSON Output Example:
 * {
 *   "success": true,
 *   "data": [
 *     { "id": "1", "name": "Item 1" },
 *     { "id": "2", "name": "Item 2" }
 *   ],
 *   "pagination": {
 *     "currentPage": 0,
 *     "pageSize": 10,
 *     "totalElements": 25,
 *     "totalPages": 3,
 *     "first": true,
 *     "last": false,
 *     "hasNext": true,
 *     "hasPrevious": false
 *   },
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * 
 * @param <T> Type of data items in the page
 * @author Furkan
 * @version 1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    /**
     * Always true for paginated success responses.
     */
    private boolean success;

    /**
     * List of items for the current page.
     */
    private List<T> data;

    /**
     * Pagination metadata.
     */
    private PaginationMeta pagination;

    /**
     * Response timestamp (ISO-8601 format).
     */
    private LocalDateTime timestamp;

    /**
     * Nested class containing pagination metadata.
     */
    public static class PaginationMeta {

        /**
         * Current page number (0-indexed).
         */
        private int currentPage;

        /**
         * Number of items per page.
         */
        private int pageSize;

        /**
         * Total number of items across all pages.
         */
        private long totalElements;

        /**
         * Total number of pages.
         */
        private int totalPages;

        /**
         * Whether this is the first page.
         */
        private boolean first;

        /**
         * Whether this is the last page.
         */
        private boolean last;

        /**
         * Whether there is a next page available.
         */
        private boolean hasNext;

        /**
         * Whether there is a previous page available.
         */
        private boolean hasPrevious;

        // ==================== CONSTRUCTORS ====================

        /**
         * Default constructor for JSON deserialization.
         */
        public PaginationMeta() {
        }

        /**
         * Constructor accepting all pagination metadata fields.
         * 
         * @param currentPage Current page number (0-indexed)
         * @param pageSize Items per page
         * @param totalElements Total items across all pages
         * @param totalPages Total number of pages
         * @param first Is first page
         * @param last Is last page
         * @param hasNext Has next page
         * @param hasPrevious Has previous page
         */
        public PaginationMeta(int currentPage, int pageSize, long totalElements, int totalPages,
                              boolean first, boolean last, boolean hasNext, boolean hasPrevious) {
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        // ==================== GETTERS ====================

        public int getCurrentPage() {
            return currentPage;
        }

        public int getPageSize() {
            return pageSize;
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

        public boolean isHasNext() {
            return hasNext;
        }

        public boolean isHasPrevious() {
            return hasPrevious;
        }

        // ==================== SETTERS ====================

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
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

        public void setHasNext(boolean hasNext) {
            this.hasNext = hasNext;
        }

        public void setHasPrevious(boolean hasPrevious) {
            this.hasPrevious = hasPrevious;
        }
    }

    // ==================== CONSTRUCTORS ====================

    /**
     * Default constructor for JSON deserialization.
     */
    public PageResponse() {
        this.success = true;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Constructor accepting data and pagination metadata.
     * 
     * @param data List of items for current page
     * @param pagination Pagination metadata
     */
    public PageResponse(List<T> data, PaginationMeta pagination) {
        this.success = true;
        this.data = data;
        this.pagination = pagination;
        this.timestamp = LocalDateTime.now();
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Creates a PageResponse from a Spring Data Page object.
     * 
     * Automatically extracts pagination metadata from the Page object
     * and creates the response with proper formatting.
     * 
     * @param page Spring Data Page containing items and pagination info
     * @param <T> Type of items in the page
     * @return PageResponse instance
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        PaginationMeta pagination = new PaginationMeta(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext(),
            page.hasPrevious()
        );

        return new PageResponse<>(page.getContent(), pagination);
    }

    /**
     * Creates a PageResponse with mapped content and page metadata.
     * 
     * This is useful when mapping entities to DTOs. Pagination metadata
     * is extracted from the original Page object.
     * 
     * @param content Mapped content for the page
     * @param page Original Page object containing pagination metadata
     * @param <T> Type of mapped items
     * @return PageResponse instance
     */
    public static <T> PageResponse<T> of(List<T> content, Page<?> page) {
        PaginationMeta pagination = new PaginationMeta(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.hasNext(),
            page.hasPrevious()
        );

        return new PageResponse<>(content, pagination);
    }

    // ==================== GETTERS ====================

    public boolean isSuccess() {
        return success;
    }

    public List<T> getData() {
        return data;
    }

    public PaginationMeta getPagination() {
        return pagination;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // ==================== SETTERS ====================

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public void setPagination(PaginationMeta pagination) {
        this.pagination = pagination;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
