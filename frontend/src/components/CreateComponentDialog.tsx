'use client';

import { useState } from 'react';
import { api } from '@/lib/api';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';

interface Props {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}

export default function CreateComponentDialog({ open, onClose, onCreated }: Props) {
  const [name, setName] = useState('');
  const [owner, setOwner] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !owner.trim()) return;
    setSubmitting(true);
    try {
      await api.createComponent({
        name: name.trim(),
        owner: owner.trim(),
      });
      setName('');
      setOwner('');
      onCreated();
      onClose();
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to create component');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(open) => !open && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="text-brand-navy">Create Component</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="comp-name">Name</Label>
            <Input
              id="comp-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g., payment-service"
              required
            />
          </div>
          <div>
            <Label htmlFor="comp-owner">Owner</Label>
            <Input
              id="comp-owner"
              value={owner}
              onChange={(e) => setOwner(e.target.value)}
              placeholder="e.g., Backend Team"
              required
            />
          </div>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={submitting}
              className="bg-brand-purple hover:bg-brand-purple/90"
            >
              {submitting ? 'Creating...' : 'Create'}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
