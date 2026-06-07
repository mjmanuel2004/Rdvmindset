import Link from "next/link";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      
      {/* Sidebar (Navigation) */}
      <aside className="glass-panel" style={{ width: '280px', margin: '24px 0 24px 24px', display: 'flex', flexDirection: 'column', gap: '2rem' }}>
        <h2>RdvMindset</h2>
        
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <Link href="/dashboard" style={{ color: 'var(--text-primary)', textDecoration: 'none', fontWeight: '500' }}>
            📊 Statistiques
          </Link>
          <Link href="/dashboard/calendar" style={{ color: 'var(--text-secondary)', textDecoration: 'none' }}>
            📅 Calendrier
          </Link>
          <Link href="/dashboard/agent" style={{ color: 'var(--text-secondary)', textDecoration: 'none' }}>
            🤖 Configuration IA
          </Link>
          <Link href="/dashboard/availability" style={{ color: 'var(--text-secondary)', textDecoration: 'none' }}>
            🕒 Horaires
          </Link>
        </nav>

        <div style={{ marginTop: 'auto' }}>
          <button className="btn-secondary" style={{ width: '100%', borderColor: 'var(--danger)', color: 'var(--danger)' }}>
            Déconnexion
          </button>
        </div>
      </aside>

      {/* Contenu principal */}
      <main style={{ flex: 1, padding: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '24px' }}>
          <div className="glass-panel" style={{ padding: '12px 24px', borderRadius: '30px' }}>
            <span>Dr. Emmanuel Monsan</span>
          </div>
        </div>
        
        {children}
      </main>

    </div>
  );
}
