const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export async function fetchWithAuth(
  endpoint: string,
  accessToken: string | undefined,
  options: RequestInit = {}
) {
  if (!accessToken) {
    throw new Error("Aucun jeton d'accès (JWT) fourni pour cet appel API.");
  }

  const defaultHeaders = {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${accessToken}`,
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  });

  if (!response.ok) {
    // Si c'est une 401, on pourrait trigger un sign out ici ou dans le composant
    throw new Error(`API Error: ${response.status} ${response.statusText}`);
  }

  return response.json();
}
