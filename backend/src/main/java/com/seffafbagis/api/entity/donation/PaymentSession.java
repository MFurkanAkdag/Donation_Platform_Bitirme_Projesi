package com.seffafbagis.api.entity.donation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.seffafbagis.api.entity.base.BaseEntity;
import com.seffafbagis.api.entity.user.User;
import com.seffafbagis.api.enums.PaymentSessionStatus;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a payment session for shopping cart functionality.
 * Allows multiple donations to be paid in a single transaction.
 * 
 * @author System
 * @version 1.0
 */
@Entity
@Table(name = "payment_sessions", indexes = {
        @Index(name = "idx_payment_sessions_user", columnList = "user_id"),
        @Index(name = "idx_payment_sessions_status", columnList = "status")
})
public class PaymentSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Nullable for guest checkout

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 3)
    private String currency = "TRY";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentSessionStatus status = PaymentSessionStatus.PENDING;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @OneToMany(mappedBy = "paymentSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Donation> donations = new ArrayList<>();

    @OneToOne(mappedBy = "paymentSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Transaction transaction;

    /**
     * Cart items (before checkout) - stored as JSON
     */
    @Type(JsonType.class)
    @Column(name = "cart_items", columnDefinition = "jsonb")
    private List<CartItem> cartItems = new ArrayList<>();

    // ==================== CART ITEM EMBEDDABLE ====================

    /**
     * Cart item before checkout (not yet a Donation).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItem {
        @JsonProperty("campaignId")
        private java.util.UUID campaignId;

        @JsonProperty("amount")
        private BigDecimal amount;

        @JsonProperty("currency")
        private String currency = "TRY";
    }

    // ==================== CONSTRUCTOR ====================

    public PaymentSession() {
    }

    public PaymentSession(User user, BigDecimal totalAmount) {
        this.user = user;
        this.totalAmount = totalAmount;
        this.status = PaymentSessionStatus.PENDING;
    }

    // ==================== BUSINESS METHODS ====================

    /**
     * Add a donation to this payment session.
     */
    public void addDonation(Donation donation) {
        donations.add(donation);
        donation.setPaymentSession(this);
    }

    /**
     * Remove a donation from this payment session.
     */
    public void removeDonation(Donation donation) {
        donations.remove(donation);
        donation.setPaymentSession(null);
    }

    /**
     * Calculate total amount from all donations.
     */
    public void recalculateTotalAmount() {
        this.totalAmount = donations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculate total from cart items (before checkout).
     */
    public BigDecimal calculateCartTotal() {
        return cartItems.stream()
                .map(CartItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Add item to cart.
     */
    public void addCartItem(java.util.UUID campaignId, BigDecimal amount, String currency) {
        cartItems.add(new CartItem(campaignId, amount, currency));
        this.totalAmount = calculateCartTotal();
    }

    /**
     * Remove item from cart by campaign ID.
     */
    public void removeCartItem(java.util.UUID campaignId) {
        cartItems.removeIf(item -> item.getCampaignId().equals(campaignId));
        this.totalAmount = calculateCartTotal();
    }

    /**
     * Clear cart items.
     */
    public void clearCart() {
        cartItems.clear();
        this.totalAmount = BigDecimal.ZERO;
    }

    /**
     * Mark payment session as completed.
     */
    public void markAsCompleted() {
        this.status = PaymentSessionStatus.COMPLETED;
        this.completedAt = OffsetDateTime.now();
    }

    /**
     * Mark payment session as failed.
     */
    public void markAsFailed() {
        this.status = PaymentSessionStatus.FAILED;
    }

    /**
     * Check if payment session is in progress.
     */
    public boolean isProcessing() {
        return this.status == PaymentSessionStatus.PROCESSING;
    }

    /**
     * Check if payment session is completed.
     */
    public boolean isCompleted() {
        return this.status == PaymentSessionStatus.COMPLETED;
    }

    // ==================== GETTERS AND SETTERS ====================

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentSessionStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentSessionStatus status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<Donation> getDonations() {
        return donations;
    }

    public void setDonations(List<Donation> donations) {
        this.donations = donations;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
}
