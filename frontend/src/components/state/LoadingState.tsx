export default function LoadingState({ label = "Yükleniyor..." }: { label?: string }) {
  return (
    <div className="flex items-center gap-2 text-neutral-600">
      <span className="animate-pulse inline-block h-2 w-2 rounded-full bg-neutral-400" />
      <span className="animate-pulse inline-block h-2 w-2 rounded-full bg-neutral-400" />
      <span className="animate-pulse inline-block h-2 w-2 rounded-full bg-neutral-400" />
      <span className="ml-2">{label}</span>
    </div>
  );
}
