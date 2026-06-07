export default function DashboardPage() {
  
  // Données fictives simulant l'appel API à /api/v1/analytics/dashboard
  const mockAnalytics = {
    totalAppointments: 142,
    confirmedAppointments: 120,
    cancelledAppointments: 22,
    totalClients: 85
  };

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
          <span style={{ fontSize: '2.5rem', fontWeight: '700' }}>{mockAnalytics.totalAppointments}</span>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem', borderTop: '4px solid var(--success)' }}>
          <h4 style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Confirmés</h4>
          <span style={{ fontSize: '2.5rem', fontWeight: '700', color: 'var(--success)' }}>{mockAnalytics.confirmedAppointments}</span>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem', borderTop: '4px solid var(--danger)' }}>
          <h4 style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Annulés</h4>
          <span style={{ fontSize: '2.5rem', fontWeight: '700', color: 'var(--danger)' }}>{mockAnalytics.cancelledAppointments}</span>
        </div>

        <div className="glass-panel" style={{ padding: '1.5rem', borderTop: '4px solid var(--warning)' }}>
          <h4 style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Clients Uniques</h4>
          <span style={{ fontSize: '2.5rem', fontWeight: '700', color: 'var(--warning)' }}>{mockAnalytics.totalClients}</span>
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
            <div style={{ padding: '1rem', background: 'rgba(255,255,255,0.03)', borderRadius: '8px' }}>
              <strong style={{ display: 'block', color: 'var(--accent-primary)' }}>Aujourd'hui à 14:30</strong>
              <span>Consultation - Jean Dupont</span>
            </div>
            <div style={{ padding: '1rem', background: 'rgba(255,255,255,0.03)', borderRadius: '8px' }}>
              <strong style={{ display: 'block', color: 'var(--accent-primary)' }}>Aujourd'hui à 16:00</strong>
              <span>Suivi - Marie Martin</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
