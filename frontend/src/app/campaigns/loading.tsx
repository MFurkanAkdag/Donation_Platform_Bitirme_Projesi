import LoadingState from "@/components/state/LoadingState";

export default function Loading() {
  return (
    <main className="mx-auto max-w-3xl p-8">
      <LoadingState label="Kampanyalar yükleniyor..." />
    </main>
  );
}
