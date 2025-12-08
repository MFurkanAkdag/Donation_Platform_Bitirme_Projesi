"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "@heroui/button";
import { Input } from "@heroui/react";
import { Card, CardBody } from "@heroui/react";
import { useCart } from "@/contexts/CartContext";
import { mockCampaigns } from "@/lib/mockData";
import Link from "next/link";

export default function CheckoutPage() {
  const router = useRouter();
  const { cart, getTotalAmount, clearCart } = useCart();
  const [isProcessing, setIsProcessing] = useState(false);
  
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    phone: "",
    address: "",
    city: "",
    country: "",
    zipCode: "",
    cardNumber: "",
    cardName: "",
    expiryDate: "",
    cvv: "",
  });

  const cartWithCampaigns = cart
    .map((item) => ({
      ...item,
      campaign: mockCampaigns.find((c) => c.id === item.campaignId),
    }))
    .filter((item) => item.campaign !== undefined);

  const totalAmount = getTotalAmount();

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validate form
    const requiredFields = ["fullName", "email", "cardNumber", "cardName", "expiryDate", "cvv"];
    const missingFields = requiredFields.filter((field) => !formData[field as keyof typeof formData]);

    if (missingFields.length > 0) {
      alert("Please fill in all required fields");
      return;
    }

    setIsProcessing(true);

    // Simulate payment processing
    setTimeout(() => {
      setIsProcessing(false);
      clearCart();
      router.push("/checkout/success");
    }, 2000);
  };

  if (cart.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center max-w-md px-4">
          <div className="text-6xl mb-4">🛒</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">
            Your Cart is Empty
          </h2>
          <p className="text-gray-600 mb-6">
            Add campaigns to your cart before proceeding to checkout
          </p>
          <Link href="/campaigns">
            <Button color="primary" size="lg">
              Browse Campaigns
            </Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-8">
          Complete Your Donation
        </h1>

        <form onSubmit={handleSubmit}>
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Checkout Form */}
            <div className="lg:col-span-2 space-y-6">
              {/* Personal Information */}
              <Card>
                <CardBody className="p-6">
                  <h2 className="text-xl font-bold text-gray-900 mb-4">
                    Personal Information
                  </h2>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <Input
                      label="Full Name"
                      name="fullName"
                      value={formData.fullName}
                      onChange={handleInputChange}
                      placeholder="John Doe"
                      required
                      isRequired
                    />
                    <Input
                      label="Email Address"
                      name="email"
                      type="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      placeholder="john@example.com"
                      required
                      isRequired
                    />
                    <Input
                      label="Phone Number"
                      name="phone"
                      type="tel"
                      value={formData.phone}
                      onChange={handleInputChange}
                      placeholder="+1 (555) 000-0000"
                    />
                    <Input
                      label="Country"
                      name="country"
                      value={formData.country}
                      onChange={handleInputChange}
                      placeholder="United States"
                    />
                  </div>
                </CardBody>
              </Card>

              {/* Billing Address (Optional) */}
              <Card>
                <CardBody className="p-6">
                  <h2 className="text-xl font-bold text-gray-900 mb-4">
                    Billing Address <span className="text-sm text-gray-500 font-normal">(Optional)</span>
                  </h2>
                  <div className="grid grid-cols-1 gap-4">
                    <Input
                      label="Street Address"
                      name="address"
                      value={formData.address}
                      onChange={handleInputChange}
                      placeholder="123 Main Street"
                    />
                    <div className="grid grid-cols-2 gap-4">
                      <Input
                        label="City"
                        name="city"
                        value={formData.city}
                        onChange={handleInputChange}
                        placeholder="New York"
                      />
                      <Input
                        label="ZIP Code"
                        name="zipCode"
                        value={formData.zipCode}
                        onChange={handleInputChange}
                        placeholder="10001"
                      />
                    </div>
                  </div>
                </CardBody>
              </Card>

              {/* Payment Information */}
              <Card>
                <CardBody className="p-6">
                  <h2 className="text-xl font-bold text-gray-900 mb-4">
                    Payment Information
                  </h2>
                  <div className="space-y-4">
                    <Input
                      label="Card Number"
                      name="cardNumber"
                      value={formData.cardNumber}
                      onChange={handleInputChange}
                      placeholder="1234 5678 9012 3456"
                      maxLength={19}
                      required
                      isRequired
                    />
                    <Input
                      label="Cardholder Name"
                      name="cardName"
                      value={formData.cardName}
                      onChange={handleInputChange}
                      placeholder="John Doe"
                      required
                      isRequired
                    />
                    <div className="grid grid-cols-2 gap-4">
                      <Input
                        label="Expiry Date"
                        name="expiryDate"
                        value={formData.expiryDate}
                        onChange={handleInputChange}
                        placeholder="MM/YY"
                        maxLength={5}
                        required
                        isRequired
                      />
                      <Input
                        label="CVV"
                        name="cvv"
                        type="password"
                        value={formData.cvv}
                        onChange={handleInputChange}
                        placeholder="123"
                        maxLength={4}
                        required
                        isRequired
                      />
                    </div>
                  </div>

                  <div className="mt-6 flex items-center gap-2 text-sm text-gray-600">
                    <span>🔒</span>
                    <span>
                      Your payment information is encrypted and secure
                    </span>
                  </div>
                </CardBody>
              </Card>
            </div>

            {/* Order Summary */}
            <div className="lg:col-span-1">
              <div className="sticky top-4 space-y-6">
                <Card>
                  <CardBody className="p-6">
                    <h2 className="text-xl font-bold text-gray-900 mb-4">
                      Order Summary
                    </h2>

                    {/* Campaign List */}
                    <div className="space-y-3 mb-4">
                      {cartWithCampaigns.map((item) => (
                        <div
                          key={item.campaignId}
                          className="flex gap-3 pb-3 border-b last:border-b-0"
                        >
                          <img
                            src={item.campaign!.image}
                            alt={item.campaign!.title}
                            className="w-16 h-16 object-cover rounded"
                          />
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium text-gray-900 line-clamp-2">
                              {item.campaign!.title}
                            </p>
                            <p className="text-sm text-gray-600">
                              ${item.amount.toLocaleString()}
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* Total */}
                    <div className="border-t pt-4 space-y-2">
                      <div className="flex justify-between text-gray-600">
                        <span>Subtotal</span>
                        <span>${totalAmount.toLocaleString()}</span>
                      </div>
                      <div className="flex justify-between text-gray-600">
                        <span>Processing Fee</span>
                        <span>$0</span>
                      </div>
                      <div className="flex justify-between text-xl font-bold text-gray-900 pt-2 border-t">
                        <span>Total</span>
                        <span>${totalAmount.toLocaleString()}</span>
                      </div>
                    </div>

                    {/* Submit Button */}
                    <Button
                      type="submit"
                      color="primary"
                      size="lg"
                      className="w-full font-semibold mt-6"
                      isLoading={isProcessing}
                      disabled={isProcessing}
                    >
                      {isProcessing ? "Processing..." : "Complete Donation"}
                    </Button>

                    <p className="text-xs text-gray-500 text-center mt-4">
                      By completing this donation, you agree to our terms of
                      service and privacy policy
                    </p>
                  </CardBody>
                </Card>

                {/* Security Badges */}
                <Card>
                  <CardBody className="p-6">
                    <div className="space-y-3">
                      <div className="flex items-center gap-2 text-sm text-gray-600">
                        <span>🔒</span>
                        <span>256-bit SSL encryption</span>
                      </div>
                      <div className="flex items-center gap-2 text-sm text-gray-600">
                        <span>✓</span>
                        <span>PCI DSS compliant</span>
                      </div>
                      <div className="flex items-center gap-2 text-sm text-gray-600">
                        <span>📧</span>
                        <span>Instant donation receipt</span>
                      </div>
                    </div>
                  </CardBody>
                </Card>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
