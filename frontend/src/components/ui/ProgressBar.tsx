interface ProgressBarProps {
  current: number;
  target: number;
  className?: string;
}

export default function ProgressBar({ current, target, className = "" }: ProgressBarProps) {
  const percentage = Math.min((current / target) * 100, 100);

  return (
    <div className={`w-full ${className}`}>
      <div className="flex justify-between text-sm mb-2">
        <span className="text-gray-700 font-medium">
          ${current.toLocaleString()} raised
        </span>
        <span className="text-gray-600">
          ${target.toLocaleString()} goal
        </span>
      </div>
      <div className="w-full bg-gray-200 rounded-full h-2.5 overflow-hidden">
        <div
          className="bg-blue-600 h-2.5 rounded-full transition-all duration-500 ease-out"
          style={{ width: `${percentage}%` }}
        />
      </div>
      <div className="text-sm text-gray-600 mt-1">
        {percentage.toFixed(1)}% funded
      </div>
    </div>
  );
}
