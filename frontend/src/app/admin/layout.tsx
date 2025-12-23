import AdminGuard from "@/components/admin/AdminGuard";
import AdminSidebar from "@/components/admin/AdminSidebar";

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  return (
    <AdminGuard>
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <div className="grid grid-cols-1 md:grid-cols-[250px_1fr] gap-6">
            <aside className="hidden md:block">
              <AdminSidebar />
            </aside>
            <main>{children}</main>
          </div>
        </div>
      </div>
    </AdminGuard>
  );
}
