"use client";

import { Card, CardBody } from "@heroui/react";

export default function TermsOfServicePage() {
  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-extrabold text-gray-900 sm:text-5xl">
            Terms of Service
          </h1>
          <p className="mt-4 text-xl text-gray-600">
            Last updated: December 8, 2025
          </p>
        </div>

        <Card>
          <CardBody className="p-6 sm:p-10">
            <div className="prose prose-lg mx-auto text-gray-600">
              <p className="text-lg">
                Welcome to DonationHub. These Terms of Service ("Terms") govern your access to and use of 
                our website and services. By accessing or using our platform, you agree to be bound by these Terms.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">1. Introduction & Definitions</h2>
              
              <p className="mt-4">
                <strong>DonationHub</strong> refers to our platform that connects donors with verified 
                nonprofit organizations and charitable campaigns.
              </p>
              
              <p className="mt-2">
                <strong>User</strong> refers to any individual or entity that accesses or uses our platform, 
                including donors, foundations, beneficiaries, and administrators.
              </p>
              
              <p className="mt-2">
                <strong>Campaign</strong> refers to a fundraising initiative created by a verified organization 
                to collect donations for a specific cause.
              </p>
              
              <p className="mt-2">
                <strong>Donation</strong> refers to monetary contributions made by users to support campaigns.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">2. User Responsibilities</h2>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Donors</h3>
              <ul className="list-disc pl-6 mt-2 space-y-2">
                <li>Provide accurate and complete information when registering an account</li>
                <li>Use the platform in accordance with applicable laws and regulations</li>
                <li>Respect the rights and privacy of other users</li>
                <li>Report any suspicious or fraudulent activities</li>
                <li>Understand that donations are non-refundable except as required by law</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Foundations/Organizations</h3>
              <ul className="list-disc pl-6 mt-2 space-y-2">
                <li>Maintain valid legal status and charitable registration</li>
                <li>Provide truthful and complete information about campaigns and fund usage</li>
                <li>Comply with all applicable laws and regulations in their jurisdiction</li>
                <li>Submit regular impact reports and financial statements</li>
                <li>Respond promptly to donor inquiries and concerns</li>
                <li>Use donated funds exclusively for stated campaign purposes</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Beneficiaries</h3>
              <ul className="list-disc pl-6 mt-2 space-y-2">
                <li>Provide accurate information about their needs and circumstances</li>
                <li>Use received funds appropriately and ethically</li>
                <li>Cooperate with foundations and DonationHub for impact verification</li>
                <li>Report on fund utilization as required</li>
              </ul>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">3. Campaign Rules & Fraud Policy</h2>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Campaign Creation</h3>
              <ul className="list-disc pl-6 mt-2 space-y-2">
                <li>All campaigns must be created by verified organizations</li>
                <li>Campaigns must have a clear, specific purpose and measurable goals</li>
                <li>Fundraising targets must be realistic and justified</li>
                <li>Organizations must provide evidence of their capacity to execute the campaign</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Fraud Prevention</h3>
              <ul className="list-disc pl-6 mt-2 space-y-2">
                <li>We employ advanced verification techniques to detect fraudulent campaigns</li>
                <li>Suspicious activities are investigated and reported to authorities when necessary</li>
                <li>Users who engage in fraudulent activities will have their accounts suspended or terminated</li>
                <li>Donors who report suspected fraud are protected from retaliation</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Transparency Requirements</h3>
              <ul className="list-disc pl-6 mt-2 space-y-2">
                <li>All campaigns must provide regular updates on fund utilization</li>
                <li>Organizations must submit evidence of impact within 30 days of campaign completion</li>
                <li>Failure to meet transparency requirements may result in account restrictions</li>
              </ul>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">4. Payment & Refunds</h2>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Payment Processing</h3>
              <ul className="list-disc pl-6 mt-2 space-y-2">
                <li>All payments are processed through secure third-party payment processors</li>
                <li>DonationHub does not store credit card or banking information</li>
                <li>Transaction fees are clearly disclosed before donation completion</li>
                <li>Payments are transferred to organizations within 7 business days of receipt</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Refund Policy</h3>
              <ul className="list-disc pl-6 mt-2 space-y-2">
                <li>Donations are generally non-refundable due to the immediate transfer to charitable causes</li>
                <li>Exceptions may be made in cases of demonstrable fraud or platform error</li>
                <li>Refund requests must be submitted within 30 days of donation</li>
                <li>Organizations may establish their own refund policies for specific campaigns</li>
              </ul>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">5. Termination & Suspension</h2>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">By DonationHub</h3>
              <p className="mt-2">
                We reserve the right to suspend or terminate accounts for violations of these Terms, 
                including but not limited to:
              </p>
              <ul className="list-disc pl-6 mt-2 space-y-2">
                <li>Engaging in fraudulent or deceptive activities</li>
                <li>Violating applicable laws or regulations</li>
                <li>Harassing or threatening other users</li>
                <li>Repeatedly submitting false or misleading information</li>
                <li>Attempting to manipulate the platform or its systems</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">By Users</h3>
              <p className="mt-2">
                Users may deactivate their accounts at any time through account settings. 
                Note that deactivation does not erase information required for legal or accounting purposes.
              </p>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Effect of Termination</h3>
              <ul className="list-disc pl-6 mt-2 space-y-2">
                <li>Users lose access to their accounts and associated features</li>
                <li>Outstanding donations will still be processed according to campaign terms</li>
                <li>Verified organizations will be given notice to transition ongoing campaigns</li>
              </ul>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">6. Limitation of Liability</h2>
              
              <p className="mt-4">
                To the fullest extent permitted by law, DonationHub shall not be liable for any indirect, 
                incidental, special, consequential, or punitive damages arising from:
              </p>
              <ul className="list-disc pl-6 mt-2 space-y-2">
                <li>Use or inability to use our platform or services</li>
                <li>Unauthorized access to or alteration of your transmissions or data</li>
                <li>Statements or conduct of any third party on our platform</li>
                <li>Any other matter relating to our services</li>
              </ul>
              
              <p className="mt-4">
                Our total liability for any claims arising from these Terms shall not exceed the amount 
                of donations processed through our platform in the preceding 12 months.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">7. Dispute Resolution</h2>
              
              <p className="mt-4">
                Any disputes arising from these Terms shall be resolved through binding arbitration 
                in accordance with the rules of the International Chamber of Commerce. The arbitration 
                shall take place in Geneva, Switzerland, and the language of arbitration shall be English.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">8. Modifications to Terms</h2>
              
              <p className="mt-4">
                We reserve the right to modify these Terms at any time. Material changes will be 
                communicated through email and platform notifications. Continued use of our services 
                after such modifications constitutes acceptance of the revised Terms.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">9. Governing Law</h2>
              
              <p className="mt-4">
                These Terms shall be governed by and construed in accordance with the laws of Switzerland, 
                without regard to its conflict of law provisions.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">10. Contact Information</h2>
              
              <p className="mt-4">
                If you have any questions about these Terms of Service, please contact us at:
              </p>
              
              <div className="mt-4 bg-gray-50 p-4 rounded-md">
                <p className="font-medium">DonationHub Legal Team</p>
                <p>Email: legal@donationhub.org</p>
                <p>Address: 123 Charity Avenue, Suite 100, Compassion City, CC 12345</p>
              </div>
            </div>
          </CardBody>
        </Card>
      </div>
    </div>
  );
}