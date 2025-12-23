"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Card, CardBody } from "@heroui/react";

const navItems = [
  { href: '/admin', label: 'Dashboard', icon: '📊', exact: true },
  { href: '/admin/users', label: 'Users', icon: '👥' },
  { href: '/admin/organizations', label: 'Organizations', icon: '🏢' },
  { href: '/admin/campaigns', label: 'Campaigns', icon: '🎯' },
  { href: '/admin/donations', label: 'Donations', icon: '💰' },
  { href: '/admin/evidences', label: 'Evidences', icon: '📋' },
  { href: '/admin/reports', label: 'Reports', icon: '⚠️' },
  { href: '/admin/settings', label: 'Settings', icon: '⚙️' },
  { href: '/admin/audit-logs', label: 'Audit Logs', icon: '📜' },
];

export default function AdminSidebar() {
  const pathname = usePathname();

  return (
    <Card className="h-full">
      <CardBody className="p-4">
        <div className="mb-4">
          <h2 className="text-lg font-bold text-gray-900">Admin Panel</h2>
        </div>
        <nav className="space-y-1">
          {navItems.map((item) => {
            const isActive = item.exact 
              ? pathname === item.href 
              : pathname.startsWith(item.href);
            
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
