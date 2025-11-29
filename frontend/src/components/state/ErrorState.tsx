"use client";

export default function ErrorState({
  message = "Bir şeyler ters gitti.",
  onRetry,
}: { message?: string; onRetry?: () => void }) {
  return (
    <div className="rounded-xl border border-red-200 bg-red-50 p-4">
      <p className="text-red-700 text-sm">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="mt-3 inline-flex items-center rounded-lg border px-3 py-1.5 text-sm hover:bg-white"
        >
          Tekrar Dene
        </button>
      )}
    </div>
  );
}
