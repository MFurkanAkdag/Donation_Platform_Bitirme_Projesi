# Phase 18.0: Donation Flow Refinements, Receipt Verification (Normalized) & Campaign Realization

## Overview
This phase focuses on refining the donation process, implementing a lightweight "Normalized" receipt/makbuz system with **Barcode** verification, and adding features to track the realization period of completed campaigns.

## Goals
1.  **Donation Confirmation:** Add a confirmation step ("Are you sure?") before processing donations.
2.  **Direct Donation Flow:** Simplify donation logic using a **Mock Payment Service** (simulating bank transfer/credit card without partial refund complexity).
3.  **Receipt (Makbuz) System:** Generate and store ONLY the barcode unique identifier. All other data (Amount, User, Date) will be fetched dynamically from the `Donation` table to avoid data redundancy.
4.  **Campaign Realization Tracking:** Define and track the period required for a funded campaign to be physically realized.
5.  **User Dashboard Updates:** Allow users to view their donations, download receipts, and track the realization status.

---

## 1. Donation Confirmation & Payment Logic
*   **Requirement:** When a user clicks "Approve"/"Donate", show a confirmation modal.
*   **Payment Logic (Mock):**
    *   Since we are not integrating Iyzico yet, we will use a `PaymentService` interface with a `MockPaymentService` implementation.
    *   **Process:** 
        1.  User confirms donation.
        2.  System simulates a call to payment provider (always returns `SUCCESS` for now).
        3.  Backend creates `Donation` record.
        4.  Backend automatically triggers `ReceiptService` to generate a barcode for this donation.

## 2. Receipt (Makbuz) & Barcode Verification
*   **Requirement:** Every successful donation generates a verification code (Barcode).
*   **Database Schema (`Receipt` Entity) - NORMALIZED:**
    *   `id` (Long, PK)
    *   `donation_id` (FK -> Donation, One-to-One)
    *   `barcode_data` (String, Unique UUID or Numeric string)
    *   *Note: Does NOT store Amount, User, Date. Fetches them via `donation_id` when needed.*
*   **Frontend Usage:** 
    *   Receipt View: Fetches Donation + Receipt info.
    *   Renders HTML Receipt Layout using Donation (Amount, Date, User Name) + Receipt (Barcode).

## 3. Campaign Realization Period
*   **Requirement:** After a campaign is fully funded (`COMPLETED`), track the time needed for the physical work.
*   **Database Updates (`Campaign` Entity):**
    *   Add `realization_deadline` (DateTime).
    *   Add `realization_status` (Enum: `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`).
*   **Flow:**
    *   Admin changes status to `IN_PROGRESS` and sets deadline.
    *   Frontend shows "Estimated Completion: [Date]" and a progress bar/timer.

## 4. User Dashboard & Proof
*   **Requirement:** Users can see details in "My Donations".
*   **Proof:** Admin uploads photo URL when status becomes `COMPLETED`.

---

## Action Plan for Phase 18.0

### Step 1: Database Migrations
1.  Create `receipts` table (Lightweight: id, donation_id, barcode_data).
2.  Update `campaigns` table.

### Step 2: Backend Implementation
1.  **Mock Payment:** Ensure `PaymentService` is clean.
2.  **Receipt Service:** Logic to create `Receipt` entity linked to `Donation`.
3.  **Verification Endpoint:** `GET /receipts/verify/{barcode}` -> Joins Receipt + Donation tables to return full info.
4.  **Campaign Realization:** Admin endpoints.

### Step 3: API Verification
1.  Test Donation -> Receipt generation.
2.  Test Barcode data retrieval (ensure it correct pulls amount/date from Donation).

---
**Output:** Please generate the necessary Spring Boot entities, repositories, services, and controllers to implement these features. Ensure DB migrations are essentially error-free.
