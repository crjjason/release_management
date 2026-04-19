'use client';

import { useState, useEffect, useCallback } from 'react';
import { Component } from '@/lib/types';
import { api } from '@/lib/api';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import CreateComponentDialog from '@/components/CreateComponentDialog';

export default function ComponentsPage() {
  const [components, setComponents] = useState<Component[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);

  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const data = await api.getComponents();
      setComponents(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return (
    <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="mb-8 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-brand-navy">Components</h1>
        <Button
          onClick={() => setCreateOpen(true)}
          className="bg-brand-purple hover:bg-brand-purple/90"
        >
          Create Component
        </Button>
      </div>

      {loading ? (
        <div className="py-20 text-center text-brand-gray">Loading...</div>
      ) : components.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center text-brand-gray">
            No components yet. Create your first component to get started.
          </CardContent>
        </Card>
      ) : (
        <Card>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Owner</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {components.map((component) => (
                  <TableRow key={component.id}>
                    <TableCell className="font-medium text-brand-navy">
                      {component.name}
                    </TableCell>
                    <TableCell>{component.owner}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      <CreateComponentDialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onCreated={fetchData}
      />
    </div>
  );
}
