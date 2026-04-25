export enum ReleaseStatus {
  NEW = 'NEW',
  PLANNED = 'PLANNED',
  IN_PROGRESS = 'IN_PROGRESS',
  FINISHED = 'FINISHED',
  CANCELLED = 'CANCELLED',
}

export interface Environment {
  id: number;
  name: string;
  type: string;
  active: boolean;
}

export interface Component {
  id: number;
  name: string;
  pipelineUrl?: string;
  owner: string;
}

export interface ReleaseArtifact {
  id: number;
  componentId: number;
  componentName: string;
  version: string;
  pipelineUrl?: string;
  owner: string;
}

export interface Deployment {
  componentId: number;
  componentName: string;
  version: string;
  releaseId?: number;
  releaseName?: string;
  deployedAt: string;
}

export interface Release {
  id: number;
  name: string;
  status: ReleaseStatus;
  sitEnvironment?: Environment;
  uatEnvironment?: Environment;
  artifacts: ReleaseArtifact[];
  createdAt: string;
  updatedAt: string;
}
