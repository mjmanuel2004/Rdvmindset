"use client";

import { useSession } from "next-auth/react";
import { useEffect, useState } from "react";
import { fetchWithAuth } from "@/lib/apiClient";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

interface Appointment {
  id: string;
  dateTime: string;
  clientFirstName: string;
  clientLastName: string;
  reason: string;
}

export default function DashboardPage() {
  const { data: session, status } = useSession();
  const [analytics, setAnalytics] = useState({
    totalAppointments: 0,
    confirmedAppointments: 0,
    cancelledAppointments: 0,
    totalClients: 0
  });
  
  const [upcomingAppointments, setUpcomingAppointments] = useState<Appointment[]>([]);
  const [companyId, setCompanyId] = useState<string | null>(null);
  
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 1. Initial Data Fetching
  useEffect(() => {
    if (status === "authenticated" && session?.accessToken) {
      const token = session.accessToken as string;

      // Fetch Analytics
      fetchWithAuth("/api/v1/analytics/dashboard", token)
        .then((data) => setAnalytics(data))
        .catch((err) => {
          console.error("Failed to fetch analytics:", err);
          setError("Impossible de charger les statistiques.");
        });

      // Fetch User Profile to get companyId
      fetchWithAuth("/api/v1/users/me", token)
        .then((user) => {
          if (user.companyId) {
            setCompanyId(user.companyId);
          }
        })
        .catch((err) => console.error("Failed to fetch user profile:", err))
        .finally(() => setIsLoading(false));
        
    } else if (status === "unauthenticated") {
      setIsLoading(false);
      setError("Veuillez vous connecter pour voir vos statistiques.");
    }
  }, [status, session]);

  // 2. WebSocket Connection
  useEffect(() => {
    if (!companyId) return;

    const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
    
    const client = new Client({
      webSocketFactory: () => new SockJS(`${API_BASE_URL}/ws-endpoint`),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("Connecté au WebSocket STOMP");
        client.subscribe(`/topic/entreprise/${companyId}/rdv`, (message) => {
          if (message.body) {
            const newRdv = JSON.parse(message.body);
            
            // Animation et mise à jour de l'interface en temps réel
            setUpcomingAppointments(prev => [newRdv, ...prev].slice(0, 5)); // Garde les 5 derniers
            
            setAnalytics(prev => ({
              ...prev,
              totalAppointments: prev.totalAppointments + 1,
              confirmedAppointments: prev.confirmedAppointments + 1
            }));
          }
        });
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
      }
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  }, [companyId]);

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
        {companyId && <span style={{ fontSize: '0.8rem', color: 'var(--success)' }}>🟢 Connecté au serveur temps réel</span>}
      </div>

      <div className="grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)', gap: '1.5rem', marginBottom: '2rem' }}>
        
        {/* KPI Cards */}
        <div className="glass-panel" style={{ padding: '1.5rem', borderTop: '4px solid var(--accent-primary)', transition: 'all 0.3s ease' }}>
          <h4 style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Total Rendez-vous</h4>
          <span style={{ fontSize: '2.5rem', fontWeight: '700' }}>{analytics.totalAppointments}</span>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem', borderTop: '4px solid var(--success)', transition: 'all 0.3s ease' }}>
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

        <div className="glass-panel" style={{ minHeight: '400px', display: 'flex', flexDirection: 'column' }}>
          <h3>Prochains Rendez-vous</h3>
          <p style={{ fontSize: '0.8rem', opacity: 0.7, marginBottom: '1rem' }}>Mise à jour en direct via IA</p>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', flex: 1, overflowY: 'auto' }}>
            {upcomingAppointments.length === 0 ? (
              <p style={{ opacity: 0.5, fontSize: '0.9rem', textAlign: 'center', marginTop: '2rem' }}>En attente de nouveaux rendez-vous...</p>
            ) : (
              upcomingAppointments.map((appt, idx) => (
                <div key={idx} className="animate-fade-in" style={{ padding: '1rem', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', borderLeft: '3px solid var(--accent-primary)' }}>
                  <strong style={{ display: 'block', color: 'var(--accent-primary)' }}>
                    {new Date(appt.dateTime).toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' })}
                  </strong>
                  <span>{appt.reason || 'Consultation'} - {appt.clientFirstName} {appt.clientLastName}</span>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
