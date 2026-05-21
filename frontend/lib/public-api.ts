export type ActorRole = "GOVERNMENT" | "OPPOSITION";

export type LanguageCode = "bn" | "en";

export type SortOption = "RECOMMENDED" | "NEWEST" | "MOST_CORROBORATED";

export type Category = {
  code: string;
  label: string;
  defaultVisible: boolean;
};

export type CategoriesResponse = {
  items: Category[];
};

export type LocationSummary = {
  country?: string | null;
  division?: string | null;
  district?: string | null;
  upazila?: string | null;
  displayText?: string | null;
};

export type Confidence = {
  score?: number | null;
  label?: string | null;
  explanation?: string | null;
};

export type SourcePreview = {
  publisherName?: string | null;
  url: string;
};

export type IncidentSummary = {
  id: string;
  actorRole: ActorRole;
  actorColor: string;
  title: string;
  summary: string;
  categories: string[];
  incidentDate?: string | null;
  location?: LocationSummary | null;
  confidence: Confidence;
  sourcesPreview: SourcePreview[];
  remainingSourceCount: number;
  detailUrl: string;
};

export type PageResponse<T> = {
  items: T[];
  page: number;
  pageSize: number;
  totalPages: number;
  totalItems: number;
};

export type ApiError = {
  message: string;
};
