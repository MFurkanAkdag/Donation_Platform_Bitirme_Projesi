"use client";
import * as React from "react";
import Link from "next/link";

type Variant = "primary" | "secondary" | "ghost";
type Size = "sm" | "md" | "lg";

function clsx(...classes: Array<string | false | null | undefined>) {
  return classes.filter(Boolean).join(" ");
}

const base =
  "inline-flex items-center justify-center rounded-xl font-medium transition-colors " +
  "focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-60 disabled:cursor-not-allowed";

const variants: Record<Variant, string> = {
  primary: "bg-black text-white hover:bg-neutral-800 focus:ring-black",
  secondary: "bg-white text-black border border-neutral-200 hover:bg-neutral-50 focus:ring-neutral-300",
  ghost: "bg-transparent text-black hover:bg-neutral-100 focus:ring-neutral-300",
};

const sizes: Record<Size, string> = {
  sm: "h-9 px-3 text-sm",
  md: "h-10 px-4 text-sm",
  lg: "h-11 px-5 text-base",
};

export type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  asChild?: boolean;
  variant?: Variant;
  size?: Size;
  loading?: boolean;
};

export default function Button({
  className,
  children,
  variant = "primary",
  size = "md",
  loading = false,
  ...props
}: ButtonProps) {
  return (
    <button
      className={clsx(base, variants[variant], sizes[size], className)}
      aria-busy={loading || undefined}
      {...props}
    >
      {loading ? "..." : children}
    </button>
  );
}

/** Optional: Link varyasyonu (butonu link gibi kullanmak için) */
export function ButtonLink({
  href,
  children,
  variant = "secondary",
  size = "md",
  className,
  ...rest
}: { href: string } & React.AnchorHTMLAttributes<HTMLAnchorElement> & { variant?: Variant; size?: Size }) {
  return (
    <Link
      href={href}
      className={clsx(base, variants[variant], sizes[size], className)}
      {...rest}
    >
      {children}
    </Link>
  );
}
