'use client';

import { useState } from 'react';
import { Release, Environment } from '@/lib/types';
import { api } from '@/lib/api';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';

interface Props {
  open: boolean;
  onClose: () => void;
  release: Release | null;
  environment: Environment | null;
  onDeployed: () => void;
}

export default function DeployDialog({
  open,
  onClose,
  release,
  environment,
  onDeployed,
}: Props) {
  const [deploying, setDeploying] = useState(false);

  const handleDeploy = async () => {
    if (!release || !environment) return;
    setDeploying(true);
    try {
      await api.deployRelease(release.id, environment.id);
      onDeployed();
      onClose();
    } catch (e) {
      alert(e instanceof Error ? e.message : 'Failed to deploy');
    } finally {
      setDeploying(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="text-brand-navy">
            Deploy to {environment?.name}
          </DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <p className="text-sm text-brand-gray">
            The following artifacts from <strong className="text-brand-navy">{release?.name}</strong> will be deployed to <strong className="text-brand-navy">{environment?.name}</strong>:
          </p>
          <div className="rounded-md border border-gray-200">
            <table className="w-full text-sm">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-3 py-2 text-left font-medium text-brand-gray">Component</th>
                  <th className="px-3 py-2 text-left font-medium text-brand-gray">Version</th>
                  <th className="px-3 py-2 text-left font-medium text-brand-gray">Owner</th>
                </tr>
              </thead>
              <tbody>
                {release?.artifacts.map((a) => (
                  <tr key={a.id} className="border-t border-gray-100">
                    <td className="px-3 py-2 text-brand-navy">{a.componentName}</td>
                    <td className="px-3 py-2 text-brand-blue">{a.version}</td>
                    <td className="px-3 py-2 text-brand-gray">{a.owner}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button
              onClick={handleDeploy}
              disabled={deploying}
              className="bg-brand-purple hover:bg-brand-purple/90"
            >
              {deploying ? 'Deploying...' : 'Deploy'}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}
