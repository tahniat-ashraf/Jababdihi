import type { PageResponse } from "@/lib/public-api";

export type AdminPendingIncident = {
  id: string;
  status: string;
  actorRole: string;
  title: string;
  summary: string;
  sourceCount: number;
  independentPublisherCount: number;
  confidenceScore: number | null;
  confidenceLevel: string | null;
  incidentDate: string | null;
  createdAt: string;
};

export type AdminSourceDetail = {
  id: string;
  publisherName: string | null;
  sourceTitle: string | null;
  sourceUrl: string;
  relevantExcerpt: string | null;
  aiRelevanceScore: number | null;
  publishedAt: string | null;
};

export type AdminIncidentDetail = {
  id: string;
  status: string;
  actorRole: string;
  title: string;
  summary: string;
  categories: string[];
  locationDistrict: string | null;
  locationDivision: string | null;
  sourceCount: number;
  independentPublisherCount: number;
  confidenceScore: number | null;
  confidenceLevel: string | null;
  incidentDate: string | null;
  createdAt: string;
  sources: AdminSourceDetail[];
};

export type AdminActionResponse = {
  id: string;
  status: string;
};

export type { PageResponse };
