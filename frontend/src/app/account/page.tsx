"use client";

import { useAuth } from "@/contexts/AuthContext";
import { Card, CardBody, CardHeader } from "@heroui/react";
import { Button } from "@heroui/button";

// Mock data for the dashboard
const mockDonations = [
  { id: 1, campaign: "Emergency Food Relief for Gaza", amount: 50, date: "2024-01-15" },
  { id: 2, campaign: "Build a School in Rural Africa", amount: 100, date: "2024-01-10" },
  { id: 3, campaign: "Winter Clothing for Syrian Refugees", amount: 75, date: "2024-01-05" },
];

const mockFavoriteOrganizations = [
  { id: 1, name: "International Relief Fund", verified: true },
  { id: 2, name: "Hope for Children Foundation", verified: true },
];

export default function AccountDashboard() {
  const { user } = useAuth();

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">
          Welcome back, {user?.firstName || user?.displayName}!
        </h1>
        <p className="mt-1 text-sm text-gray-600">
          Here's what's happening with your account today.
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardBody className="p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-blue-100 rounded-md flex items-center justify-center">
                  <span className="text-blue-600 font-bold">€</span>
                </div>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    Total Donations
                  </dt>
                  <dd className="flex items-baseline">
                    <div className="text-2xl font-semibold text-gray-900">
                      €225
                    </div>
                  </dd>
                </dl>
              </div>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-green-100 rounded-md flex items-center justify-center">
                  <span className="text-green-600 font-bold">#</span>
                </div>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    Campaigns Supported
                  </dt>
                  <dd className="flex items-baseline">
                    <div className="text-2xl font-semibold text-gray-900">
                      3
                    </div>
                  </dd>
                </dl>
              </div>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-purple-100 rounded-md flex items-center justify-center">
                  <span className="text-purple-600 font-bold">♥</span>
                </div>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    Organizations Followed
                  </dt>
                  <dd className="flex items-baseline">
                    <div className="text-2xl font-semibold text-gray-900">
                      2
                    </div>
                  </dd>
                </dl>
              </div>
            </div>
          </CardBody>
        </Card>

        <Card>
          <CardBody className="p-6">
            <div className="flex items-center">
              <div className="flex-shrink-0">
                <div className="w-8 h-8 bg-yellow-100 rounded-md flex items-center justify-center">
                  <span className="text-yellow-600 font-bold">★</span>
                </div>
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">
                    Impact Score
                  </dt>
                  <dd className="flex items-baseline">
                    <div className="text-2xl font-semibold text-gray-900">
                      92%
                    </div>
                  </dd>
                </dl>
              </div>
            </div>
          </CardBody>
        </Card>
      </div>

      {/* Recent Activity and Favorites */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Recent Donations */}
        <Card>
          <CardHeader className="pb-4">
            <h3 className="text-lg font-medium text-gray-900">Recent Donations</h3>
          </CardHeader>
          <CardBody className="p-6">
            <div className="flow-root">
              <ul className="-my-5 divide-y divide-gray-200">
                {mockDonations.map((donation) => (
                  <li key={donation.id} className="py-4">
                    <div className="flex items-center space-x-4">
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {donation.campaign}
                        </p>
                        <p className="text-sm text-gray-500 truncate">
                          {donation.date}
                        </p>
                      </div>
                      <div>
                        <p className="text-sm font-medium text-gray-900">
                          €{donation.amount}
                        </p>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
            <div className="mt-6">
              <Button 
                color="primary" 
                variant="flat"
                className="w-full"
              >
                View all donations
              </Button>
            </div>
          </CardBody>
        </Card>

        {/* Favorite Organizations */}
        <Card>
          <CardHeader className="pb-4">
            <h3 className="text-lg font-medium text-gray-900">Favorite Organizations</h3>
          </CardHeader>
          <CardBody className="p-6">
            <div className="flow-root">
              <ul className="-my-5 divide-y divide-gray-200">
                {mockFavoriteOrganizations.map((org) => (
                  <li key={org.id} className="py-4">
                    <div className="flex items-center space-x-4">
                      <div className="flex-shrink-0">
                        <div className="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center">
                          <span className="text-gray-700 font-bold">
                            {org.name.charAt(0)}
                          </span>
                        </div>
                      </div>
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium text-gray-900 truncate">
                          {org.name}
                        </p>
                        <div className="flex items-center">
                          {org.verified && (
                            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-800">
                              Verified
                            </span>
                          )}
                        </div>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            </div>
            <div className="mt-6">
              <Button 
                color="primary" 
                variant="flat"
                className="w-full"
              >
                View all organizations
              </Button>
            </div>
          </CardBody>
        </Card>
      </div>
    </div>
  );
}