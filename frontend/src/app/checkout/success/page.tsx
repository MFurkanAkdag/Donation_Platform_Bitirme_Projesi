import Link from "next/link";
import { Button } from "@heroui/button";

export default function CheckoutSuccessPage() {
  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="max-w-2xl mx-auto px-4 py-16 text-center">
        {/* Success Animation */}
        <div className="mb-8">
          <div className="w-24 h-24 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4 animate-bounce">
            <span className="text-5xl">✓</span>
          </div>
          <div className="w-32 h-1 bg-green-500 mx-auto rounded-full"></div>
        </div>

        {/* Success Message */}
        <h1 className="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
          Thank You for Your Generosity!
        </h1>
        <p className="text-xl text-gray-600 mb-8">
          Your donation has been successfully processed
        </p>

        {/* Details */}
        <div className="bg-white rounded-lg shadow-sm p-8 mb-8 text-left">
          <h2 className="text-xl font-bold text-gray-900 mb-4">
            What Happens Next?
          </h2>
          <div className="space-y-4">
            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                <span>📧</span>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900 mb-1">
                  Email Confirmation
                </h3>
                <p className="text-gray-600 text-sm">
                  You'll receive a detailed receipt and donation confirmation at
                  your email address within the next few minutes.
                </p>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                <span>💝</span>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900 mb-1">
                  Immediate Impact
                </h3>
                <p className="text-gray-600 text-sm">
                  Your donation will be immediately allocated to the campaigns
                  you selected. The NGOs will be notified of your contribution.
                </p>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                <span>📊</span>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900 mb-1">
                  Track Your Impact
                </h3>
                <p className="text-gray-600 text-sm">
                  You'll receive regular updates about how your donation is being
                  used and the real-world impact it's creating.
                </p>
              </div>
            </div>

            <div className="flex items-start gap-3">
              <div className="flex-shrink-0 w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center">
                <span>🏆</span>
              </div>
              <div>
                <h3 className="font-semibold text-gray-900 mb-1">
                  Tax Receipt
                </h3>
                <p className="text-gray-600 text-sm">
                  A tax-deductible receipt will be available in your email for
                  your records.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Impact Message */}
        <div className="bg-gradient-to-br from-blue-50 to-blue-100 rounded-lg p-8 mb-8">
          <div className="text-4xl mb-3">🌍</div>
          <h3 className="text-2xl font-bold text-gray-900 mb-2">
            You're Making a Real Difference
          </h3>
          <p className="text-gray-700">
            Thanks to donors like you, we've helped thousands of people around
            the world. Your contribution will create lasting positive change in
            communities that need it most.
          </p>
        </div>

        {/* Action Buttons */}
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Link href="/campaigns">
            <Button color="primary" size="lg" className="w-full sm:w-auto">
              Browse More Campaigns
            </Button>
          </Link>
          <Link href="/">
            <Button variant="bordered" size="lg" className="w-full sm:w-auto">
              Return to Home
            </Button>
          </Link>
        </div>

        {/* Social Share */}
        <div className="mt-12 pt-8 border-t">
          <p className="text-gray-600 mb-4">
            Inspire others to make a difference
          </p>
          <div className="flex gap-3 justify-center">
            <Button
              variant="light"
              className="bg-blue-600 text-white hover:bg-blue-700"
            >
              Share on Facebook
            </Button>
            <Button
              variant="light"
              className="bg-blue-400 text-white hover:bg-blue-500"
            >
              Share on Twitter
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
