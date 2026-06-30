import { Link, useLocation } from "wouter";
import { LayoutDashboard, Users, Gauge, ReceiptText, Zap } from "lucide-react";
import { cn } from "@/lib/utils";

const navItems = [
  { name: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
  { name: "Customers", href: "/customers", icon: Users },
  { name: "Meter Readings", href: "/meter-readings", icon: Gauge },
  { name: "Bills", href: "/bills", icon: ReceiptText },
  { name: "Tariffs", href: "/tariffs", icon: Zap },
];

export function Layout({ children }: { children: React.ReactNode }) {
  const [location] = useLocation();

  return (
    <div className="flex min-h-screen w-full flex-col lg:flex-row bg-background">
      <aside className="w-full lg:w-64 bg-sidebar text-sidebar-foreground border-r border-sidebar-border flex-shrink-0">
        <div className="p-6">
          <div className="flex items-center gap-2 font-bold text-xl tracking-tight">
            <Zap className="h-6 w-6 text-sidebar-primary" />
            <span>VoltGrid Admin</span>
          </div>
        </div>
        <nav className="flex-1 px-4 py-4 space-y-1">
          {navItems.map((item) => {
            const isActive = location.startsWith(item.href);
            return (
              <Link key={item.name} href={item.href}>
                <div
                  className={cn(
                    "flex items-center gap-3 px-3 py-2.5 rounded-md text-sm font-medium transition-colors cursor-pointer",
                    isActive 
                      ? "bg-sidebar-accent text-sidebar-accent-foreground" 
                      : "text-sidebar-foreground/70 hover:bg-sidebar-accent/50 hover:text-sidebar-foreground"
                  )}
                >
                  <item.icon className="h-4 w-4" />
                  {item.name}
                </div>
              </Link>
            );
          })}
        </nav>
      </aside>
      <main className="flex-1 overflow-auto">
        <div className="p-6 md:p-8 max-w-7xl mx-auto">
          {children}
        </div>
      </main>
    </div>
  );
}
