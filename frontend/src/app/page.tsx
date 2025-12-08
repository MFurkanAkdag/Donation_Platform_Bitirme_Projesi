import Link from "next/link";
import { Button } from "@heroui/button";
import CampaignCard from "@/components/ui/CampaignCard";
import { mockCampaigns, featuredCampaignIds } from "@/lib/mockData";

export default function HomePage() {
  const featuredCampaigns = mockCampaigns.filter((campaign) =>
    featuredCampaignIds.includes(campaign.id)
  );

  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="bg-gradient-to-br from-blue-600 via-blue-700 to-blue-800 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 md:py-28">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
            <div className="space-y-6">
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold leading-tight">
                Make a Difference
                <br />
                <span className="text-blue-200">One Donation at a Time</span>
              </h1>
              <p className="text-lg md:text-xl text-blue-100 max-w-xl">
                Connect with trusted NGOs and charities worldwide. Support
                meaningful causes with complete transparency and see the real
                impact of your generosity.
              </p>
              <div className="flex flex-col sm:flex-row gap-4">
                <Link href="/campaigns">
                  <Button
                    size="lg"
                    className="bg-white text-blue-600 hover:bg-blue-50 font-semibold px-8 w-full sm:w-auto"
                  >
                    Browse Campaigns
                  </Button>
                </Link>
                <Link href="/about">
                  <Button
                    size="lg"
                    variant="bordered"
                    className="border-2 border-white text-white hover:bg-white/10 font-semibold px-8 w-full sm:w-auto"
                  >
                    Learn More
                  </Button>
                </Link>
              </div>

              {/* Stats */}
              <div className="grid grid-cols-3 gap-6 pt-8">
                <div>
                  <div className="text-3xl md:text-4xl font-bold">500+</div>
                  <div className="text-blue-200 text-sm">Active Campaigns</div>
                </div>
                <div>
                  <div className="text-3xl md:text-4xl font-bold">$2.5M</div>
                  <div className="text-blue-200 text-sm">Funds Raised</div>
                </div>
                <div>
                  <div className="text-3xl md:text-4xl font-bold">50K+</div>
                  <div className="text-blue-200 text-sm">Donors</div>
                </div>
              </div>
            </div>

            <div className="hidden lg:block">
              <div className="relative">
                <div className="absolute inset-0 bg-blue-400 rounded-2xl transform rotate-6"></div>
                <img
                  src="https://images.unsplash.com/photo-1488521787991-ed7bbaae773c?w=600&h=700&fit=crop"
                  alt="People helping"
                  className="relative rounded-2xl shadow-2xl object-cover w-full h-[500px]"
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Campaigns Section */}
      <section className="py-16 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
              Featured Campaigns
            </h2>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              Urgent causes that need your support right now. Every contribution
              makes a real difference.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            {featuredCampaigns.map((campaign) => (
              <CampaignCard key={campaign.id} campaign={campaign} />
            ))}
          </div>

          <div className="text-center mt-12">
            <Link href="/campaigns">
              <Button size="lg" color="primary" className="px-8">
                View All Campaigns
              </Button>
            </Link>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section className="py-16 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
              How It Works
            </h2>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              Making a difference is simple with our transparent donation
              platform
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-3xl">🔍</span>
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">
                1. Browse Campaigns
              </h3>
              <p className="text-gray-600">
                Explore verified campaigns from trusted NGOs and charities
                around the world
              </p>
            </div>

            <div className="text-center">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-3xl">💝</span>
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">
                2. Choose Your Impact
              </h3>
              <p className="text-gray-600">
                Select causes you care about and decide how much you want to
                contribute
              </p>
            </div>

            <div className="text-center">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-3xl">✅</span>
              </div>
              <h3 className="text-xl font-bold text-gray-900 mb-2">
                3. Track Your Donation
              </h3>
              <p className="text-gray-600">
                Receive updates and see the real-world impact of your
                generosity
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Why Choose Us Section */}
      <section className="py-16 bg-blue-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
              Why Choose DonationHub?
            </h2>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              We're committed to transparency, security, and real impact
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-white p-6 rounded-lg shadow-sm">
              <div className="text-3xl mb-3">🔒</div>
              <h3 className="font-bold text-gray-900 mb-2">Secure Payments</h3>
              <p className="text-gray-600 text-sm">
                Bank-level encryption for all transactions
              </p>
            </div>

            <div className="bg-white p-6 rounded-lg shadow-sm">
              <div className="text-3xl mb-3">✓</div>
              <h3 className="font-bold text-gray-900 mb-2">Verified NGOs</h3>
              <p className="text-gray-600 text-sm">
                All organizations are thoroughly vetted
              </p>
            </div>

            <div className="bg-white p-6 rounded-lg shadow-sm">
              <div className="text-3xl mb-3">📊</div>
              <h3 className="font-bold text-gray-900 mb-2">Full Transparency</h3>
              <p className="text-gray-600 text-sm">
                Track exactly how your donation is used
              </p>
            </div>

            <div className="bg-white p-6 rounded-lg shadow-sm">
              <div className="text-3xl mb-3">🌍</div>
              <h3 className="font-bold text-gray-900 mb-2">Global Impact</h3>
              <p className="text-gray-600 text-sm">
                Support causes worldwide from one platform
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-16 bg-gradient-to-r from-blue-600 to-blue-700 text-white">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl md:text-4xl font-bold mb-4">
            Ready to Make a Difference?
          </h2>
          <p className="text-xl text-blue-100 mb-8">
            Join thousands of donors making the world a better place
          </p>
          <Link href="/campaigns">
            <Button
              size="lg"
              className="bg-white text-blue-600 hover:bg-blue-50 font-semibold px-8"
            >
              Start Donating Today
            </Button>
          </Link>
        </div>
      </section>
    </div>
  );
}
