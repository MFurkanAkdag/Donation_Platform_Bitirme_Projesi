"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Navbar as HeroNavbar, NavbarBrand, NavbarContent, NavbarItem, Dropdown, DropdownTrigger, DropdownMenu, DropdownItem } from "@heroui/react";
import { Button } from "@heroui/button";
import { useCart } from "@/contexts/CartContext";
import { useAuth } from "@/contexts/AuthContext";
import { Avatar } from "@heroui/react";

export default function Navbar() {
  const pathname = usePathname();
  const { cart } = useCart();
  const { user, isAuthenticated, logout } = useAuth();

  const isActive = (path: string) => {
    return pathname === path;
  };

  const cartItemCount = cart.length;

  const handleLogout = () => {
    logout();
    // Redirect to home page
    window.location.href = "/";
  };

  return (
    <HeroNavbar
      maxWidth="xl"
      className="bg-white shadow-sm"
      classNames={{
        wrapper: "px-4 sm:px-6",
      }}
    >
      <NavbarBrand>
        <Link href="/" className="flex items-center gap-2">
          <div className="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-lg">❤️</span>
          </div>
          <span className="font-bold text-xl text-gray-900 hidden sm:block">
            DonationHub
          </span>
        </Link>
      </NavbarBrand>

      <NavbarContent className="hidden sm:flex gap-6" justify="center">
        <NavbarItem>
          <Link
            href="/"
            className={`text-base font-medium transition-colors ${
              isActive("/")
                ? "text-blue-600"
                : "text-gray-700 hover:text-blue-600"
            }`}
          >
            Home
          </Link>
        </NavbarItem>
        <NavbarItem>
          <Link
            href="/campaigns"
            className={`text-base font-medium transition-colors ${
              isActive("/campaigns")
                ? "text-blue-600"
                : "text-gray-700 hover:text-blue-600"
            }`}
          >
            Campaigns
          </Link>
        </NavbarItem>
        <NavbarItem>
          <Link
            href="/about"
            className={`text-base font-medium transition-colors ${
              isActive("/about")
                ? "text-blue-600"
                : "text-gray-700 hover:text-blue-600"
            }`}
          >
            About
          </Link>
        </NavbarItem>
        <NavbarItem>
          <Link
            href="/transparency"
            className={`text-base font-medium transition-colors ${
              isActive("/transparency")
                ? "text-blue-600"
                : "text-gray-700 hover:text-blue-600"
            }`}
          >
            Transparency
          </Link>
        </NavbarItem>
        <NavbarItem>
          <Link
            href="/contact"
            className={`text-base font-medium transition-colors ${
              isActive("/contact")
                ? "text-blue-600"
                : "text-gray-700 hover:text-blue-600"
            }`}
          >
            Contact
          </Link>
        </NavbarItem>
      </NavbarContent>

      <NavbarContent justify="end">
        {isAuthenticated && user ? (
          <>
            <NavbarItem>
              <Dropdown>
                <DropdownTrigger>
                  <div className="flex items-center gap-2 cursor-pointer">
                    {user.avatarUrl ? (
                      <Avatar src={user.avatarUrl} size="sm" />
                    ) : (
                      <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
                        <span className="text-blue-800 font-medium">
                          {user.firstName?.charAt(0) || user.displayName?.charAt(0) || "U"}
                        </span>
                      </div>
                    )}
                    <span className="hidden md:inline text-sm font-medium">
                      {user.displayName || user.firstName}
                    </span>
                    {user.role === "admin" && (
                      <span className="hidden md:inline text-xs bg-red-100 text-red-800 px-2 py-0.5 rounded-full">
                        Admin
                      </span>
                    )}
                  </div>
                </DropdownTrigger>
                <DropdownMenu aria-label="User menu">
                  <DropdownItem key="account" href="/account">
                    Account
                  </DropdownItem>
                  <DropdownItem key="settings" href="/account/settings">
                    Settings
                  </DropdownItem>
                  <DropdownItem key="profile" href="/account/profile">
                    Profile
                  </DropdownItem>
                  <DropdownItem 
                    key="logout" 
                    onClick={handleLogout}
                    className="text-danger"
                    color="danger"
                  >
                    Log out
                  </DropdownItem>
                </DropdownMenu>
              </Dropdown>
            </NavbarItem>
          </>
        ) : (
          <>
            <NavbarItem className="hidden lg:flex">
              <Link href="/auth/login">
                <Button variant="light">Log in</Button>
              </Link>
            </NavbarItem>
            <NavbarItem>
              <Link href="/auth/register">
                <Button color="primary" variant="flat">
                  Sign up
                </Button>
              </Link>
            </NavbarItem>
          </>
        )}
        
        <NavbarItem>
          <Link href="/cart">
            <Button
              color={cartItemCount > 0 ? "primary" : "default"}
              variant={cartItemCount > 0 ? "flat" : "light"}
              className="relative"
            >
              <span className="text-lg">🛒</span>
              {cartItemCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                  {cartItemCount}
                </span>
              )}
              <span className="hidden sm:inline ml-1">Cart</span>
            </Button>
          </Link>
        </NavbarItem>
      </NavbarContent>
    </HeroNavbar>
  );
}