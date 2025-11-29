import EmptyState from "@/components/state/EmptyState";
import { getCampaigns, progressPct } from "@/lib/utils";

export default async function Page({
  searchParams,
}: {
  searchParams?: { empty?: string; error?: string };
}) {
  const empty = searchParams?.empty === "1";
  const error = searchParams?.error === "1";

  const campaigns = await getCampaigns({ empty, error });

  return (
    <main className="mx-auto max-w-3xl p-8">
      <h1 className="text-2xl font-semibold">Kampanyalar</h1>

      {campaigns.length === 0 ? (
        <div className="mt-6">
          <EmptyState title="Hiç kampanya yok" hint="Yeni kampanyalar yakında eklenecek." />
        </div>
      ) : (
        <ul className="mt-6 grid gap-4">
          {campaigns.map((c) => (
            <li key={c.id} className="rounded-xl border p-4">
              <div className="flex items-center justify-between">
                <p className="font-medium">{c.title}</p>
                <span className="text-sm text-neutral-500">
                  {c.raised} / {c.goal} ₺
                </span>
              </div>
              <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-neutral-200">
                <div
                  className="h-full bg-black"
                  style={{ width: `${progressPct(c)}%` }}
                />
              </div>
              <p className="mt-1 text-xs text-neutral-500">{progressPct(c)}%</p>
            </li>
          ))}
        </ul>
      )}
    </main>
  );
}
