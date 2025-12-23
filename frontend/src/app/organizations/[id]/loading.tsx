export default function Loading() {
  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-7xl mx-auto">
        <div className="bg-white rounded-lg shadow-sm p-6 mb-8 animate-pulse">
          <div className="flex gap-6">
            <div className="w-32 h-32 bg-gray-200 rounded-lg"></div>
            <div className="flex-1">
              <div className="h-8 bg-gray-200 rounded w-64 mb-4"></div>
              <div className="h-4 bg-gray-200 rounded w-full mb-2"></div>
              <div className="h-4 bg-gray-200 rounded w-3/4"></div>
            </div>
          </div>
        </div>
        
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2 space-y-8">
            <div className="bg-white rounded-lg shadow-sm p-6 animate-pulse">
              <div className="h-6 bg-gray-200 rounded w-48 mb-4"></div>
              <div className="h-32 bg-gray-200 rounded"></div>
            </div>
          </div>
          <div>
            <div className="bg-white rounded-lg shadow-sm p-6 animate-pulse">
              <div className="h-6 bg-gray-200 rounded w-32 mb-4"></div>
              <div className="h-24 bg-gray-200 rounded"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
