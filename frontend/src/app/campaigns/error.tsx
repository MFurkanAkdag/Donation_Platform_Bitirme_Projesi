"use client";
import ErrorState from "@/components/state/ErrorState";

export default function ErrorBoundary({ error, reset }: { error: Error; reset: () => void }) {
  return (
    <main className="mx-auto max-w-3xl p-8">
      <ErrorState message={error.message} onRetry={reset} />
    </main>
  );
}
