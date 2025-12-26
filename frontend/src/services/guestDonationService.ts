import api from '@/lib/axios';

export interface GuestCartItem {
    campaignId: string;
    amount: number;
    currency: string;
}

export interface GuestCheckoutRequest {
    guestEmail: string;
    guestName: string;
    guestPhone?: string;
    cartItems: GuestCartItem[];
    paymentDetails: {
        cardHolderName: string;
        cardNumber: string;
        expireMonth: string;
        expireYear: string;
        cvc: string;
        saveCard?: boolean;
    };
    donorMessage?: string;
    isAnonymous?: boolean;
}

export interface DonationReceiptInfo {
    donationId: string;
    receiptId: string;
    campaignId: string;
    campaignTitle: string;
    amount: number;
    currency: string;
    receiptNumber: string;
    receiptPdfUrl: string;
}

export interface GuestCheckoutResponse {
    success: boolean;
    message: string;
    donations: DonationReceiptInfo[];
    totalAmount: number;
    currency: string;
    guestEmail: string;
}

export const guestDonationService = {
    /**
     * Process guest checkout
     * Creates donations and receipts for anonymous users
     */
    async checkout(request: GuestCheckoutRequest): Promise<GuestCheckoutResponse> {
        const response = await api.post('/guest/checkout', request);
        return response.data.data; // ApiResponse wrapper
    },
};
