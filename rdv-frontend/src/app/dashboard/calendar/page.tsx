"use client";

import { useSession } from "next-auth/react";
import { useEffect, useState } from "react";
import { fetchWithAuth } from "@/lib/apiClient";

interface Appointment {
  id: string;
  dateTime: string;
  durationMinutes: number;
  clientFirstName: string;
  clientLastName: string;
  clientPhone: string;
  reason: string;
  status: string;
}

export default function CalendarPage() {
  const { data: session, status } = useSession();
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (status === "authenticated" && session?.accessToken) {
      fetchWithAuth("/api/v1/appointments", session.accessToken as string)
        .then((data) => setAppointments(data))
        .catch((err) => {
          console.error("Failed to fetch appointments:", err);
          setError("Impossible de charger l'agenda.");
        })
        .finally(() => setIsLoading(false));
    } else if (status === "unauthenticated") {
      setIsLoading(false);
      setError("Veuillez vous connecter pour voir l'agenda.");
    }
  }, [status, session]);

  // Regroupement par date (ex: "lundi 15 juin 2026")
  const groupedAppointments = appointments.reduce((acc, appt) => {
    const dateStr = new Date(appt.dateTime).toLocaleDateString('fr-FR', {
      weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });
    if (!acc[dateStr]) acc[dateStr] = [];
    acc[dateStr].push(appt);
    return acc;
  }, {} as Record<string, Appointment[]>);

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '300px' }}>
        <p className="animate-fade-in" style={{ opacity: 0.5 }}>Chargement de votre agenda...</p>
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
        <h1>Mon Agenda</h1>
        <p>Vos prochains rendez-vous pris par l'IA ou vos équipes.</p>
      </div>

      {Object.keys(groupedAppointments).length === 0 ? (
        <div className="glass-panel" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
          <h3>Aucun rendez-vous à venir 🏖️</h3>
          <p style={{ color: 'var(--text-secondary)', marginTop: '1rem' }}>
            Votre agenda est vide pour le moment. Laissez l'IA travailler pour vous !
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
          {Object.entries(groupedAppointments).map(([dateLabel, appts], index) => (
            <div key={index} className="animate-fade-in" style={{ animationDelay: `${index * 0.1}s` }}>
              
              <h3 style={{ 
                fontSize: '1.2rem', 
                color: 'var(--accent-primary)', 
                borderBottom: '1px solid var(--glass-border)',
                paddingBottom: '0.5rem',
                marginBottom: '1rem',
                textTransform: 'capitalize'
              }}>
                {dateLabel}
              </h3>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {appts.map((appt) => {
                  const dateObj = new Date(appt.dateTime);
                  const startTime = dateObj.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
                  const endDateObj = new Date(dateObj.getTime() + appt.durationMinutes * 60000);
                  const endTime = endDateObj.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });

                  return (
                    <div key={appt.id} className="glass-panel" style={{ 
                      display: 'flex', 
                      alignItems: 'center',
                      padding: '1rem 1.5rem',
                      borderLeft: appt.status === 'CANCELLED' ? '4px solid var(--danger)' : '4px solid var(--success)'
                    }}>
                      
                      <div style={{ minWidth: '120px', fontWeight: 'bold', fontSize: '1.1rem' }}>
                        {startTime} - {endTime}
                      </div>
                      
                      <div style={{ flex: 1 }}>
                        <div style={{ fontSize: '1.1rem', fontWeight: '600' }}>
                          {appt.clientFirstName} {appt.clientLastName}
                        </div>
                        <div style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '0.2rem' }}>
                          📞 {appt.clientPhone} &bull; {appt.reason || 'Consultation'}
                        </div>
                      </div>

                      <div>
                        {appt.status === 'CANCELLED' ? (
                          <span style={{ padding: '4px 12px', background: 'rgba(239, 68, 68, 0.2)', color: 'var(--danger)', borderRadius: '20px', fontSize: '0.8rem', fontWeight: 'bold' }}>
                            ANNULÉ
                          </span>
                        ) : (
                          <span style={{ padding: '4px 12px', background: 'rgba(16, 185, 129, 0.2)', color: 'var(--success)', borderRadius: '20px', fontSize: '0.8rem', fontWeight: 'bold' }}>
                            CONFIRMÉ
                          </span>
                        )}
                      </div>

                    </div>
                  );
                })}
              </div>

            </div>
          ))}
        </div>
      )}
    </div>
  );
}
