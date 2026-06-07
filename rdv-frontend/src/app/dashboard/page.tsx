"use client";

import { useSession } from "next-auth/react";
import { useEffect, useState } from "react";
import { fetchWithAuth } from "@/lib/apiClient";

export default function DashboardPage() {
  const { data: session, status } = useSession();
  const [analytics, setAnalytics] = useState({
    totalAppointments: 0,
    confirmedAppointments: 0,
    cancelledAppointments: 0,
    totalClients: 0
  });
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (status === "authenticated" && session?.accessToken) {
      // Fetching real data from Spring Boot API
      fetchWithAuth("/api/v1/analytics/dashboard", session.accessToken as string)
        .then((data) => {
          setAnalytics(data);
          setIsLoading(false);
        })
        .catch((err) => {
          console.error("Failed to fetch analytics:", err);
          setError("Impossible de charger les statistiques.");
          setIsLoading(false);
        });
    } else if (status === "unauthenticated") {
      setIsLoading(false);
      setError("Veuillez vous connecter pour voir vos statistiques.");
    }
  }, [status, session]);

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '300px' }}>
        <p className="animate-fade-in" style={{ opacity: 0.5 }}>Chargement des données en direct...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="glass-panel" style={{ borderLeft: '4px solid var(--danger)' }}>
        <h3 style={{ color: 'var(--danger)' }}>Erreur</h3>
        <p>{error}</p>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <div style={{ marginBottom: '2rem' }}>
        <h1>Vue d'ensemble</h1>
        <p>Bienvenue sur votre tableau de bord. Voici vos statistiques en direct.</p>
      </div>

      <div className="grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)', gap: '1.5rem', marginBottom: '2rem' }}>
        
        {/* KPI Cards */}
        <div className="glass-panel" style={{ padding: '1.5rem', borderTop: '4px solid var(--accent-primary)' }}>
          <h4 style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Total Rendez-vous</h4>
          <span style={{ fontSize: '2.5rem', fontWeight: '700' }}>{analytics.totalAppointments}</span>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem', borderTop: '4px solid var(--success)' }}>
          <h4 style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Confirmés</h4>
          <span style={{ fontSize: '2.5rem', fontWeight: '700', color: 'var(--success)' }}>{analytics.confirmedAppointments}</span>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem', borderTop: '4px solid var(--danger)' }}>
          <h4 style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Annulés</h4>
          <span style={{ fontSize: '2.5rem', fontWeight: '700', color: 'var(--danger)' }}>{analytics.cancelledAppointments}</span>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem', borderTop: '4px solid var(--warning)' }}>
          <h4 style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Clients Uniques</h4>
          <span style={{ fontSize: '2.5rem', fontWeight: '700', color: 'var(--warning)' }}>{analytics.totalClients}</span>
        </div>
      </div>

      <div className="grid" style={{ gridTemplateColumns: '2fr 1fr', gap: '1.5rem' }}>
        <div className="glass-panel" style={{ minHeight: '400px' }}>
          <h3>Activité Récente</h3>
          <p style={{ marginTop: '2rem', textAlign: 'center', opacity: 0.5 }}>Le graphique d'activité apparaîtra ici.</p>
        </div>

        <div className="glass-panel" style={{ minHeight: '400px' }}>
          <h3>Prochains Rendez-vous</h3>
          <div style={{ marginTop: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {/* Simulation of upcoming appointments until API is connected for this specific part */}
            <p style={{ opacity: 0.5, fontSize: '0.9rem' }}>Données des prochains rendez-vous bientôt disponibles via WebSockets...</p>
          </div>
        </div>
      </div>
    </div>
  );
}
