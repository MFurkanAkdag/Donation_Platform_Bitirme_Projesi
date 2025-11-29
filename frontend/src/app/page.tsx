"use client";
import Button from "@/components/ui/Button";

export default function Page() {
  return (
    <main className="mx-auto max-w-2xl p-8">
      <h1 className="text-3xl font-bold">Ömer’in Akademisi — Frontend İskeleti</h1>
      <p className="mt-2 text-neutral-600">Next.js + Tailwind hazır 🚀</p>

      <div className="mt-6 flex gap-3">
        <Button onClick={() => alert("Merhaba!")} variant="primary">Tıkla</Button>
        <Button variant="secondary">İkincil</Button>
        <Button variant="ghost">Ghost</Button>
      </div>
    </main>
  );
  
}
