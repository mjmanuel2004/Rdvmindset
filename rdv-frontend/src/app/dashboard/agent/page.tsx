"use client";

import { useSession } from "next-auth/react";
import { useEffect, useState } from "react";
import { fetchWithAuth } from "@/lib/apiClient";

interface AgentConfig {
  tone: string;
  faq: string;
  pricing: string;
  appointmentDurationMinutes: number;
}

interface Agent {
  id: string;
  name: string;
  type: string;
  config: AgentConfig;
}

export default function AgentConfigPage() {
  const { data: session, status } = useSession();
  
  const [agent, setAgent] = useState<Agent | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saveSuccess, setSaveSuccess] = useState(false);

  // Form states
  const [formData, setFormData] = useState<AgentConfig>({
    tone: "PROFESSIONAL",
    faq: "",
    pricing: "",
    appointmentDurationMinutes: 30
  });

  const fetchAgents = async (token: string) => {
    try {
      const data = await fetchWithAuth("/api/v1/agents", token);
      if (data && data.length > 0) {
        const currentAgent = data[0];
        setAgent(currentAgent);
        if (currentAgent.config) {
          setFormData({
            tone: currentAgent.config.tone || "PROFESSIONAL",
            faq: currentAgent.config.faq || "",
            pricing: currentAgent.config.pricing || "",
            appointmentDurationMinutes: currentAgent.config.appointmentDurationMinutes || 30
          });
        }
      }
    } catch (err) {
      console.error("Failed to fetch agent:", err);
      setError("Impossible de charger la configuration de l'Agent.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (status === "authenticated" && session?.accessToken) {
      fetchAgents(session.accessToken as string);
    } else if (status === "unauthenticated") {
      setIsLoading(false);
      setError("Veuillez vous connecter pour voir cette page.");
    }
  }, [status, session]);

  const handleCreateAgent = async () => {
    setIsSaving(true);
    try {
      const token = session?.accessToken as string;
      const newAgent = await fetchWithAuth("/api/v1/agents", token, {
        method: "POST",
        body: JSON.stringify({
          name: "Assistant IA RdvMindset",
          type: "VOCAL"
        })
      });
      setAgent(newAgent);
    } catch (err) {
      console.error(err);
      setError("Erreur lors de la création de l'agent.");
    } finally {
      setIsSaving(false);
    }
  };

  const handleSaveConfig = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!agent || !session?.accessToken) return;
    
    setIsSaving(true);
    setSaveSuccess(false);
    
    try {
      await fetchWithAuth(`/api/v1/agents/${agent.id}/config`, session.accessToken as string, {
        method: "PUT",
        body: JSON.stringify(formData)
      });
      setSaveSuccess(true);
      setTimeout(() => setSaveSuccess(false), 3000); // Reset after 3 seconds
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
        <p className="animate-fade-in" style={{ opacity: 0.5 }}>Chargement du Cerveau de l'IA...</p>
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

  if (!agent) {
    return (
      <div className="animate-fade-in glass-panel" style={{ textAlign: 'center', padding: '4rem 2rem' }}>
        <h2>Aucun Agent IA détecté</h2>
        <p style={{ marginTop: '1rem', marginBottom: '2rem', color: 'var(--text-secondary)' }}>
          Pour que l'Intelligence Artificielle puisse répondre au téléphone et prendre vos rendez-vous, vous devez initialiser votre Agent.
        </p>
        <button 
          className="btn-primary" 
          onClick={handleCreateAgent}
          disabled={isSaving}
        >
          {isSaving ? "Création en cours..." : "Créer mon Assistant IA"}
        </button>
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <div style={{ marginBottom: '2rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1>Configuration de l'Agent</h1>
          <p>Personnalisez le comportement de votre {agent.name} ({agent.type}).</p>
        </div>
      </div>

      <form onSubmit={handleSaveConfig} className="grid grid-cols-3">
        
        {/* Paramètres de base */}
        <div className="glass-panel" style={{ gridColumn: 'span 1' }}>
          <h3>Personnalité</h3>
          
          <div style={{ marginTop: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.9rem', fontWeight: '500' }}>Ton de la voix</label>
              <select 
                value={formData.tone}
                onChange={(e) => setFormData({...formData, tone: e.target.value})}
                style={{
                  background: 'rgba(0,0,0,0.2)', color: 'white', border: '1px solid var(--glass-border)',
                  padding: '12px', borderRadius: '8px', outline: 'none'
                }}
              >
                <option value="PROFESSIONAL">Professionnel (Vouvoiement, sérieux)</option>
                <option value="FRIENDLY">Amical (Tutoiement, chaleureux)</option>
                <option value="DYNAMIC">Dynamique (Rapide, énergique)</option>
              </select>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.9rem', fontWeight: '500' }}>Durée des rendez-vous (min)</label>
              <input 
                type="number"
                min="10" max="120" step="5"
                value={formData.appointmentDurationMinutes}
                onChange={(e) => setFormData({...formData, appointmentDurationMinutes: parseInt(e.target.value)})}
                style={{
                  background: 'rgba(0,0,0,0.2)', color: 'white', border: '1px solid var(--glass-border)',
                  padding: '12px', borderRadius: '8px', outline: 'none'
                }}
              />
            </div>
          </div>
        </div>

        {/* Connaissances Métier */}
        <div className="glass-panel" style={{ gridColumn: 'span 2' }}>
          <h3>Connaissances (FAQ)</h3>
          <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
            Apprenez à votre IA comment répondre aux questions récurrentes de vos clients.
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.9rem', fontWeight: '500' }}>Foire Aux Questions (FAQ)</label>
              <textarea 
                rows={6}
                placeholder="Ex: - Comment s'y rendre ? Nous sommes au 2ème étage à droite.&#10;- Faut-il amener une serviette ? Non, tout est fourni."
                value={formData.faq}
                onChange={(e) => setFormData({...formData, faq: e.target.value})}
                style={{
                  background: 'rgba(0,0,0,0.2)', color: 'white', border: '1px solid var(--glass-border)',
                  padding: '12px', borderRadius: '8px', outline: 'none', resize: 'vertical'
                }}
              />
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              <label style={{ fontSize: '0.9rem', fontWeight: '500' }}>Tarification</label>
              <textarea 
                rows={3}
                placeholder="Ex: Consultation standard à 50€. Blanchiment dentaire à 120€."
                value={formData.pricing}
                onChange={(e) => setFormData({...formData, pricing: e.target.value})}
                style={{
                  background: 'rgba(0,0,0,0.2)', color: 'white', border: '1px solid var(--glass-border)',
                  padding: '12px', borderRadius: '8px', outline: 'none', resize: 'vertical'
                }}
              />
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: '1rem' }}>
              <button 
                type="submit" 
                className="btn-primary"
                disabled={isSaving}
                style={{ 
                  background: saveSuccess ? 'var(--success)' : 'var(--accent-gradient)',
                  transition: 'background 0.3s ease'
                }}
              >
                {isSaving ? "Sauvegarde..." : (saveSuccess ? "✓ Sauvegardé !" : "Enregistrer la configuration")}
              </button>
            </div>

          </div>
        </div>

      </form>
    </div>
  );
}
