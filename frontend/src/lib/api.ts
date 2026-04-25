import { Component, Deployment, Environment, Release, ReleaseArtifact, ReleaseStatus } from './types';

const API_BASE = 'http://localhost:8080/api';

async function fetchJson<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  return res.json();
}

export const api = {
  getEnvironments: () => fetchJson<Environment[]>(`${API_BASE}/environments`),
  createEnvironment: (env: Omit<Environment, 'id' | 'active'>) =>
    fetchJson<Environment>(`${API_BASE}/environments`, {
      method: 'POST',
      body: JSON.stringify(env),
    }),
  toggleEnvironment: (id: number) =>
    fetchJson<Environment>(`${API_BASE}/environments/${id}/toggle`, {
      method: 'PUT',
    }),
  getComponents: () => fetchJson<Component[]>(`${API_BASE}/components`),
  createComponent: (component: Omit<Component, 'id'>) =>
    fetchJson<Component>(`${API_BASE}/components`, {
      method: 'POST',
      body: JSON.stringify(component),
    }),
  getReleases: () => fetchJson<Release[]>(`${API_BASE}/releases`),
  getRelease: (id: number) => fetchJson<Release>(`${API_BASE}/releases/${id}`),
  createRelease: (name: string) =>
    fetchJson<Release>(`${API_BASE}/releases`, {
      method: 'POST',
      body: JSON.stringify({ name }),
    }),
  updateReleaseStatus: (id: number, status: ReleaseStatus) =>
    fetchJson<Release>(`${API_BASE}/releases/${id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status }),
    }),
  assignRelease: (
    id: number,
    sitEnvironmentId: number,
    uatEnvironmentId: number,
    releaseArtifacts: ReleaseArtifact[]
  ) =>
    fetchJson<Release>(`${API_BASE}/releases/${id}/assign`, {
      method: 'PUT',
      body: JSON.stringify({ sitEnvironmentId, uatEnvironmentId, releaseArtifacts }),
    }),
  updateReleaseArtifacts: (id: number, releaseArtifacts: ReleaseArtifact[]) =>
    fetchJson<Release>(`${API_BASE}/releases/${id}/artifacts`, {
      method: 'PUT',
      body: JSON.stringify({ artifacts: releaseArtifacts }),
    }),
  getEnvironmentDeployments: (envId: number) =>
    fetchJson<Deployment[]>(`${API_BASE}/environments/${envId}/deployments`),
  deployRelease: (releaseId: number, environmentId: number) =>
    fetchJson<Deployment[]>(`${API_BASE}/deployments`, {
      method: 'POST',
      body: JSON.stringify({ releaseId, environmentId }),
    }),
};
