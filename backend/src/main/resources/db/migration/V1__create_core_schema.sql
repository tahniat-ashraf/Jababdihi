create extension if not exists pgcrypto;

create table locations (
    id uuid primary key default gen_random_uuid(),
    country text not null default 'Bangladesh',
    division text,
    district text,
    upazila text,
    union_or_area text,
    latitude numeric(9,6),
    longitude numeric(9,6)
);

create table incidents (
    id uuid primary key default gen_random_uuid(),
    actor_role text not null,
    incident_date date,
    location_id uuid references locations(id),
    extracted_location_text text,
    confidence_score numeric(5,2),
    confidence_level text,
    status text not null,
    source_count integer not null default 0,
    independent_publisher_count integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint incidents_actor_role_check check (actor_role in ('GOVERNMENT', 'OPPOSITION', 'UNKNOWN')),
    constraint incidents_confidence_score_check check (confidence_score is null or (confidence_score >= 0 and confidence_score <= 100)),
    constraint incidents_status_check check (status in ('RAW_CAPTURED', 'AI_EXTRACTED', 'PENDING_REVIEW', 'AUTO_PUBLISHED', 'MANUALLY_PUBLISHED', 'REJECTED', 'ARCHIVED')),
    constraint incidents_source_count_check check (source_count >= 0),
    constraint incidents_independent_publisher_count_check check (independent_publisher_count >= 0)
);

create table incident_translations (
    incident_id uuid not null references incidents(id) on delete cascade,
    language_code text not null,
    title text not null,
    summary text not null,
    primary key (incident_id, language_code),
    constraint incident_translations_language_code_check check (language_code in ('bn', 'en'))
);

create table publishers (
    id uuid primary key default gen_random_uuid(),
    name text not null,
    type text not null,
    domain text,
    homepage_url text,
    logo_url text,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint publishers_type_check check (type in ('NEWSPAPER', 'OFFICIAL_GOVERNMENT', 'OFFICIAL_POLICE', 'COURT_OR_LEGAL'))
);

create table incident_sources (
    id uuid primary key default gen_random_uuid(),
    incident_id uuid not null references incidents(id) on delete cascade,
    publisher_id uuid not null references publishers(id),
    source_url text not null,
    canonical_url text not null,
    source_title text,
    relevant_excerpt text,
    published_at timestamptz,
    fetched_at timestamptz not null,
    source_type text not null,
    ai_relevance_score numeric(5,2),
    created_at timestamptz not null default now(),
    constraint incident_sources_ai_relevance_score_check check (ai_relevance_score is null or (ai_relevance_score >= 0 and ai_relevance_score <= 100)),
    constraint incident_sources_source_type_check check (source_type in ('NEWSPAPER', 'OFFICIAL_GOVERNMENT', 'OFFICIAL_POLICE', 'COURT_OR_LEGAL'))
);

create table raw_contents (
    id uuid primary key default gen_random_uuid(),
    publisher_id uuid not null references publishers(id),
    source_url text not null,
    canonical_url text not null,
    source_title text,
    extracted_text text,
    relevant_excerpt text,
    content_hash text not null,
    language_code text,
    fetched_at timestamptz not null,
    parsing_status text not null,
    ai_processing_status text not null,
    constraint raw_contents_language_code_check check (language_code is null or language_code in ('bn', 'en')),
    constraint raw_contents_publisher_canonical_url_key unique (publisher_id, canonical_url),
    constraint raw_contents_content_hash_key unique (content_hash)
);

create table processing_tasks (
    id uuid primary key default gen_random_uuid(),
    task_type text not null,
    payload jsonb not null,
    status text not null,
    attempt_count integer not null default 0,
    max_attempts integer not null default 5,
    available_at timestamptz not null,
    locked_at timestamptz,
    locked_by text,
    last_error text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint processing_tasks_attempt_count_check check (attempt_count >= 0),
    constraint processing_tasks_max_attempts_check check (max_attempts > 0)
);

create table ingestion_job_runs (
    id uuid primary key default gen_random_uuid(),
    job_type text not null,
    target_date date,
    status text not null,
    started_at timestamptz,
    finished_at timestamptz,
    error_message text,
    created_at timestamptz not null default now(),
    constraint ingestion_job_runs_status_check check (status in ('RUNNING', 'SUCCEEDED', 'FAILED', 'CANCELLED'))
);

create index idx_incidents_actor_role on incidents(actor_role);
create index idx_incidents_status on incidents(status);
create index idx_incidents_incident_date on incidents(incident_date);
create index idx_incidents_confidence_score on incidents(confidence_score);

create index idx_publishers_domain on publishers(domain);
create index idx_publishers_homepage_url on publishers(homepage_url);

create index idx_processing_tasks_status on processing_tasks(status);
