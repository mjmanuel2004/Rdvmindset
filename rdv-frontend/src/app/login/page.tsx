"use client";

import { signIn } from "next-auth/react";

export default function LoginPage() {
  return (
    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh' }}>
      <div className="glass-panel animate-fade-in" style={{ width: '100%', maxWidth: '400px', padding: '2.5rem' }}>
        
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h2>Connexion</h2>
          <p style={{ fontSize: '0.9rem' }}>Accédez à votre espace sécurisé RdvMindset</p>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem', textAlign: 'center' }}>
          
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            L'authentification est déléguée à notre serveur sécurisé Keycloak. Vous allez être redirigé.
          </p>

          <button 
            type="button" 
            className="btn-primary" 
            style={{ width: '100%', padding: '16px' }}
            onClick={() => signIn("keycloak", { callbackUrl: "/dashboard" })}
          >
            Se connecter via Keycloak
          </button>

        </div>
      </div>
    </div>
  );
}
