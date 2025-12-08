"use client";

import { Card, CardBody, CardHeader } from "@heroui/react";
import { Chip } from "@heroui/react";
import { Tabs, Tab } from "@heroui/react";
import { Progress } from "@heroui/react";

export default function TransparencyPage() {
  // Mock data for transparency examples
  const organizations = [
    {
      id: 1,
      name: "International Relief Fund",
      transparencyScore: 94,
      campaignCount: 12,
      completedCampaigns: 8,
      pendingReports: 2
    },
    {
      id: 2,
      name: "Hope for Children Foundation",
      transparencyScore: 87,
      campaignCount: 8,
      completedCampaigns: 6,
      pendingReports: 1
    },
    {
      id: 3,
      name: "Clean Water Initiative",
      transparencyScore: 91,
      campaignCount: 15,
      completedCampaigns: 12,
      pendingReports: 0
    }
  ];

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-extrabold text-gray-900 sm:text-5xl">
            Transparency & Accountability
          </h1>
          <p className="mt-4 text-xl text-gray-600">
            Our commitment to full transparency in charitable giving
          </p>
        </div>

        <div className="bg-white rounded-lg shadow-lg overflow-hidden mb-12">
          <div className="px-6 py-8 sm:p-10">
            <div className="prose prose-lg mx-auto text-gray-600">
              <p className="text-lg">
                At DonationHub, we believe that transparency is the cornerstone of effective philanthropy. 
                We've built a comprehensive system to ensure that every donation can be tracked, every impact 
                can be measured, and every donor can see exactly how their contribution is making a difference.
              </p>
              
              <h2 className="text-2xl font-bold text-gray-900 mt-8">Our Transparency Model</h2>
              <p>
                Our platform implements a robust transparency framework based on our database concepts, 
                including <code className="bg-gray-100 px-1 rounded">transparency_scores</code>, 
                <code className="bg-gray-100 px-1 rounded">transparency_score_history</code>, 
                <code className="bg-gray-100 px-1 rounded">evidences</code>, and 
                <code className="bg-gray-100 px-1 rounded">evidence_documents</code>. This system ensures 
                accountability at every level of our operations.
              </p>
              
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-8">
                <div className="border border-gray-200 rounded-lg p-6">
                  <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center mb-4">
                    <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                    </svg>
                  </div>
                  <h3 className="text-lg font-medium text-gray-900">Verified Organizations</h3>
                  <p className="mt-2 text-gray-600">
                    Every organization must undergo rigorous verification before launching campaigns.
                  </p>
                </div>
                
                <div className="border border-gray-200 rounded-lg p-6">
                  <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center mb-4">
                    <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                  </div>
                  <h3 className="text-lg font-medium text-gray-900">Impact Reporting</h3>
                  <p className="mt-2 text-gray-600">
                    Regular impact reports with evidence documents showing how funds were utilized.
                  </p>
                </div>
                
                <div className="border border-gray-200 rounded-lg p-6">
                  <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center mb-4">
                    <svg className="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                    </svg>
                  </div>
                  <h3 className="text-lg font-medium text-gray-900">Fraud Protection</h3>
                  <p className="mt-2 text-gray-600">
                    Advanced monitoring systems to detect and prevent fraudulent activities.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <Tabs aria-label="Transparency Information" className="mb-12">
          <Tab key="score" title="Transparency Score Calculation">
            <Card>
              <CardBody className="p-6">
                <div className="prose max-w-none">
                  <h3 className="text-xl font-bold text-gray-900">How We Calculate Transparency Scores</h3>
                  <p className="mt-2 text-gray-600">
                    Our proprietary algorithm evaluates organizations based on multiple factors to assign a 
                    transparency score between 0-100.
                  </p>
                  
                  <div className="mt-6 grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <h4 className="font-medium text-gray-900">Financial Transparency (40%)</h4>
                      <ul className="mt-2 list-disc pl-5 space-y-1 text-gray-600">
                        <li>Public financial statements</li>
                        <li>Independent audits</li>
                        <li>Budget allocation disclosure</li>
                        <li>Executive compensation transparency</li>
                      </ul>
                    </div>
                    
                    <div>
                      <h4 className="font-medium text-gray-900">Impact Reporting (30%)</h4>
                      <ul className="mt-2 list-disc pl-5 space-y-1 text-gray-600">
                        <li>Regular progress updates</li>
                        <li>Photo/video documentation</li>
                        <li>Beneficiary testimonials</li>
                        <li>Third-party verification</li>
                      </ul>
                    </div>
                    
                    <div>
                      <h4 className="font-medium text-gray-900">Governance (20%)</h4>
                      <ul className="mt-2 list-disc pl-5 space-y-1 text-gray-600">
                        <li>Board composition</li>
                        <li>Conflict of interest policies</li>
                        <li>Ethical guidelines</li>
                        <li>Complaint handling procedures</li>
                      </ul>
                    </div>
                    
                    <div>
                      <h4 className="font-medium text-gray-900">Platform Engagement (10%)</h4>
                      <ul className="mt-2 list-disc pl-5 space-y-1 text-gray-600">
                        <li>Response time to donor inquiries</li>
                        <li>Community engagement</li>
                        <li>Feedback incorporation</li>
                      </ul>
                    </div>
                  </div>
                  
                  <div className="mt-8 p-4 bg-blue-50 rounded-lg">
                    <h4 className="font-medium text-blue-800">Score Updates</h4>
                    <p className="mt-1 text-blue-700">
                      Transparency scores are recalculated monthly based on the latest available data and 
                      organization activity. Significant changes are reviewed manually before updating.
                    </p>
                  </div>
                </div>
              </CardBody>
            </Card>
          </Tab>
          
          <Tab key="examples" title="Evidence Documents">
            <Card>
              <CardBody className="p-6">
                <div className="prose max-w-none">
                  <h3 className="text-xl font-bold text-gray-900">Types of Evidence Documents</h3>
                  <p className="mt-2 text-gray-600">
                    Organizations are required to submit various forms of evidence to demonstrate impact 
                    and fund utilization.
                  </p>
                  
                  <div className="mt-6 grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div className="border border-gray-200 rounded-lg p-4">
                      <div className="w-10 h-10 bg-yellow-100 rounded-lg flex items-center justify-center mb-3">
                        <svg className="w-5 h-5 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                      </div>
                      <h4 className="font-medium text-gray-900">Financial Reports</h4>
                      <p className="mt-2 text-sm text-gray-600">
                        Detailed breakdowns of fund allocation and expenditure with receipts.
                      </p>
                    </div>
                    
                    <div className="border border-gray-200 rounded-lg p-4">
                      <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center mb-3">
                        <svg className="w-5 h-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                      </div>
                      <h4 className="font-medium text-gray-900">Photo Documentation</h4>
                      <p className="mt-2 text-sm text-gray-600">
                        Before, during, and after images showing project implementation.
                      </p>
                    </div>
                    
                    <div className="border border-gray-200 rounded-lg p-4">
                      <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center mb-3">
                        <svg className="w-5 h-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
                        </svg>
                      </div>
                      <h4 className="font-medium text-gray-900">Impact Stories</h4>
                      <p className="mt-2 text-sm text-gray-600">
                        Testimonials and case studies from beneficiaries and community members.
                      </p>
                    </div>
                  </div>
                  
                  <div className="mt-8 p-4 bg-green-50 rounded-lg">
                    <h4 className="font-medium text-green-800">Verification Process</h4>
                    <p className="mt-1 text-green-700">
                      All evidence documents are reviewed by our verification team and, when possible, 
                      cross-referenced with third-party sources to ensure authenticity and accuracy.
                    </p>
                  </div>
                </div>
              </CardBody>
            </Card>
          </Tab>
          
          <Tab key="reporting" title="Reporting Violations">
            <Card>
              <CardBody className="p-6">
                <div className="prose max-w-none">
                  <h3 className="text-xl font-bold text-gray-900">Reporting Concerns or Violations</h3>
                  <p className="mt-2 text-gray-600">
                    We take all reports seriously and investigate them thoroughly to maintain the integrity 
                    of our platform.
                  </p>
                  
                  <div className="mt-6">
                    <h4 className="font-medium text-gray-900">How to Report</h4>
                    <ol className="mt-2 list-decimal pl-5 space-y-2 text-gray-600">
                      <li>
                        <strong>Through the Platform:</strong> Use the "Report" button on any campaign or 
                        organization page to submit concerns directly through our system
                      </li>
                      <li>
                        <strong>Via Email:</strong> Send detailed information to 
                        transparency@donationhub.org with subject line "TRANSPARENCY CONCERN"
                      </li>
                      <li>
                        <strong>By Phone:</strong> Call our Transparency Hotline at +1-800-DONATE-1 
                        (available 24/7)
                      </li>
                    </ol>
                  </div>
                  
                  <div className="mt-8">
                    <h4 className="font-medium text-gray-900">What Happens After You Report</h4>
                    <div className="mt-4 space-y-4">
                      <div className="flex">
                        <div className="flex-shrink-0">
                          <div className="flex items-center justify-center h-8 w-8 rounded-full bg-blue-100">
                            <span className="text-blue-800 text-sm font-medium">1</span>
                          </div>
                        </div>
                        <div className="ml-3">
                          <p className="text-gray-600">
                            <strong>Initial Review:</strong> Our team reviews your report within 24 hours 
                            to assess urgency and required action
                          </p>
                        </div>
                      </div>
                      
                      <div className="flex">
                        <div className="flex-shrink-0">
                          <div className="flex items-center justify-center h-8 w-8 rounded-full bg-blue-100">
                            <span className="text-blue-800 text-sm font-medium">2</span>
                          </div>
                        </div>
                        <div className="ml-3">
                          <p className="text-gray-600">
                            <strong>Investigation:</strong> Thorough investigation involving document review, 
                            third-party verification, and stakeholder interviews
                          </p>
                        </div>
                      </div>
                      
                      <div className="flex">
                        <div className="flex-shrink-0">
                          <div className="flex items-center justify-center h-8 w-8 rounded-full bg-blue-100">
                            <span className="text-blue-800 text-sm font-medium">3</span>
                          </div>
                        </div>
                        <div className="ml-3">
                          <p className="text-gray-600">
                            <strong>Action:</strong> Based on findings, we may require additional evidence, 
                            suspend campaigns, or terminate partnerships
                          </p>
                        </div>
                      </div>
                      
                      <div className="flex">
                        <div className="flex-shrink-0">
                          <div className="flex items-center justify-center h-8 w-8 rounded-full bg-blue-100">
                            <span className="text-blue-800 text-sm font-medium">4</span>
                          </div>
                        </div>
                        <div className="ml-3">
                          <p className="text-gray-600">
                            <strong>Communication:</strong> You'll receive updates on the investigation 
                            progress and final resolution (while respecting privacy)
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                  
                  <div className="mt-8 p-4 bg-red-50 rounded-lg">
                    <h4 className="font-medium text-red-800">Protection for Whistleblowers</h4>
                    <p className="mt-1 text-red-700">
                      We maintain strict confidentiality for reporters and have zero tolerance for 
                      retaliation against anyone who reports concerns in good faith.
                    </p>
                  </div>
                </div>
              </CardBody>
            </Card>
          </Tab>
        </Tabs>

        <div className="mb-12">
          <h2 className="text-2xl font-bold text-gray-900 mb-6">Organization Transparency Examples</h2>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {organizations.map((org) => (
              <Card key={org.id}>
                <CardHeader className="pb-4">
                  <div className="flex justify-between items-start">
                    <h3 className="text-lg font-bold text-gray-900">{org.name}</h3>
                    <Chip color={org.transparencyScore >= 90 ? "success" : org.transparencyScore >= 80 ? "warning" : "default"}>
                      {org.transparencyScore}%
                    </Chip>
                  </div>
                </CardHeader>
                <CardBody className="p-6">
                  <div className="space-y-4">
                    <div>
                      <div className="flex justify-between text-sm text-gray-600 mb-1">
                        <span>Transparency Score</span>
                        <span>{org.transparencyScore}%</span>
                      </div>
                      <Progress 
                        value={org.transparencyScore} 
                        color={org.transparencyScore >= 90 ? "success" : org.transparencyScore >= 80 ? "warning" : "default"}
                        className="h-2"
                      />
                    </div>
                    
                    <div className="grid grid-cols-2 gap-4 text-center">
                      <div>
                        <p className="text-2xl font-bold text-gray-900">{org.campaignCount}</p>
                        <p className="text-sm text-gray-600">Campaigns</p>
                      </div>
                      <div>
                        <p className="text-2xl font-bold text-gray-900">{org.completedCampaigns}</p>
                        <p className="text-sm text-gray-600">Completed</p>
                      </div>
                    </div>
                    
                    <div className="pt-2">
                      <p className="text-sm text-gray-600">
                        {org.pendingReports > 0 ? (
                          <span className="text-orange-600">
                            {org.pendingReports} report{org.pendingReports > 1 ? 's' : ''} pending
                          </span>
                        ) : (
                          <span className="text-green-600">All reports submitted</span>
                        )}
                      </p>
                    </div>
                  </div>
                </CardBody>
              </Card>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}