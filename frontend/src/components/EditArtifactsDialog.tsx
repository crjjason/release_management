'use client';

import { useState, useEffect } from 'react';
import { Component, ReleaseArtifact } from '@/lib/types';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import ReleaseArtifactEditor, { ArtifactFormItem, buildArtifactFormItems } from './ReleaseArtifactEditor';

interface Props {
  open: boolean;
  onClose: () => void;
  components: Component[];
  currentReleaseArtifacts: ReleaseArtifact[];
  onSave: (releaseArtifacts: ReleaseArtifact[]) => void;
}

export default function EditArtifactsDialog({
  open,
  onClose,
  components,
  currentReleaseArtifacts,
  onSave,
}: Props) {
  const [items, setItems] = useState<ArtifactFormItem[]>([]);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!open) return;
    setItems(buildArtifactFormItems(components, currentReleaseArtifacts, false));
    setSubmitting(false);
  }, [open, components, currentReleaseArtifacts]);

  const selectedCount = items.filter((i) => i.selected).length;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const selected = items.filter((it) => it.selected);
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
      onSave(releaseArtifacts);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="text-brand-navy">Edit Artifacts</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <ReleaseArtifactEditor
            items={items}
            onChange={setItems}
          />
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={submitting || selectedCount === 0}
              className="bg-brand-purple hover:bg-brand-purple/90"
            >
              {submitting ? 'Saving...' : 'Save Artifacts'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
