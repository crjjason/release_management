'use client';

import { Component, ReleaseArtifact } from '@/lib/types';
import { Input } from '@/components/ui/input';

export interface ArtifactFormItem {
  componentId: number;
  componentName: string;
  version: string;
  pipelineUrl: string;
  owner: string;
  selected: boolean;
}

export function buildArtifactFormItems(
  components: Component[],
  currentReleaseArtifacts: ReleaseArtifact[],
  selectAll: boolean
): ArtifactFormItem[] {
  return components.map((c) => {
    const existing = currentReleaseArtifacts.find((ra) => ra.componentId === c.id);
    return {
      componentId: c.id,
      componentName: c.name,
      version: existing?.version ?? '',
      pipelineUrl: existing?.pipelineUrl ?? c.pipelineUrl ?? '',
      owner: existing?.owner ?? c.owner ?? '',
      selected: selectAll || !!existing,
    };
  });
}

interface Props {
  items: ArtifactFormItem[];
  onChange: (items: ArtifactFormItem[]) => void;
}

export default function ReleaseArtifactEditor({ items, onChange }: Props) {
  const toggleSelected = (componentId: number) => {
    onChange(
      items.map((it) =>
        it.componentId === componentId ? { ...it, selected: !it.selected } : it
      )
    );
  };

  const updateItem = (componentId: number, patch: Partial<ArtifactFormItem>) => {
    onChange(
      items.map((it) => (it.componentId === componentId ? { ...it, ...patch } : it))
    );
  };

  return (
    <div className="max-h-72 space-y-3 overflow-y-auto rounded border p-3">
      {items.map((it) => (
        <div key={it.componentId} className="space-y-2 rounded bg-gray-50 p-2">
          <label className="flex cursor-pointer items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={it.selected}
              onChange={() => toggleSelected(it.componentId)}
              className="h-4 w-4 rounded border-gray-300 text-brand-blue focus:ring-brand-blue"
            />
            <span className="font-medium text-brand-navy">{it.componentName}</span>
          </label>
          {it.selected && (
            <div className="ml-6 grid grid-cols-1 gap-2 sm:grid-cols-3">
              <Input
                placeholder="Version"
                value={it.version}
                onChange={(e) => updateItem(it.componentId, { version: e.target.value })}
                required
              />
              <Input
                placeholder="Owner"
                value={it.owner}
                onChange={(e) => updateItem(it.componentId, { owner: e.target.value })}
                required
              />
              <Input
                placeholder="Pipeline URL"
                value={it.pipelineUrl}
                onChange={(e) => updateItem(it.componentId, { pipelineUrl: e.target.value })}
              />
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
