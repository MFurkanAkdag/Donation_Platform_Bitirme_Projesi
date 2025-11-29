export default function EmptyState({
  title = "Kayıt bulunamadı",
  hint,
}: { title?: string; hint?: string }) {
  return (
    <div className="rounded-xl border border-dashed p-8 text-center text-neutral-500">
      <p className="text-sm">{title}</p>
      {hint ? <p className="mt-1 text-xs">{hint}</p> : null}
    </div>
  );
}
