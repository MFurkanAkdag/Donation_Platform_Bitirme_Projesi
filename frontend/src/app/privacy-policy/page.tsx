"use client";

import { Card, CardBody, CardHeader } from "@heroui/react";

export default function PrivacyPolicyPage() {
  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-12">
          <h1 className="text-4xl font-extrabold text-gray-900 sm:text-5xl">
            Privacy Policy
          </h1>
          <p className="mt-4 text-xl text-gray-600">
            Last updated: December 8, 2025
          </p>
        </div>

        <Card>
          <CardBody className="p-6 sm:p-10">
            <div className="prose prose-lg mx-auto text-gray-600">
              <p className="text-lg">
                This Privacy Policy describes how DonationHub ("we", "our", or "us") collects, uses, 
                and shares your personal information when you use our website and services.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">Information We Collect</h2>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Account Information</h3>
              <p>
                When you create an account, we collect information such as your name, email address, 
                password, and role (donor, foundation, beneficiary, or admin). This information is stored 
                in our <code className="bg-gray-100 px-1 rounded">users</code> and 
                <code className="bg-gray-100 px-1 rounded">user_profiles</code> tables.
              </p>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Sensitive Data</h3>
              <p>
                For compliance with regulations such as GDPR and KVKK, we collect and securely store 
                sensitive personal data in encrypted form in our 
                <code className="bg-gray-100 px-1 rounded">user_sensitive_data</code> table. 
                This may include identification documents, financial information, and other personally 
                identifiable information required for verification purposes.
              </p>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Preferences and Settings</h3>
              <p>
                We store your notification preferences, privacy settings, and communication choices in 
                our <code className="bg-gray-100 px-1 rounded">user_preferences</code> table to 
                customize your experience.
              </p>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Activity and Security Data</h3>
              <p>
                To protect your account and ensure platform security, we maintain logs of your activity, 
                including login attempts, IP addresses, and device information in our 
                <code className="bg-gray-100 px-1 rounded">login_history</code> and 
                <code className="bg-gray-100 px-1 rounded">audit_logs</code> tables.
              </p>
              
              <h3 className="text-xl font-semibold text-gray-800 mt-4">Cookies and Tracking Technologies</h3>
              <p>
                We use cookies and similar tracking technologies to enhance your browsing experience, 
                remember your preferences, and analyze platform usage. You can control cookie preferences 
                through your browser settings.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">How We Use Your Information</h2>
              
              <ul className="list-disc pl-6 mt-4 space-y-2">
                <li>To provide and maintain our services</li>
                <li>To verify your identity and prevent fraud</li>
                <li>To communicate with you about your account and donations</li>
                <li>To personalize your experience and recommend relevant campaigns</li>
                <li>To improve our platform and develop new features</li>
                <li>To comply with legal obligations and protect our rights</li>
                <li>To detect and prevent unauthorized access or malicious activities</li>
              </ul>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">Data Sharing and Disclosure</h2>
              
              <p className="mt-4">
                We do not sell, trade, or rent your personal information to third parties. We may share 
                your information in the following circumstances:
              </p>
              
              <ul className="list-disc pl-6 mt-4 space-y-2">
                <li>
                  <strong>With your consent:</strong> When you explicitly authorize us to share information 
                  with specific organizations or for specific purposes
                </li>
                <li>
                  <strong>Service providers:</strong> With trusted third-party vendors who assist us in 
                  operating our platform, conducting business, or serving our users
                </li>
                <li>
                  <strong>Legal requirements:</strong> When required by law, regulation, or legal process
                </li>
                <li>
                  <strong>Protection of rights:</strong> To protect and defend the rights or property of 
                  DonationHub or our users
                </li>
                <li>
                  <strong>Business transfers:</strong> In connection with a merger, acquisition, or sale 
                  of assets, subject to confidentiality agreements
                </li>
              </ul>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">Data Security</h2>
              
              <p className="mt-4">
                We implement robust security measures to protect your personal information, including:
              </p>
              
              <ul className="list-disc pl-6 mt-4 space-y-2">
                <li>End-to-end encryption for sensitive data transmission and storage</li>
                <li>Regular security audits and penetration testing</li>
                <li>Multi-factor authentication for administrative access</li>
                <li>Secure access controls and role-based permissions</li>
                <li>Automated monitoring for suspicious activities</li>
                <li>Data backup and disaster recovery procedures</li>
              </ul>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">Your Rights</h2>
              
              <p className="mt-4">
                Depending on your jurisdiction, you may have the following rights regarding your personal data:
              </p>
              
              <ul className="list-disc pl-6 mt-4 space-y-2">
                <li><strong>Right to Access:</strong> Request copies of your personal information</li>
                <li><strong>Right to Rectification:</strong> Correct inaccurate or incomplete information</li>
                <li><strong>Right to Erasure:</strong> Request deletion of your personal data</li>
                <li><strong>Right to Restrict Processing:</strong> Limit how we use your information</li>
                <li><strong>Right to Data Portability:</strong> Obtain and reuse your data across services</li>
                <li><strong>Right to Object:</strong> Object to processing based on legitimate interests</li>
                <li><strong>Rights related to automated decision-making:</strong> Not be subject to solely automated decisions</li>
              </ul>
              
              <p className="mt-4">
                To exercise these rights, please contact us at privacy@donationhub.org. We will respond 
                to your request within 30 days.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">Data Retention</h2>
              
              <p className="mt-4">
                We retain your personal information for as long as necessary to fulfill the purposes outlined 
                in this policy, unless a longer retention period is required by law. When data is no longer 
                needed, we securely delete it according to our retention schedules.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">International Data Transfers</h2>
              
              <p className="mt-4">
                Your information may be transferred to and maintained on computers located outside your 
                state, province, country, or other governmental jurisdiction where data protection laws may 
                differ from those in your jurisdiction. We ensure appropriate safeguards are in place for 
                such transfers.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">Children's Privacy</h2>
              
              <p className="mt-4">
                Our services are not intended for individuals under the age of 16. We do not knowingly 
                collect personal information from children. If we become aware that we have collected 
                personal information from a child, we will take steps to delete such information.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">Changes to This Privacy Policy</h2>
              
              <p className="mt-4">
                We may update this Privacy Policy from time to time. We will notify you of any changes 
                by posting the new policy on this page and updating the "Last updated" date. We encourage 
                you to review this policy periodically for any changes.
              </p>

              <h2 className="text-2xl font-bold text-gray-900 mt-8">Contact Us</h2>
              
              <p className="mt-4">
                If you have any questions about this Privacy Policy, please contact us at:
              </p>
              
              <div className="mt-4 bg-gray-50 p-4 rounded-md">
                <p className="font-medium">DonationHub Privacy Team</p>
                <p>Email: privacy@donationhub.org</p>
                <p>Address: 123 Charity Avenue, Suite 100, Compassion City, CC 12345</p>
              </div>
            </div>
          </CardBody>
        </Card>
      </div>
    </div>
  );
}