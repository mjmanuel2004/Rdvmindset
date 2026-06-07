import type { Metadata } from "next";
import "./globals.css";

import SessionProviderWrapper from "@/components/SessionProviderWrapper";

export const metadata: Metadata = {
  title: "RdvMindset | Tableau de Bord",
  description: "Gérez vos rendez-vous et votre Agent IA en toute simplicité.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="fr">
      <body>
        <SessionProviderWrapper>
          <main className="container">
            {children}
          </main>
        </SessionProviderWrapper>
      </body>
    </html>
  );
}
