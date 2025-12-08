"use client";

import { Card, CardBody, CardHeader } from "@heroui/react";
import { Accordion, AccordionItem } from "@heroui/react";

export default function AboutPage() {
  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-extrabold text-gray-900 sm:text-5xl">
            About DonationHub
          </h1>
          <p className="mt-4 text-xl text-gray-600">
            Connecting compassionate donors with verified organizations to create meaningful impact
          </p>
        </div>

        <div className="bg-white rounded-lg shadow-lg overflow-hidden mb-12">
          <div className="px-6 py-8 sm:p-10">
            <div className="prose prose-lg mx-auto text-gray-600">
              <p className="text-lg">
                DonationHub was founded with a simple but powerful mission: to make charitable giving more transparent, 
                efficient, and impactful. We believe that everyone who wants to help should be able to do so with confidence, 
                knowing exactly where their donation is going and how it's being used.
              </p>
              
              <h2 className="text-2xl font-bold text-gray-900 mt-8">Our Mission</h2>
              <p>
                Our mission is to bridge the gap between generous donors and verified nonprofit organizations by providing 
                a transparent platform where every donation can be tracked, every impact can be measured, and every donor 
                can see the real-world results of their generosity.
              </p>
              
              <h2 className="text-2xl font-bold text-gray-900 mt-8">How It Works</h2>
              <div className="mt-6">
                <div className="flex">
                  <div className="flex-shrink-0">
                    <div className="flex items-center justify-center h-12 w-12 rounded-md bg-blue-500 text-white">
                      1
                    </div>
                  </div>
                  <div className="ml-4">
                    <h3 className="text-lg font-medium text-gray-900">Discover Campaigns</h3>
                    <p className="mt-2 text-gray-600">
                      Browse our carefully curated selection of verified campaigns from trusted organizations around the world.
                    </p>
                  </div>
                </div>
                
                <div className="flex mt-8">
                  <div className="flex-shrink-0">
                    <div className="flex items-center justify-center h-12 w-12 rounded-md bg-blue-500 text-white">
                      2
                    </div>
                  </div>
                  <div className="ml-4">
                    <h3 className="text-lg font-medium text-gray-900">Donate Securely</h3>
                    <p className="mt-2 text-gray-600">
                      Make your donation through our secure payment system, knowing that 100% of your funds go directly to the cause.
                    </p>
                  </div>
                </div>
                
                <div className="flex mt-8">
                  <div className="flex-shrink-0">
                    <div className="flex items-center justify-center h-12 w-12 rounded-md bg-blue-500 text-white">
                      3
                    </div>
                  </div>
                  <div className="ml-4">
                    <h3 className="text-lg font-medium text-gray-900">Track Impact</h3>
                    <p className="mt-2 text-gray-600">
                      Follow your donation's journey through regular updates and transparent reporting from the organizations.
                    </p>
                  </div>
                </div>
                
                <div className="flex mt-8">
                  <div className="flex-shrink-0">
                    <div className="flex items-center justify-center h-12 w-12 rounded-md bg-blue-500 text-white">
                      4
                    </div>
                  </div>
                  <div className="ml-4">
                    <h3 className="text-lg font-medium text-gray-900">See Results</h3>
                    <p className="mt-2 text-gray-600">
                      Witness the tangible impact of your contribution through photos, videos, and stories from the field.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-12">
          <Card>
            <CardHeader className="pb-4">
              <h3 className="text-xl font-bold text-gray-900">Transparency</h3>
            </CardHeader>
            <CardBody className="p-6">
              <p className="text-gray-600">
                Every donation is tracked with full transparency. We provide detailed reports and evidence of how funds are used.
              </p>
            </CardBody>
          </Card>
          
          <Card>
            <CardHeader className="pb-4">
              <h3 className="text-xl font-bold text-gray-900">Verification</h3>
            </CardHeader>
            <CardBody className="p-6">
              <p className="text-gray-600">
                All organizations and campaigns undergo rigorous verification to ensure authenticity and effectiveness.
              </p>
            </CardBody>
          </Card>
          
          <Card>
            <CardHeader className="pb-4">
              <h3 className="text-xl font-bold text-gray-900">Impact</h3>
            </CardHeader>
            <CardBody className="p-6">
              <p className="text-gray-600">
                We measure and showcase the real-world impact of every campaign, so you can see exactly how you're helping.
              </p>
            </CardBody>
          </Card>
        </div>

        <Accordion variant="splitted">
          <AccordionItem key="1" aria-label="Accordion 1" title="Who verifies the organizations?">
            <p className="text-gray-600">
              Our dedicated verification team works with third-party auditors and local partners to thoroughly vet every organization 
              before they can launch campaigns on our platform. We examine financial records, legal standing, impact reports, and 
              conduct interviews with key personnel.
            </p>
          </AccordionItem>
          <AccordionItem key="2" aria-label="Accordion 2" title="How much of my donation reaches the cause?">
            <p className="text-gray-600">
              100% of your donation goes directly to the cause. We cover our operational costs through platform fees paid by 
              organizations, not through deductions from donor contributions.
            </p>
          </AccordionItem>
          <AccordionItem key="3" aria-label="Accordion 3" title="Can I donate monthly?">
            <p className="text-gray-600">
              Yes! You can set up recurring donations to support ongoing campaigns. You can adjust the frequency, amount, 
              or cancel at any time through your account settings.
            </p>
          </AccordionItem>
          <AccordionItem key="4" aria-label="Accordion 4" title="Are my donations tax deductible?">
            <p className="text-gray-600">
              Tax deductibility depends on your location and the organization you're supporting. We provide detailed receipts 
              for all donations, and many of our partnered organizations offer tax-deductible contributions. Please consult 
              with a tax professional for advice specific to your situation.
            </p>
          </AccordionItem>
        </Accordion>
      </div>
    </div>
  );
}