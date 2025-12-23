"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Card, CardBody } from "@heroui/react";

const navItems = [
  { href: '/org/dashboard', label: 'Dashboard', icon: '📊' },
  { href: '/org/profile', label: 'Profile', icon: '🏢' },
  { href: '/org/contact', label: 'Contacts', icon: '📞' },
  { href: '/org/banking', label: 'Banking', icon: '🏦' },
  { href: '/org/documents', label: 'Documents', icon: '📄' },
  { href: '/org/campaigns', label: 'Campaigns', icon: '🎯' },
];

export default function OrgSidebar() {
  const pathname = usePathname();

  return (
    <Card className="h-full">
      <CardBody className="p-4">
        <nav className="space-y-1">
          {navItems.map((item) => {
            const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                  isActive
                    ? 'bg-blue-50 text-blue-600 font-medium'
                    : 'text-gray-700 hover:bg-gray-100'
                }`}
              >
                <span className="text-xl">{item.icon}</span>
                <span>{item.label}</span>
              </Link>
            );
          })}
        </nav>
      </CardBody>
    </Card>
  );
}
