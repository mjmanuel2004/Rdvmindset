"use client";

import { useSession } from "next-auth/react";
import { useEffect, useState } from "react";
import { fetchWithAuth } from "@/lib/apiClient";

interface Availability {
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  maxCapacity: number;
}

const DAYS = [
  { id: 1, name: "Lundi" },
  { id: 2, name: "Mardi" },
  { id: 3, name: "Mercredi" },
  { id: 4, name: "Jeudi" },
  { id: 5, name: "Vendredi" },
  { id: 6, name: "Samedi" },
  { id: 7, name: "Dimanche" }
];

export default function AvailabilityPage() {
  const { data: session, status } = useSession();
  const [availabilities, setAvailabilities] = useState<Availability[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saveSuccess, setSaveSuccess] = useState(false);

  useEffect(() => {
    if (status === "authenticated" && session?.accessToken) {
      fetchWithAuth("/api/v1/availabilities", session.accessToken as string)
        .then((data) => {
          // Si l'utilisateur n'a pas d'horaires, on peut initialiser un tableau vide
          setAvailabilities(data || []);
        })
        .catch((err) => {
          console.error("Failed to fetch availabilities:", err);
          setError("Impossible de charger vos horaires.");
        })
        .finally(() => setIsLoading(false));
    } else if (status === "unauthenticated") {
      setIsLoading(false);
      setError("Veuillez vous connecter pour voir vos horaires.");
    }
  }, [status, session]);

  const handleToggleDay = (dayId: number) => {
    const exists = availabilities.find(a => a.dayOfWeek === dayId);
    if (exists) {
      setAvailabilities(availabilities.filter(a => a.dayOfWeek !== dayId));
    } else {
      setAvailabilities([...availabilities, { dayOfWeek: dayId, startTime: "09:00", endTime: "18:00", maxCapacity: 1 }]);
    }
  };

  const handleUpdateSlot = (dayId: number, field: keyof Availability, value: string | number) => {
    setAvailabilities(availabilities.map(a => {
      if (a.dayOfWeek === dayId) {
        return { ...a, [field]: value };
      }
      return a;
    }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!session?.accessToken) return;
    
    setIsSaving(true);
    setSaveSuccess(false);
    setError(null);
    
    try {
      // Le backend attend { availabilities: [...] } et les temps au format HH:mm:ss
      const payload = {
        availabilities: availabilities.map(a => ({
          dayOfWeek: a.dayOfWeek,
          startTime: a.startTime.length === 5 ? `${a.startTime}:00` : a.startTime,
          endTime: a.endTime.length === 5 ? `${a.endTime}:00` : a.endTime,
          maxCapacity: a.maxCapacity
        }))
      };

      await fetchWithAuth("/api/v1/availabilities", session.accessToken as string, {
        method: "PUT",
        body: JSON.stringify(payload)
      });
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000);
    } catch (err) {
      console.error(err);
      setError("Erreur lors de la sauvegarde.");
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '300px' }}>
        <p className="animate-fade-in" style={{ opacity: 0.5 }}>Chargement de vos horaires...</p>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <div style={{ marginBottom: '2rem' }}>
        <h1>Horaires d'Ouverture</h1>
        <p>Définissez quand vos clients et l'IA peuvent réserver un créneau.</p>
      </div>

      {error && (
        <div className="glass-panel" style={{ borderLeft: '4px solid var(--danger)', marginBottom: '2rem' }}>
          <h3 style={{ color: 'var(--danger)' }}>Erreur</h3>
          <p>{error}</p>
        </div>
      )}

      <form onSubmit={handleSave} className="glass-panel" style={{ maxWidth: '800px', margin: '0 auto' }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          
          <div style={{ display: 'flex', borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '1rem', fontWeight: 'bold' }}>
            <div style={{ flex: '1' }}>Jour</div>
            <div style={{ flex: '1', textAlign: 'center' }}>Ouverture</div>
            <div style={{ flex: '1', textAlign: 'center' }}>Fermeture</div>
            <div style={{ flex: '0.5', textAlign: 'center' }}>Capacité</div>
          </div>

          {DAYS.map((day) => {
            const currentSlot = availabilities.find(a => a.dayOfWeek === day.id);
            const isOpen = !!currentSlot;

            return (
              <div key={day.id} style={{ display: 'flex', alignItems: 'center', padding: '1rem 0', borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                
                {/* Toggle & Day Name */}
                <div style={{ flex: '1', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <label className="switch">
                    <input 
                      type="checkbox" 
                      checked={isOpen}
                      onChange={() => handleToggleDay(day.id)}
                      style={{ 
                        accentColor: 'var(--accent-primary)',
                        width: '20px', height: '20px'
                      }}
                    />
                  </label>
                  <span style={{ fontWeight: isOpen ? 'bold' : 'normal', opacity: isOpen ? 1 : 0.5 }}>
                    {day.name}
                  </span>
                </div>

                {/* Times & Capacity */}
                {isOpen ? (
                  <>
                    <div style={{ flex: '1', display: 'flex', justifyContent: 'center' }}>
                      <input 
                        type="time" 
                        value={currentSlot.startTime.substring(0, 5)}
                        onChange={(e) => handleUpdateSlot(day.id, 'startTime', e.target.value)}
                        style={{ background: 'rgba(0,0,0,0.3)', color: 'white', border: '1px solid var(--glass-border)', padding: '8px', borderRadius: '4px', outline: 'none' }}
                      />
                    </div>
                    <div style={{ flex: '1', display: 'flex', justifyContent: 'center' }}>
                      <input 
                        type="time" 
                        value={currentSlot.endTime.substring(0, 5)}
                        onChange={(e) => handleUpdateSlot(day.id, 'endTime', e.target.value)}
                        style={{ background: 'rgba(0,0,0,0.3)', color: 'white', border: '1px solid var(--glass-border)', padding: '8px', borderRadius: '4px', outline: 'none' }}
                      />
                    </div>
                    <div style={{ flex: '0.5', display: 'flex', justifyContent: 'center' }}>
                      <input 
                        type="number" min="1" max="10"
                        value={currentSlot.maxCapacity}
                        onChange={(e) => handleUpdateSlot(day.id, 'maxCapacity', parseInt(e.target.value))}
                        style={{ width: '60px', background: 'rgba(0,0,0,0.3)', color: 'white', border: '1px solid var(--glass-border)', padding: '8px', borderRadius: '4px', outline: 'none' }}
                      />
                    </div>
                  </>
                ) : (
                  <div style={{ flex: '2.5', textAlign: 'center', opacity: 0.3 }}>
                    Fermé
                  </div>
                )}
              </div>
            );
          })}

        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '2rem' }}>
          <button 
            type="submit" 
            className="btn-primary"
            disabled={isSaving}
            style={{ 
              background: saveSuccess ? 'var(--success)' : 'var(--accent-gradient)',
              transition: 'background 0.3s ease'
            }}
          >
            {isSaving ? "Sauvegarde..." : (saveSuccess ? "✓ Sauvegardé !" : "Enregistrer les horaires")}
          </button>
        </div>

      </form>
    </div>
  );
}
