# Guide Frontend : Phase 7

Ce guide détaille la conception et l'implémentation de l'application cliente Next.js 14 (App Router, TypeScript, Tailwind CSS, Zustand, TanStack Query).

---

## Structure de l'application Next.js (`src/app/`)
```text
src/
├── app/
│   ├── (auth)/                 # Groupe de routes d'authentification
│   │   ├── login/page.tsx
│   │   └── register/page.tsx
│   ├── (dashboard)/            # Groupe de routes du panel d'administration
│   │   ├── layout.tsx          # Sidebar persistante + Navbar
│   │   ├── page.tsx            # Accueil / Métriques / KPIs
│   │   ├── agenda/page.tsx     # Calendrier interactif
│   │   ├── configuration/page.tsx # Prompts, FAQ, Tarifs de l'agent
│   │   └── onboarding/page.tsx # Stepper d'inscription
│   ├── layout.tsx              # Root Layout, Polices, Providers
│   └── page.tsx                # Page d'accueil publique / Landing
├── components/                 # Composants d'interface (shadcn)
│   ├── ui/
│   └── agenda-view.tsx
├── hooks/
│   ├── use-rdv-realtime.ts     # Hook WebSocket STOMP
│   └── use-api.ts              # Requêtes API
└── store/
    └── auth-store.ts           # State Zustand pour la session
```

---

## 1. Gestion d'état d'authentification (`auth-store.ts`)
Zustand est choisi pour sa légèreté et sa compatibilité immédiate avec React 18.

```typescript
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  id: string;
  email: string;
  role: 'SUPER_ADMIN' | 'ENTREPRISE' | 'CLIENT';
  entrepriseId?: string;
}

interface AuthState {
  user: User | null;
  token: string | null;
  refreshToken: string | null;
  setAuth: (user: User, token: string, refreshToken: string) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      token: null,
      refreshToken: null,
      setAuth: (user, token, refreshToken) => set({ user, token, refreshToken }),
      clearAuth: () => set({ user: null, token: null, refreshToken: null }),
    }),
    {
      name: 'rdvmindset-auth-storage', // Stocké dans le localStorage
    }
  )
);
```

---

## 2. Axios Interceptor pour le Refresh Auto du JWT
Un intercepteur HTTP intercepte les requêtes pour injecter le token JWT dans les en-têtes et, en cas de retour d'erreur `401 Unauthorized`, tente d'obtenir un nouveau token de session de manière transparente pour l'utilisateur.

```typescript
import axios from 'axios';
import { useAuthStore } from '@/store/auth-store';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1',
});

// 1. Injecter le token dans chaque requête
api.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 2. Gestion de l'expiration du token et refresh automatique
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const refreshToken = useAuthStore.getState().refreshToken;
        const res = await axios.post('http://localhost:8080/api/v1/auth/refresh', { refreshToken });
        
        const { token, newRefreshToken, user } = res.data;
        useAuthStore.getState().setAuth(user, token, newRefreshToken);
        
        originalRequest.headers.Authorization = `Bearer ${token}`;
        return api(originalRequest);
      } catch (refreshError) {
        useAuthStore.getState().clearAuth();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

## 3. Middleware de Protection des Routes (`middleware.ts`)
Situé à la racine du dossier `src/`, ce fichier filtre les accès côté serveur Next.js.

```typescript
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const token = request.cookies.get('token')?.value; // Lecture du token persisté en cookie
  const isDashboardRoute = request.nextUrl.pathname.startsWith('/(dashboard)');

  if (isDashboardRoute && !token) {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/dashboard/:path*', '/agenda/:path*', '/configuration/:path*'],
};
```

---

## 4. Connexion temps réel WebSocket dans React (`use-rdv-realtime.ts`)
Ce hook se connecte au serveur STOMP de Spring Boot pour recevoir en temps réel les créations/annulations de rendez-vous faites par l'agent vocal.

```typescript
import { useEffect } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useQueryClient } from '@tanstack/react-query';
import { useAuthStore } from '@/store/auth-store';

export const useRdvRealtime = () => {
  const queryClient = useQueryClient();
  const user = useAuthStore((state) => state.user);

  useEffect(() => {
    if (!user || !user.entrepriseId) return;

    const socket = new SockJS('http://localhost:8080/ws-rdv');
    const stompClient = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log(str),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    stompClient.onConnect = (frame) => {
      console.log('Connecté au WebSocket STOMP : ' + frame);
      
      stompClient.subscribe(`/topic/entreprise/${user.entrepriseId}/rdv`, (message) => {
        const nouveauRdv = JSON.parse(message.body);
        console.log('Nouveau RDV reçu en temps réel !', nouveauRdv);
        
        // Notification sonore / Toast UI
        // Invalidation du cache de TanStack Query pour rafraîchir le calendrier
        queryClient.invalidateQueries({ queryKey: ['appointments'] });
      });
    };

    stompClient.activate();

    return () => {
      stompClient.deactivate();
    };
  }, [user, queryClient]);
};
```
Une fois connecté, le composant `<AgendaView />` qui utilise React Query mettra instantanément son calendrier à jour à chaque fois que le hook invalidera la requête `['appointments']`.
