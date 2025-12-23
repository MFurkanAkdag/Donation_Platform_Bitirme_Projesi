"use client";

import Link from "next/link";
import { Card, CardBody, CardFooter, CardHeader } from "@heroui/react";
import { Button } from "@heroui/button";
import { Chip } from "@heroui/react";
import { Progress } from "@heroui/react";
import { OrganizationSummary } from "@/lib/organizationSummary";

interface OrganizationCardProps {
  organization: OrganizationSummary;
}

export default function OrganizationCard({ organization }: OrganizationCardProps) {
  const getVerificationColor = (status: string) => {
    switch (status) {
      case 'approved': return 'success';
      case 'in_review': return 'warning';
      case 'rejected': return 'danger';
      default: return 'default';
    }
  };

  const getOrganizationTypeColor = (type: string) => {
    switch (type) {
      case 'foundation': return 'primary';
      case 'association': return 'secondary';
      case 'ngo': return 'success';
      default: return 'default';
    }
  };

  return (
    <Card className="w-full hover:shadow-xl transition-shadow duration-300">
      <CardHeader className="pb-0 pt-4 px-4 flex-col items-start">
        <div className="flex items-start gap-3 w-full">
          {organization.logo_url && (
            <img
              src={organization.logo_url}
              alt={organization.legal_name}
              className="w-16 h-16 rounded-lg object-cover flex-shrink-0"
            />
          )}
          <div className="flex-1 min-w-0">
            <h3 className="text-lg font-bold text-gray-900 line-clamp-2">
              {organization.legal_name}
            </h3>
            {organization.trade_name && (
              <p className="text-sm text-gray-500">{organization.trade_name}</p>
            )}
          </div>
        </div>
      </CardHeader>
      
      <CardBody className="px-4 py-3">
        <div className="flex flex-wrap gap-2 mb-3">
          <Chip 
            color={getOrganizationTypeColor(organization.organization_type)}
            size="sm"
            variant="flat"
          >
            {organization.organization_type}
          </Chip>
          <Chip 
            color={getVerificationColor(organization.verification_status)}
            size="sm"
          >
            {organization.verification_status}
          </Chip>
          {organization.is_featured && (
            <Chip color="warning" size="sm" variant="flat">
              ⭐ Featured
            </Chip>
          )}
        </div>
        
        {organization.city && (
          <p className="text-sm text-gray-600 mb-3">
            📍 {organization.city}, {organization.country}
          </p>
        )}
        
        {organization.current_score !== undefined && (
          <div className="mb-3">
            <div className="flex justify-between text-sm text-gray-600 mb-1">
              <span>Transparency Score</span>
              <span className="font-semibold">{organization.current_score}%</span>
            </div>
            <Progress 
              value={organization.current_score} 
              color={organization.current_score >= 90 ? "success" : organization.current_score >= 75 ? "warning" : "default"}
              className="h-2"
            />
          </div>
        )}
        
        <div className="grid grid-cols-3 gap-2 text-center">
          <div>
            <p className="text-lg font-bold text-gray-900">{organization.total_campaigns}</p>
            <p className="text-xs text-gray-600">Total</p>
          </div>
          <div>
            <p className="text-lg font-bold text-green-600">{organization.active_campaigns}</p>
            <p className="text-xs text-gray-600">Active</p>
          </div>
          <div>
            <p className="text-lg font-bold text-gray-900">
              €{(organization.total_collected / 1000).toFixed(0)}k
            </p>
            <p className="text-xs text-gray-600">Collected</p>
          </div>
        </div>
      </CardBody>
      
      <CardFooter className="pt-0 px-4 pb-4">
        <Link href={`/organizations/${organization.organization_id}`} className="w-full">
          <Button color="primary" variant="flat" className="w-full">
            View Organization
          </Button>
        </Link>
      </CardFooter>
    </Card>
  );
}
