create table ingestion_cursors (
    id uuid primary key default gen_random_uuid(),
    job_type text not null,
    publisher_id uuid not null references publishers(id),
    cursor_published_at timestamptz,
    locked_by text,
    locked_at timestamptz,
    heartbeat_at timestamptz,
    last_run_status text,
    last_error text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint ingestion_cursors_job_type_check
        check (job_type in ('CURRENT_INGESTION', 'BACKFILL')),
    constraint ingestion_cursors_status_check
        check (last_run_status is null or last_run_status in ('SUCCEEDED', 'FAILED', 'SKIPPED_ALREADY_RUNNING'))
);

create unique index idx_ingestion_cursors_job_type_publisher
    on ingestion_cursors(job_type, publisher_id);

create index idx_ingestion_cursors_locked_at
    on ingestion_cursors(locked_at);
