import Link from "next/link";

export default function LoginPage() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh' }}>
      <div className="glass-panel animate-fade-in" style={{ width: '100%', maxWidth: '400px', padding: '2.5rem' }}>
        
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h2>Connexion</h2>
          <p style={{ fontSize: '0.9rem' }}>Accédez à votre espace sécurisé RdvMindset</p>
        </div>

        <form style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <label style={{ fontSize: '0.9rem', fontWeight: '500' }}>Email Professionnel</label>
            <input 
              type="email" 
              placeholder="dr.dupont@clinique.com"
              style={{
                background: 'rgba(0, 0, 0, 0.2)',
                border: '1px solid var(--glass-border)',
                padding: '12px',
                borderRadius: '8px',
                color: 'white',
                outline: 'none'
              }}
            />
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
            <label style={{ fontSize: '0.9rem', fontWeight: '500' }}>Mot de passe</label>
            <input 
              type="password" 
              placeholder="••••••••"
              style={{
                background: 'rgba(0, 0, 0, 0.2)',
                border: '1px solid var(--glass-border)',
                padding: '12px',
                borderRadius: '8px',
                color: 'white',
                outline: 'none'
              }}
            />
          </div>

          <Link href="/dashboard" style={{ textDecoration: 'none', width: '100%', marginTop: '1rem' }}>
            <button type="button" className="btn-primary" style={{ width: '100%' }}>
              Se connecter (Simulation)
            </button>
          </Link>

        </form>

        <div style={{ textAlign: 'center', marginTop: '2rem', fontSize: '0.85rem' }}>
          <p style={{ opacity: 0.7 }}>Redirection sécurisée via Keycloak OAuth2 en production.</p>
        </div>

      </div>
    </div>
  );
}
