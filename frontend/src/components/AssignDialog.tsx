'use client';

import { useState, useEffect } from 'react';
import { Environment, ReleaseArtifact } from '@/lib/types';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import ReleaseArtifactEditor, { ArtifactFormItem, buildArtifactFormItems } from './ReleaseArtifactEditor';

interface Props {
  open: boolean;
  onClose: () => void;
  environments: Environment[];
  components: import('@/lib/types').Component[];
  currentReleaseArtifacts: ReleaseArtifact[];
  onAssign: (sitEnvId: number, uatEnvId: number, releaseArtifacts: ReleaseArtifact[]) => void;
}

export default function AssignDialog({
  open,
  onClose,
  environments,
  components,
  currentReleaseArtifacts,
  onAssign,
}: Props) {
  const [sitEnvId, setSitEnvId] = useState('');
  const [uatEnvId, setUatEnvId] = useState('');
  const [items, setItems] = useState<ArtifactFormItem[]>([]);
  const [submitting, setSubmitting] = useState(false);

  const sitEnvs = environments.filter((e) => e.type === 'SIT' && e.active !== false);
  const uatEnvs = environments.filter((e) => e.type === 'UAT' && e.active !== false);

  useEffect(() => {
    if (!open) return;
    setItems(buildArtifactFormItems(components, currentReleaseArtifacts, true));
    setSitEnvId('');
    setUatEnvId('');
    setSubmitting(false);
  }, [open, components, currentReleaseArtifacts]);

  const selectedCount = items.filter((i) => i.selected).length;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!sitEnvId || !uatEnvId) return;
    const selected = items.filter((it) => it.selected);
    if (selected.length === 0) return;
    const releaseArtifacts: ReleaseArtifact[] = selected.map((it) => ({
      id: 0,
      componentId: it.componentId,
      componentName: it.componentName,
      version: it.version.trim(),
      pipelineUrl: it.pipelineUrl.trim() || undefined,
      owner: it.owner.trim(),
    }));
    setSubmitting(true);
    try {
      onAssign(Number(sitEnvId), Number(uatEnvId), releaseArtifacts);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="text-brand-navy">
            Assign Environments & Artifacts
          </DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label>SIT Environment</Label>
            <Select value={sitEnvId} onValueChange={(v) => setSitEnvId(v ?? '')}>
              <SelectTrigger>
                <SelectValue placeholder="Select SIT environment">
                  {(value: string | null) =>
                    value ? sitEnvs.find((e) => String(e.id) === value)?.name ?? 'Select SIT environment' : 'Select SIT environment'
                  }
                </SelectValue>
              </SelectTrigger>
              <SelectContent>
                {sitEnvs.map((env) => (
                  <SelectItem key={env.id} value={String(env.id)}>
                    {env.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label>UAT Environment</Label>
            <Select value={uatEnvId} onValueChange={(v) => setUatEnvId(v ?? '')}>
              <SelectTrigger>
                <SelectValue placeholder="Select UAT environment">
                  {(value: string | null) =>
                    value ? uatEnvs.find((e) => String(e.id) === value)?.name ?? 'Select UAT environment' : 'Select UAT environment'
                  }
                </SelectValue>
              </SelectTrigger>
              <SelectContent>
                {uatEnvs.map((env) => (
                  <SelectItem key={env.id} value={String(env.id)}>
                    {env.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label>Artifacts</Label>
            <div className="mt-2">
              <ReleaseArtifactEditor
                items={items}
                onChange={setItems}
              />
            </div>
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={submitting || !sitEnvId || !uatEnvId || selectedCount === 0}
              className="bg-brand-purple hover:bg-brand-purple/90"
            >
              {submitting ? 'Assigning...' : 'Assign & Start'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
