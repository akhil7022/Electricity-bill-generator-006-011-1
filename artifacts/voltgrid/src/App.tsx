import { Switch, Route, Router as WouterRouter, Redirect } from "wouter";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { Layout } from "@/components/layout";
import NotFound from "@/pages/not-found";

import Dashboard from "@/pages/dashboard";
import Customers from "@/pages/customers";
import CustomerDetail from "@/pages/customers/[id]";
import MeterReadings from "@/pages/meter-readings";
import Bills from "@/pages/bills";
import Tariffs from "@/pages/tariffs";
import SwingPreview from "@/pages/swing-preview";

const queryClient = new QueryClient();

function Router() {
  return (
    <Switch>
      <Route path="/" component={() => <Redirect to="/dashboard" />} />
      <Route path="/dashboard" component={Dashboard} />
      <Route path="/customers" component={Customers} />
      <Route path="/customers/:id" component={CustomerDetail} />
      <Route path="/meter-readings" component={MeterReadings} />
      <Route path="/bills" component={Bills} />
      <Route path="/tariffs" component={Tariffs} />
      <Route path="/swing" component={SwingPreview} />
      <Route component={NotFound} />
    </Switch>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <WouterRouter base={import.meta.env.BASE_URL.replace(/\/$/, "")}>
          <Switch>
            {/* Swing preview renders full-screen without the admin layout */}
            <Route path="/swing" component={SwingPreview} />
            {/* All other routes use the admin layout */}
            <Route>
              <Layout>
                <Router />
              </Layout>
            </Route>
          </Switch>
        </WouterRouter>
        <Toaster />
      </TooltipProvider>
    </QueryClientProvider>
  );
}

export default App;
