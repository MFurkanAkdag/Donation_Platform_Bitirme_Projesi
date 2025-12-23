"use client";

import { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { Button } from "@heroui/button";
import { Input } from "@heroui/react";
import { Card, CardBody } from "@heroui/react";
import { mockCampaigns, mockNGOs } from "@/lib/mockData";
import { useCart } from "@/contexts/CartContext";
import ProgressBar from "@/components/ui/ProgressBar";
import Link from "next/link";

export default function CampaignDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { addToCart } = useCart();
  const [donationAmount, setDonationAmount] = useState("");
  const [showSuccess, setShowSuccess] = useState(false);

  const campaign = mockCampaigns.find((c) => c.id === params.id);
  const ngo = campaign ? mockNGOs.find((n) => n.name === campaign.ngoName) : null;

  if (!campaign) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="text-6xl mb-4">😔</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-2">
            Campaign Not Found
          </h2>
          <p className="text-gray-600 mb-6">
            The campaign you're looking for doesn't exist.
          </p>
          <Link href="/campaigns">
            <Button color="primary">Browse Campaigns</Button>
          </Link>
        </div>
      </div>
    );
  }

  const quickAmounts = [25, 50, 100, 250, 500, 1000];

  const handleAddToCart = () => {
    const amount = parseFloat(donationAmount);
    if (isNaN(amount) || amount <= 0) {
      alert("Please enter a valid donation amount");
      return;
    }

    addToCart(campaign.id, amount);
    setShowSuccess(true);
    setTimeout(() => setShowSuccess(false), 3000);
    setDonationAmount("");
  };

  const handleQuickAmount = (amount: number) => {
    setDonationAmount(amount.toString());
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Success Message */}
      {showSuccess && (
        <div className="fixed top-20 right-4 z-50 bg-green-500 text-white px-6 py-4 rounded-lg shadow-lg animate-slide-in">
          <div className="flex items-center gap-2">
            <span className="text-2xl">✓</span>
            <div>
              <p className="font-semibold">Added to cart!</p>
              <p className="text-sm">Ready to checkout</p>
            </div>
          </div>
        </div>
      )}

      {/* Hero Image */}
      <div className="relative h-96 overflow-hidden">
        <img
          src={campaign.image}
          alt={campaign.title}
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
        <div className="absolute bottom-0 left-0 right-0 p-8">
          <div className="max-w-7xl mx-auto">
            <div className="inline-block bg-white/90 backdrop-blur-sm px-4 py-2 rounded-full text-sm font-medium text-gray-700 mb-4">
              {campaign.category}
            </div>
            <h1 className="text-4xl md:text-5xl font-bold text-white mb-2">
              {campaign.title}
            </h1>
            <p className="text-xl text-white/90">{campaign.country}</p>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* NGO Info */}
            {ngo && (
              <Card>
                <CardBody className="p-6">
                  <div className="flex items-center gap-4">
                    <img
                      src={ngo.logo}
                      alt={ngo.name}
                      className="w-16 h-16 rounded-full object-cover"
                    />
                    <div className="flex-1">
                      <div className="flex items-center gap-2">
                        <Link 
                          href={`/organizations/${campaign.organizationId}`}
                          className="text-lg font-bold text-gray-900 hover:text-blue-600 transition-colors"
                        >
                          {ngo.name}
                        </Link>
                        {ngo.verified && (
                          <span className="text-blue-600 text-xl">✓</span>
                        )}
                      </div>
                      <p className="text-gray-600 text-sm">{ngo.description}</p>
                    </div>
                  </div>
                </CardBody>
              </Card>
            )}

            {/* Campaign Description */}
            <Card>
              <CardBody className="p-6">
                <h2 className="text-2xl font-bold text-gray-900 mb-4">
                  About This Campaign
                </h2>
                <div className="prose prose-gray max-w-none">
                  {campaign.detailedDescription.split("\n\n").map((paragraph, index) => (
                    <p key={index} className="text-gray-700 mb-4 whitespace-pre-line">
                      {paragraph}
                    </p>
                  ))}
                </div>
              </CardBody>
            </Card>

            {/* Campaign Details */}
            <Card>
              <CardBody className="p-6">
                <h2 className="text-2xl font-bold text-gray-900 mb-4">
                  Campaign Details
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <p className="text-gray-600 text-sm">Start Date</p>
                    <p className="font-semibold text-gray-900">
                      {new Date(campaign.startDate).toLocaleDateString()}
                    </p>
                  </div>
                  <div>
                    <p className="text-gray-600 text-sm">End Date</p>
                    <p className="font-semibold text-gray-900">
                      {new Date(campaign.endDate).toLocaleDateString()}
                    </p>
                  </div>
                  <div>
                    <p className="text-gray-600 text-sm">Location</p>
                    <p className="font-semibold text-gray-900">{campaign.country}</p>
                  </div>
                  <div>
                    <p className="text-gray-600 text-sm">Status</p>
                    <span className="inline-block bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm font-medium">
                      Active
                    </span>
                  </div>
                </div>
              </CardBody>
            </Card>
          </div>

          {/* Donation Sidebar */}
          <div className="lg:col-span-1">
            <div className="sticky top-4">
              <Card>
                <CardBody className="p-6">
                  <ProgressBar
                    current={campaign.currentAmount}
                    target={campaign.targetAmount}
                    className="mb-6"
                  />

                  <div className="mb-6">
                    <h3 className="text-lg font-bold text-gray-900 mb-4">
                      Choose Donation Amount
                    </h3>
                    <div className="grid grid-cols-3 gap-2 mb-4">
                      {quickAmounts.map((amount) => (
                        <Button
                          key={amount}
                          variant={donationAmount === amount.toString() ? "solid" : "bordered"}
                          color={donationAmount === amount.toString() ? "primary" : "default"}
                          onClick={() => handleQuickAmount(amount)}
                          className="w-full"
                        >
                          ${amount}
                        </Button>
                      ))}
                    </div>
                    <Input
                      type="number"
                      placeholder="Enter custom amount"
                      value={donationAmount}
                      onChange={(e) => setDonationAmount(e.target.value)}
                      startContent={<span className="text-gray-600">$</span>}
                      min="1"
                      className="mb-4"
                    />
                    <Button
                      color="primary"
                      size="lg"
                      className="w-full font-semibold"
                      onClick={handleAddToCart}
                      disabled={!donationAmount || parseFloat(donationAmount) <= 0}
                    >
                      Add to Cart
                    </Button>
                  </div>

                  <div className="border-t pt-4">
                    <Link href="/cart">
                      <Button
                        variant="bordered"
                        color="primary"
                        size="lg"
                        className="w-full font-semibold"
                      >
                        View Cart & Checkout
                      </Button>
                    </Link>
                  </div>

                  <div className="mt-6 space-y-3">
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                      <span>🔒</span>
                      <span>Secure payment processing</span>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                      <span>✓</span>
                      <span>Verified charity organization</span>
                    </div>
                    <div className="flex items-center gap-2 text-sm text-gray-600">
                      <span>📊</span>
                      <span>100% transparent tracking</span>
                    </div>
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
