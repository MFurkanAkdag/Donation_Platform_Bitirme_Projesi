export const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

export type Campaign = { id: string; title: string; goal: number; raised: number };

export async function getCampaigns(params?: { empty?: boolean; error?: boolean }) {
  // API bağlanana kadar MOCK: yükleme ve hata/boş senaryolarını test edeceğiz
  await sleep(800); // hafif gecikme
  if (params?.error) throw new Error("Kampanyalar yüklenemedi");
  if (params?.empty) return [] as Campaign[];

  return [
    { id: "c1", title: "Yetim Bursu", goal: 10000, raised: 4200 },
    { id: "c2", title: "Kitap Kumbarası", goal: 5000, raised: 5000 },
  ] as Campaign[];
}

export function progressPct(c: Campaign) {
  return Math.min(100, Math.round((c.raised / c.goal) * 100));
}
