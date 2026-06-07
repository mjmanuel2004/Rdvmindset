import Link from "next/link";

export default function Home() {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', textAlign: 'center' }}>
      
      <div className="glass-panel animate-fade-in" style={{ maxWidth: '600px', width: '100%' }}>
        <div style={{ marginBottom: '2rem' }}>
          <h1>RdvMindset</h1>
          <p style={{ fontSize: '1.1rem', marginTop: '1rem' }}>
            L'intelligence artificielle au service de votre agenda. Laissez votre Agent IA prendre les appels et gérer vos rendez-vous 24h/24 et 7j/7.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '16px', justifyContent: 'center' }}>
          <Link href="/dashboard" style={{ textDecoration: 'none' }}>
            <button className="btn-primary">
              Accéder au Tableau de Bord
            </button>
          </Link>
          <button className="btn-secondary">
            En savoir plus
          </button>
        </div>
      </div>
      
      <div className="grid grid-cols-3" style={{ marginTop: '4rem', width: '100%' }}>
        <div className="glass-panel animate-fade-in" style={{ animationDelay: '0.2s' }}>
          <h3>🤖 Assistant Vocal</h3>
          <p>Un agent configuré sur Vapi qui décroche au téléphone et planifie vos rendez-vous avec un ton naturel.</p>
        </div>
        <div className="glass-panel animate-fade-in" style={{ animationDelay: '0.3s' }}>
          <h3>📅 Synchro Google</h3>
          <p>Chaque rendez-vous pris par l'IA apparaît instantanément sur votre Google Calendar.</p>
        </div>
        <div className="glass-panel animate-fade-in" style={{ animationDelay: '0.4s' }}>
          <h3>⚡ Temps Réel</h3>
          <p>Surveillez la prise de rendez-vous en direct grâce à nos WebSockets intégrés.</p>
        </div>
      </div>

    </div>
  );
}
