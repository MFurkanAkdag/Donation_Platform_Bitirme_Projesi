"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Button } from "@heroui/button";
import { Input } from "@heroui/react";
import { Card, CardBody } from "@heroui/react";
import { useCart } from "@/contexts/CartContext";
import { mockCampaigns } from "@/lib/mockData";

export default function CartPage() {
  const router = useRouter();
  const { cart, updateCartItem, removeFromCart, getTotalAmount } = useCart();

  const cartWithCampaigns = cart
    .map((item) => ({
      ...item,
      campaign: mockCampaigns.find((c) => c.id === item.campaignId),
    }))
    .filter((item) => item.campaign !== undefined);

  const totalAmount = getTotalAmount();

  const handleUpdateAmount = (campaignId: string, newAmount: string) => {
    const amount = parseFloat(newAmount);
    if (!isNaN(amount) && amount > 0) {
      updateCartItem(campaignId, amount);
    }
  };

  const handleCheckout = () => {
    if (cart.length === 0) {
      alert("Your cart is empty");
      return;
    }
    router.push("/checkout");
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
            Start making a difference by adding campaigns to your cart
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
          Your Donation Cart
        </h1>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Cart Items */}
          <div className="lg:col-span-2 space-y-4">
            {cartWithCampaigns.map((item) => (
              <Card key={item.campaignId}>
                <CardBody className="p-6">
                  <div className="flex flex-col sm:flex-row gap-4">
                    {/* Campaign Image */}
                    <div className="flex-shrink-0">
                      <img
                        src={item.campaign!.image}
                        alt={item.campaign!.title}
                        className="w-full sm:w-32 h-32 object-cover rounded-lg"
                      />
                    </div>

                    {/* Campaign Info */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between gap-2 mb-2">
                        <div className="flex-1">
                          <Link
                            href={`/campaigns/${item.campaignId}`}
                            className="text-lg font-bold text-gray-900 hover:text-blue-600 line-clamp-2"
                          >
                            {item.campaign!.title}
                          </Link>
                          <p className="text-sm text-gray-600 mt-1">
                            {item.campaign!.ngoName} • {item.campaign!.country}
                          </p>
                        </div>
                        <Button
                          isIconOnly
                          variant="light"
                          color="danger"
                          onClick={() => removeFromCart(item.campaignId)}
                          className="flex-shrink-0"
                        >
                          <span className="text-xl">×</span>
                        </Button>
                      </div>

                      <div className="inline-block bg-blue-50 text-blue-700 px-3 py-1 rounded-full text-xs font-medium mb-3">
                        {item.campaign!.category}
                      </div>

                      {/* Amount Input */}
                      <div className="flex items-center gap-4">
                        <div className="flex-1 max-w-xs">
                          <Input
                            type="number"
                            value={item.amount.toString()}
                            onChange={(e) =>
                              handleUpdateAmount(item.campaignId, e.target.value)
                            }
                            startContent={
                              <span className="text-gray-600">$</span>
                            }
                            min="1"
                            step="1"
                          />
                        </div>
                        <div className="text-xl font-bold text-gray-900">
                          ${item.amount.toLocaleString()}
                        </div>
                      </div>
                    </div>
                  </div>
                </CardBody>
              </Card>
            ))}

            <div className="pt-4">
              <Link href="/campaigns">
                <Button variant="light" color="primary">
                  ← Continue Shopping
                </Button>
              </Link>
            </div>
          </div>

          {/* Order Summary */}
          <div className="lg:col-span-1">
            <div className="sticky top-4">
              <Card>
                <CardBody className="p-6">
                  <h2 className="text-xl font-bold text-gray-900 mb-6">
                    Order Summary
                  </h2>

                  <div className="space-y-3 mb-6">
                    <div className="flex justify-between text-gray-600">
                      <span>Number of Campaigns</span>
                      <span className="font-medium">{cart.length}</span>
                    </div>
                    <div className="flex justify-between text-gray-600">
                      <span>Subtotal</span>
                      <span className="font-medium">
                        ${totalAmount.toLocaleString()}
                      </span>
                    </div>
                    <div className="border-t pt-3">
                      <div className="flex justify-between text-lg font-bold text-gray-900">
                        <span>Total Donation</span>
                        <span>${totalAmount.toLocaleString()}</span>
                      </div>
                    </div>
                  </div>

                  <Button
                    color="primary"
                    size="lg"
                    className="w-full font-semibold mb-4"
                    onClick={handleCheckout}
                  >
                    Proceed to Checkout
                  </Button>

                  <div className="space-y-3 pt-4 border-t">
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                      <span>🔒</span>
                      <span>Secure & encrypted checkout</span>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                      <span>✓</span>
                      <span>100% goes to verified NGOs</span>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                      <span>📧</span>
                      <span>Instant receipt via email</span>
                    </div>
                  </div>
                </CardBody>
              </Card>

              {/* Impact Message */}
              <Card className="mt-4">
                <CardBody className="p-6 bg-gradient-to-br from-blue-50 to-blue-100">
                  <div className="text-center">
                    <div className="text-4xl mb-2">💝</div>
                    <h3 className="font-bold text-gray-900 mb-1">
                      Your Impact
                    </h3>
                    <p className="text-sm text-gray-700">
                      Your donation of ${totalAmount.toLocaleString()} will make
                      a real difference in {cart.length} important{" "}
                      {cart.length === 1 ? "cause" : "causes"}
                    </p>
                  </div>
                </CardBody>
              </Card>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
