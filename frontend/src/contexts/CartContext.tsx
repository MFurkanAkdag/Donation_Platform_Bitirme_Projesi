"use client";

import { createContext, useContext, useState, useEffect, ReactNode } from "react";
import { CartItem } from "@/lib/mockData";

interface CartContextType {
  cart: CartItem[];
  addToCart: (campaignId: string, amount: number) => void;
  removeFromCart: (campaignId: string) => void;
  updateCartItem: (campaignId: string, amount: number) => void;
  clearCart: () => void;
  getTotalAmount: () => number;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export function CartProvider({ children }: { children: ReactNode }) {
  const [cart, setCart] = useState<CartItem[]>([]);

  // Load cart from localStorage on mount
  useEffect(() => {
    const savedCart = localStorage.getItem("donationCart");
    if (savedCart) {
      setCart(JSON.parse(savedCart));
    }
  }, []);

  // Save cart to localStorage whenever it changes
  useEffect(() => {
    localStorage.setItem("donationCart", JSON.stringify(cart));
  }, [cart]);

  const addToCart = (campaignId: string, amount: number) => {
    setCart((prev) => {
      const existing = prev.find((item) => item.campaignId === campaignId);
      if (existing) {
        return prev.map((item) =>
          item.campaignId === campaignId
            ? { ...item, amount: item.amount + amount }
            : item
        );
      }
      return [...prev, { campaignId, amount }];
    });
  };

  const removeFromCart = (campaignId: string) => {
    setCart((prev) => prev.filter((item) => item.campaignId !== campaignId));
  };

  const updateCartItem = (campaignId: string, amount: number) => {
    if (amount <= 0) {
      removeFromCart(campaignId);
      return;
    }
    setCart((prev) =>
      prev.map((item) =>
        item.campaignId === campaignId ? { ...item, amount } : item
      )
    );
  };

  const clearCart = () => {
    setCart([]);
  };

  const getTotalAmount = () => {
    return cart.reduce((total, item) => total + item.amount, 0);
  };

  return (
    <CartContext.Provider
      value={{
        cart,
        addToCart,
        removeFromCart,
        updateCartItem,
        clearCart,
        getTotalAmount,
      }}
    >
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error("useCart must be used within a CartProvider");
  }
  return context;
}
