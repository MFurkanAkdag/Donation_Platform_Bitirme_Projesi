"use client";

import { useState, useMemo } from "react";
import { Input, Select, SelectItem } from "@heroui/react";
import { Button } from "@heroui/button";
import OrganizationCard from "@/components/ui/OrganizationCard";
import { mockOrganizationSummary } from "@/lib/organizationSummary";

export default function OrganizationsPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedType, setSelectedType] = useState("all");
  const [selectedStatus, setSelectedStatus] = useState("all");
  const [selectedFeatured, setSelectedFeatured] = useState("all");
  const [selectedCity, setSelectedCity] = useState("all");
  const [sortBy, setSortBy] = useState("score");

  // Extract unique cities
  const cities = useMemo(() => {
    const citySet = new Set(mockOrganizationSummary.map(org => org.city).filter(Boolean));
    return Array.from(citySet).sort();
  }, []);

  // Filter and sort organizations
  const filteredOrganizations = useMemo(() => {
    let filtered = mockOrganizationSummary.filter((org) => {
      const matchesSearch =
        org.legal_name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (org.trade_name?.toLowerCase().includes(searchQuery.toLowerCase()) || false);

      const matchesType = selectedType === "all" || org.organization_type === selectedType;
      const matchesStatus = selectedStatus === "all" || org.verification_status === selectedStatus;
      const matchesFeatured = selectedFeatured === "all" || 
        (selectedFeatured === "featured" ? org.is_featured : !org.is_featured);
      const matchesCity = selectedCity === "all" || org.city === selectedCity;

      return matchesSearch && matchesType && matchesStatus && matchesFeatured && matchesCity;
    });

    // Sort
    filtered = [...filtered].sort((a, b) => {
      switch (sortBy) {
        case "score":
          return (b.current_score || 0) - (a.current_score || 0);
        case "campaigns":
          return b.total_campaigns - a.total_campaigns;
        case "collected":
          return b.total_collected - a.total_collected;
        default:
          return 0;
      }
    });

    return filtered;
  }, [searchQuery, selectedType, selectedStatus, selectedFeatured, selectedCity, sortBy]);

  const handleClearFilters = () => {
    setSearchQuery("");
    setSelectedType("all");
    setSelectedStatus("all");
    setSelectedFeatured("all");
    setSelectedCity("all");
    setSortBy("score");
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-4xl font-bold text-gray-900 mb-2">Organizations</h1>
          <p className="text-lg text-gray-600">
            Discover verified organizations making a difference around the world
          </p>
        </div>

        {/* Search and Filters */}
        <div className="bg-white rounded-lg shadow-sm p-6 mb-8">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-4">
            <Input
              placeholder="Search organizations..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full"
              startContent={
                <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              }
            />

            <Select
              selectedKeys={[selectedType]}
              onSelectionChange={(keys) => setSelectedType(Array.from(keys)[0] as string)}
              placeholder="Organization Type"
              className="w-full"
            >
              <SelectItem key="all">All Types</SelectItem>
              <SelectItem key="foundation">Foundation</SelectItem>
              <SelectItem key="association">Association</SelectItem>
              <SelectItem key="ngo">NGO</SelectItem>
            </Select>

            <Select
              selectedKeys={[selectedStatus]}
              onSelectionChange={(keys) => setSelectedStatus(Array.from(keys)[0] as string)}
              placeholder="Verification Status"
              className="w-full"
            >
              <SelectItem key="all">All Status</SelectItem>
              <SelectItem key="approved">Approved</SelectItem>
              <SelectItem key="in_review">In Review</SelectItem>
              <SelectItem key="pending">Pending</SelectItem>
              <SelectItem key="rejected">Rejected</SelectItem>
            </Select>

            <Select
              selectedKeys={[selectedFeatured]}
              onSelectionChange={(keys) => setSelectedFeatured(Array.from(keys)[0] as string)}
              placeholder="Featured"
              className="w-full"
            >
              <SelectItem key="all">All Organizations</SelectItem>
              <SelectItem key="featured">Featured Only</SelectItem>
              <SelectItem key="non-featured">Non-Featured</SelectItem>
            </Select>

            <Select
              selectedKeys={[selectedCity]}
              onSelectionChange={(keys) => setSelectedCity(Array.from(keys)[0] as string)}
              placeholder="City"
              className="w-full"
            >
              <SelectItem key="all">All Cities</SelectItem>
              {cities.map((city) => (
                <SelectItem key={city}>
                  {city}
                </SelectItem>
              ))}
            </Select>

            <Select
              selectedKeys={[sortBy]}
              onSelectionChange={(keys) => setSortBy(Array.from(keys)[0] as string)}
              placeholder="Sort By"
              className="w-full"
            >
              <SelectItem key="score">Highest Transparency Score</SelectItem>
              <SelectItem key="campaigns">Most Campaigns</SelectItem>
              <SelectItem key="collected">Highest Total Collected</SelectItem>
            </Select>
          </div>

          <div className="flex justify-between items-center">
            <p className="text-sm text-gray-600">
              {filteredOrganizations.length} organization{filteredOrganizations.length !== 1 ? 's' : ''} found
            </p>
            <Button
              variant="flat"
              onPress={handleClearFilters}
              size="sm"
            >
              Clear Filters
            </Button>
          </div>
        </div>

        {/* Organizations Grid */}
        {filteredOrganizations.length === 0 ? (
          <div className="bg-white rounded-lg shadow-sm p-12 text-center">
            <div className="text-6xl mb-4">🔍</div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">
              No organizations found
            </h2>
            <p className="text-gray-600 mb-6">
              Try adjusting your filters or search criteria
            </p>
            <Button color="primary" onPress={handleClearFilters}>
              Clear Filters
            </Button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredOrganizations.map((org) => (
              <OrganizationCard key={org.organization_id} organization={org} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
