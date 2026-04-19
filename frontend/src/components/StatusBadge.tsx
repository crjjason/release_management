import { ReleaseStatus } from '@/lib/types';

const statusConfig: Record<ReleaseStatus, { label: string; classes: string }> = {
  [ReleaseStatus.NEW]: {
    label: 'New',
    classes: 'bg-blue-100 text-blue-800 border-blue-200',
  },
  [ReleaseStatus.PLANNED]: {
    label: 'Planned',
    classes: 'bg-purple-100 text-purple-800 border-purple-200',
  },
  [ReleaseStatus.IN_PROGRESS]: {
    label: 'In Progress',
    classes: 'bg-amber-100 text-amber-800 border-amber-200',
  },
  [ReleaseStatus.FINISHED]: {
    label: 'Finished',
    classes: 'bg-green-100 text-green-800 border-green-200',
  },
  [ReleaseStatus.CANCELLED]: {
    label: 'Cancelled',
    classes: 'bg-gray-100 text-gray-800 border-gray-200',
  },
};

export default function StatusBadge({ status }: { status: ReleaseStatus }) {
  const config = statusConfig[status];
  return (
    <span
      className={`inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold ${config.classes}`}
    >
      {config.label}
    </span>
  );
}
