"use client";

import Link from "next/link";
import { Card, CardBody, CardFooter } from "@heroui/react";
import { Button } from "@heroui/button";
import { Campaign } from "@/lib/mockData";
import ProgressBar from "./ProgressBar";

interface CampaignCardProps {
  campaign: Campaign;
}

export default function CampaignCard({ campaign }: CampaignCardProps) {
  return (
    <Card className="w-full hover:shadow-xl transition-shadow duration-300">
      <CardBody className="p-0">
        <div className="relative w-full h-48 overflow-hidden">
          <img
            src={campaign.image}
            alt={campaign.title}
            className="w-full h-full object-cover"
          />
          <div className="absolute top-3 right-3 bg-white/90 backdrop-blur-sm px-3 py-1 rounded-full text-sm font-medium text-gray-700">
            {campaign.category}
          </div>
        </div>
        <div className="p-4">
          <div className="flex items-center gap-2 mb-2">
            <span className="text-sm text-gray-600">{campaign.ngoName}</span>
            <span className="text-blue-600">✓</span>
          </div>
          <h3 className="text-xl font-bold text-gray-900 mb-2 line-clamp-2">
            {campaign.title}
          </h3>
          <p className="text-gray-600 text-sm mb-4 line-clamp-2">
            {campaign.description}
          </p>
          <ProgressBar
            current={campaign.currentAmount}
            target={campaign.targetAmount}
          />
        </div>
      </CardBody>
      <CardFooter className="px-4 pb-4 pt-0">
        <Link href={`/campaigns/${campaign.id}`} className="w-full">
          <Button color="primary" className="w-full">
            View Campaign
          </Button>
        </Link>
      </CardFooter>
    </Card>
  );
}
