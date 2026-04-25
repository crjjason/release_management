'use client';

import { useState, useEffect, useCallback } from 'react';
import { Environment, Release, Deployment } from '@/lib/types';
import { api } from '@/lib/api';
import { Button } from '@/components/ui/button';
import StatusBadge from '@/components/StatusBadge';
import CreateEnvironmentDialog from '@/components/CreateEnvironmentDialog';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';

export default function EnvironmentsPage() {
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [releases, setReleases] = useState<Release[]>([]);
  const [deployments, setDeployments] = useState<Record<number, Deployment[]>>({});
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [envData, relData] = await Promise.all([
        api.getEnvironments(),
        api.getReleases(),
      ]);
      setEnvironments(envData);
      setReleases(relData);

      // Fetch deployments for each environment
      const deploymentMap: Record<number, Deployment[]> = {};
      await Promise.all(
        envData.map(async (env) => {
          try {
            const deps = await api.getEnvironmentDeployments(env.id);
            deploymentMap[env.id] = deps;
          } catch (e) {
            console.error(e);
            deploymentMap[env.id] = [];
          }
        })
      );
      setDeployments(deploymentMap);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const getReleasesForEnv = (envId: number) => {
    return releases.filter(
      (r) =>
        r.sitEnvironment?.id === envId || r.uatEnvironment?.id === envId
    );
  };

  const handleToggle = async (id: number) => {
    try {
      await api.toggleEnvironment(id);
      fetchData();
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to toggle environment');
    }
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-brand-navy">Environments</h1>
        <Button
          onClick={() => setCreateOpen(true)}
          className="bg-brand-purple hover:bg-brand-purple/90"
        >
          Create Environment
        </Button>
      </div>

      {loading ? (
        <div className="py-20 text-center text-brand-gray">Loading...</div>
      ) : environments.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center text-brand-gray">
            No environments yet. Create your first environment to get started.
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {environments.map((env) => {
            const envReleases = getReleasesForEnv(env.id);
            const envDeployments = deployments[env.id] ?? [];
            const isDisabled = !env.active;
            return (
              <Card
                key={env.id}
                className={`border-l-4 ${isDisabled ? 'border-l-gray-300 opacity-60' : 'border-l-brand-blue'}`}
              >
                <CardHeader className="pb-3">
                  <div className="flex items-center justify-between">
                    <CardTitle className={`text-lg ${isDisabled ? 'text-brand-gray' : 'text-brand-navy'}`}>
                      {env.name}
                    </CardTitle>
                    <div className="flex items-center gap-2">
                      <span className="rounded bg-gray-100 px-2 py-0.5 text-xs font-medium text-brand-gray">
                        {env.type}
                      </span>
                      {isDisabled && (
                        <span className="rounded bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700">
                          Disabled
                        </span>
                      )}
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="mb-3 flex justify-end">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleToggle(env.id)}
                      className={isDisabled ? 'text-green-700 hover:text-green-800' : 'text-red-700 hover:text-red-800'}
                    >
                      {isDisabled ? 'Enable' : 'Disable'}
                    </Button>
                  </div>

                  {envDeployments.length > 0 && (
                    <div className="mb-4">
                      <h4 className="mb-2 text-xs font-semibold uppercase tracking-wide text-brand-gray">
                        Deployed Components
                      </h4>
                      <div className="space-y-1">
                        {envDeployments.map((d) => (
                          <div
                            key={d.componentId}
                            className="flex items-center justify-between rounded bg-gray-50 px-2 py-1.5 text-sm"
                          >
                            <span className="font-medium text-brand-navy">{d.componentName}</span>
                            <span className="text-brand-blue">v{d.version}</span>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {envReleases.length === 0 ? (
                    <p className="text-sm text-brand-gray">No active releases</p>
                  ) : (
                    <div className="space-y-2">
                      <h4 className="text-xs font-semibold uppercase tracking-wide text-brand-gray">
                        Linked Releases
                      </h4>
                      {envReleases.map((r) => (
                        <div
                          key={r.id}
                          className="flex items-center justify-between rounded bg-gray-50 px-3 py-2"
                        >
                          <span className="text-sm font-medium text-brand-navy">
                            {r.name}
                          </span>
                          <StatusBadge status={r.status} />
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      <CreateEnvironmentDialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={fetchData}
      />
    </div>
  );
}
