import OrgGuard from "@/components/org/OrgGuard";
import OrgSidebar from "@/components/org/OrgSidebar";

export default function OrgLayout({ children }: { children: React.ReactNode }) {
  return (
    <OrgGuard>
      <div className="min-h-screen bg-gray-50">
        <div className="container mx-auto px-4 py-8">
          <div className="grid grid-cols-1 md:grid-cols-[250px_1fr] gap-6">
            <aside className="hidden md:block">
              <OrgSidebar />
            </aside>
            <main>{children}</main>
          </div>
        </div>
      </div>
    </OrgGuard>
  );
}
