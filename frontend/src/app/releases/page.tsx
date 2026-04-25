'use client';

import { useState, useEffect, useCallback } from 'react';
import { Release, ReleaseStatus, Environment, Component, ReleaseArtifact, Deployment } from '@/lib/types';
import { api } from '@/lib/api';
import StatusBadge from '@/components/StatusBadge';
import CreateReleaseDialog from '@/components/CreateReleaseDialog';
import AssignDialog from '@/components/AssignDialog';
import EditArtifactsDialog from '@/components/EditArtifactsDialog';
import DeployDialog from '@/components/DeployDialog';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
} from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';

const columns = [
  ReleaseStatus.NEW,
  ReleaseStatus.PLANNED,
  ReleaseStatus.IN_PROGRESS,
  ReleaseStatus.FINISHED,
  ReleaseStatus.CANCELLED,
];

const columnLabels: Record<ReleaseStatus, string> = {
  [ReleaseStatus.NEW]: 'New',
  [ReleaseStatus.PLANNED]: 'Planned',
  [ReleaseStatus.IN_PROGRESS]: 'In Progress',
  [ReleaseStatus.FINISHED]: 'Finished',
  [ReleaseStatus.CANCELLED]: 'Cancelled',
};

export default function ReleasesPage() {
  const [releases, setReleases] = useState<Release[]>([]);
  const [environments, setEnvironments] = useState<Environment[]>([]);
  const [components, setComponents] = useState<Component[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [selectedRelease, setSelectedRelease] = useState<Release | null>(null);
  const [assignOpen, setAssignOpen] = useState(false);
  const [editArtifactsOpen, setEditArtifactsOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [deployEnv, setDeployEnv] = useState<Environment | null>(null);
  const [deployOpen, setDeployOpen] = useState(false);
  const [sitDeployments, setSitDeployments] = useState<Deployment[]>([]);
  const [uatDeployments, setUatDeployments] = useState<Deployment[]>([]);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const [relData, envData, compData] = await Promise.all([
        api.getReleases(),
        api.getEnvironments(),
        api.getComponents(),
      ]);
      setReleases(relData);
      setEnvironments(envData);
      setComponents(compData);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const fetchDeploymentsForRelease = useCallback(async (release: Release) => {
    if (release.sitEnvironment) {
      try {
        const data = await api.getEnvironmentDeployments(release.sitEnvironment.id);
        setSitDeployments(data);
      } catch (e) {
        console.error(e);
        setSitDeployments([]);
      }
    } else {
      setSitDeployments([]);
    }
    if (release.uatEnvironment) {
      try {
        const data = await api.getEnvironmentDeployments(release.uatEnvironment.id);
        setUatDeployments(data);
      } catch (e) {
        console.error(e);
        setUatDeployments([]);
      }
    } else {
      setUatDeployments([]);
    }
  }, []);

  const handleStatusChange = async (release: Release, nextStatus: ReleaseStatus) => {
    if (nextStatus === ReleaseStatus.IN_PROGRESS) {
      setSelectedRelease(release);
      setAssignOpen(true);
      return;
    }
    try {
      await api.updateReleaseStatus(release.id, nextStatus);
      fetchData();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to update status');
    }
  };

  const handleAssign = async (
    sitEnvId: number,
    uatEnvId: number,
    releaseArtifacts: ReleaseArtifact[]
  ) => {
    if (!selectedRelease) return;
    try {
      await api.assignRelease(selectedRelease.id, sitEnvId, uatEnvId, releaseArtifacts);
      await api.updateReleaseStatus(selectedRelease.id, ReleaseStatus.IN_PROGRESS);
      setAssignOpen(false);
      setSelectedRelease(null);
      fetchData();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to assign');
    }
  };

  const handleSaveArtifacts = async (
    releaseArtifacts: ReleaseArtifact[]
  ) => {
    if (!selectedRelease) return;
    try {
      await api.updateReleaseArtifacts(selectedRelease.id, releaseArtifacts);
      setEditArtifactsOpen(false);
      fetchData();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to update artifacts');
    }
  };

  const openDetail = (release: Release) => {
    setSelectedRelease(release);
    setDetailOpen(true);
    fetchDeploymentsForRelease(release);
  };

  const openDeploy = (env: Environment) => {
    setDeployEnv(env);
    setDeployOpen(true);
  };

  const grouped = columns.reduce(
    (acc, status) => {
      acc[status] = releases.filter((r) => r.status === status);
      return acc;
    },
    {} as Record<ReleaseStatus, Release[]>
  );

  const validTransitions: Record<ReleaseStatus, ReleaseStatus[]> = {
    [ReleaseStatus.NEW]: [ReleaseStatus.PLANNED, ReleaseStatus.CANCELLED],
    [ReleaseStatus.PLANNED]: [ReleaseStatus.IN_PROGRESS, ReleaseStatus.CANCELLED],
    [ReleaseStatus.IN_PROGRESS]: [ReleaseStatus.FINISHED, ReleaseStatus.CANCELLED],
    [ReleaseStatus.FINISHED]: [],
    [ReleaseStatus.CANCELLED]: [],
  };

  const canEditArtifacts = (status: ReleaseStatus) =>
    status === ReleaseStatus.NEW || status === ReleaseStatus.PLANNED || status === ReleaseStatus.IN_PROGRESS;

  const getDeploymentVersion = (deployments: Deployment[], componentId: number) => {
    return deployments.find((d) => d.componentId === componentId)?.version;
  };

  const renderDiffSection = (label: string, env: Environment | undefined, deployments: Deployment[], release: Release | null) => {
    if (!env || !release) return null;
    return (
      <div className="space-y-2">
        <h4 className="text-sm font-semibold text-brand-navy">{label}: {env.name}</h4>
        {release.artifacts.length === 0 ? (
          <p className="text-sm text-brand-gray">No requested artifacts</p>
        ) : (
          <ul className="space-y-1">
            {release.artifacts.map((a) => {
              const deployedVersion = getDeploymentVersion(deployments, a.componentId);
              const match = deployedVersion === a.version;
              return (
                <li
                  key={a.id}
                  className={`flex items-center justify-between rounded px-3 py-2 text-sm ${
                    match ? 'bg-green-50' : 'bg-yellow-50'
                  }`}
                >
                  <span className="font-medium text-brand-navy">{a.componentName}</span>
                  <div className="flex items-center gap-3">
                    <span className="text-brand-gray">
                      Requested: <span className="font-medium text-brand-blue">{a.version}</span>
                    </span>
                    <span className="text-brand-gray">
                      Deployed:{' '}
                      <span className={`font-medium ${match ? 'text-green-700' : 'text-brand-yellow'}`}>
                        {deployedVersion ?? 'Not deployed'}
                      </span>
                    </span>
                  </div>
                </li>
              );
            })}
          </ul>
        )}
      </div>
    );
  };

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-brand-navy">Releases</h1>
        <Button
          onClick={() => setCreateOpen(true)}
          className="bg-brand-purple hover:bg-brand-purple/90"
        >
          Create Release
        </Button>
      </div>

      {loading ? (
        <div className="py-20 text-center text-brand-gray">Loading...</div>
      ) : (
        <div className="grid grid-cols-1 gap-6 md:grid-cols-3 lg:grid-cols-5">
          {columns.map((status) => (
            <div key={status} className="flex flex-col gap-3">
              <div className="flex items-center justify-between">
                <h2 className="text-sm font-semibold text-brand-navy">
                  {columnLabels[status]}
                </h2>
                <span className="rounded-full bg-gray-200 px-2 py-0.5 text-xs text-gray-700">
                  {grouped[status].length}
                </span>
              </div>
              <div className="flex flex-col gap-3">
                {grouped[status].map((release) => (
                  <Card
                    key={release.id}
                    className="cursor-pointer border-l-4 border-l-brand-blue shadow-sm transition-shadow hover:shadow-md"
                    onClick={() => openDetail(release)}
                  >
                    <CardContent className="p-4">
                      <p className="font-medium text-brand-navy">{release.name}</p>
                      <p className="mt-1 text-xs text-brand-gray">
                        {new Date(release.createdAt).toLocaleDateString()}
                      </p>
                      {release.sitEnvironment && (
                        <p className="mt-1 text-xs text-brand-blue">
                          SIT: {release.sitEnvironment.name}
                        </p>
                      )}
                      {release.uatEnvironment && (
                        <p className="mt-1 text-xs text-brand-purple">
                          UAT: {release.uatEnvironment.name}
                        </p>
                      )}
                      {release.artifacts.length > 0 && (
                        <p className="mt-1 text-xs text-brand-gray">
                          {release.artifacts.length} artifact
                          {release.artifacts.length === 1 ? '' : 's'}
                        </p>
                      )}
                    </CardContent>
                  </Card>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}

      <CreateReleaseDialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={fetchData}
      />

      <AssignDialog
        open={assignOpen}
        onClose={() => {
          setAssignOpen(false);
          setSelectedRelease(null);
        }}
        environments={environments}
        components={components}
        currentReleaseArtifacts={selectedRelease?.artifacts ?? []}
        onAssign={handleAssign}
      />

      <EditArtifactsDialog
        open={editArtifactsOpen}
        onClose={() => setEditArtifactsOpen(false)}
        components={components}
        currentReleaseArtifacts={selectedRelease?.artifacts ?? []}
        onSave={handleSaveArtifacts}
      />

      <DeployDialog
        open={deployOpen}
        onClose={() => {
          setDeployOpen(false);
          setDeployEnv(null);
        }}
        release={selectedRelease}
        environment={deployEnv}
        onDeployed={() => {
          fetchData();
          if (selectedRelease) {
            fetchDeploymentsForRelease(selectedRelease);
          }
        }}
      />

      <Dialog open={detailOpen} onOpenChange={setDetailOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle className="text-brand-navy">
              {selectedRelease?.name}
            </DialogTitle>
          </DialogHeader>
          {selectedRelease && (
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <span className="text-sm text-brand-gray">Status:</span>
                <StatusBadge status={selectedRelease.status} />
              </div>
              {selectedRelease.sitEnvironment && (
                <div className="flex items-center gap-2">
                  <span className="text-sm text-brand-gray">SIT Environment:</span>
                  <span className="text-sm font-medium text-brand-blue">
                    {selectedRelease.sitEnvironment.name}
                  </span>
                </div>
              )}
              {selectedRelease.uatEnvironment && (
                <div className="flex items-center gap-2">
                  <span className="text-sm text-brand-gray">UAT Environment:</span>
                  <span className="text-sm font-medium text-brand-purple">
                    {selectedRelease.uatEnvironment.name}
                  </span>
                </div>
              )}

              {selectedRelease.artifacts.length > 0 && (
                <div className="space-y-3">
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-semibold text-brand-navy">Requested Artifacts:</span>
                  </div>
                  <ul className="mt-1 space-y-1">
                    {selectedRelease.artifacts.map((a) => (
                      <li key={a.id} className="text-sm text-brand-navy">
                        {a.componentName} v{a.version} ({a.owner})
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {(selectedRelease.sitEnvironment || selectedRelease.uatEnvironment) && selectedRelease.artifacts.length > 0 && (
                <div className="space-y-3 border-t border-gray-200 pt-3">
                  <h3 className="text-sm font-semibold text-brand-navy">Deployment Status</h3>
                  {renderDiffSection('SIT', selectedRelease.sitEnvironment, sitDeployments, selectedRelease)}
                  {renderDiffSection('UAT', selectedRelease.uatEnvironment, uatDeployments, selectedRelease)}
                </div>
              )}

              <div className="flex flex-wrap gap-2 pt-2">
                {selectedRelease.sitEnvironment && selectedRelease.artifacts.length > 0 && (
                  <Button
                    size="sm"
                    className="bg-brand-blue hover:bg-brand-blue/90"
                    onClick={() => openDeploy(selectedRelease.sitEnvironment!)}
                  >
                    Deploy to SIT
                  </Button>
                )}
                {selectedRelease.uatEnvironment && selectedRelease.artifacts.length > 0 && (
                  <Button
                    size="sm"
                    className="bg-brand-purple hover:bg-brand-purple/90"
                    onClick={() => openDeploy(selectedRelease.uatEnvironment!)}
                  >
                    Deploy to UAT
                  </Button>
                )}
              </div>

              {canEditArtifacts(selectedRelease.status) && (
                <div className="pt-2">
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setEditArtifactsOpen(true)}
                  >
                    Edit Artifacts
                  </Button>
                </div>
              )}
              {validTransitions[selectedRelease.status].length > 0 && (
                <div className="pt-2">
                  <span className="text-sm text-brand-gray">Move to:</span>
                  <div className="mt-2 flex gap-2">
                    {validTransitions[selectedRelease.status].map((next) => (
                      <Button
                        key={next}
                        size="sm"
                        variant="outline"
                        onClick={() => {
                          handleStatusChange(selectedRelease, next);
                          setDetailOpen(false);
                        }}
                      >
                        {columnLabels[next]}
                      </Button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}
