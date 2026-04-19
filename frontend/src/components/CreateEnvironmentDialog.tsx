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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

interface Props {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}

export default function CreateEnvironmentDialog({ open, onClose, onCreated }: Props) {
  const [name, setName] = useState('');
  const [type, setType] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim() || !type.trim()) return;
    setSubmitting(true);
    try {
      await api.createEnvironment({
        name: name.trim(),
        type: type.trim(),
      });
      setName('');
      setType('');
      onCreated();
      onClose();
    } catch (err) {
      alert(err instanceof Error ? err.message : 'Failed to create environment');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={(open) => !open && onClose()}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle className="text-brand-navy">Create Environment</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="env-name">Name</Label>
            <Input
              id="env-name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g., SIT3"
              required
            />
          </div>
          <div>
            <Label htmlFor="env-type">Type</Label>
            <Select value={type} onValueChange={(v) => setType(v ?? '')}>
              <SelectTrigger>
                <SelectValue placeholder="Select environment type">
                  {(value: string | null) =>
                    value ? value : 'Select environment type'
                  }
                </SelectValue>
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="DEV">DEV</SelectItem>
                <SelectItem value="SIT">SIT</SelectItem>
                <SelectItem value="UAT">UAT</SelectItem>
              </SelectContent>
            </Select>
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
