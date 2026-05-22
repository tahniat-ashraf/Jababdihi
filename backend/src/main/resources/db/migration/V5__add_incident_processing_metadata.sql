alter table incidents
    add column political_accountability_link boolean not null default false,
    add column extraction_confidence numeric(4,2),
    add constraint incidents_extraction_confidence_check
        check (extraction_confidence is null or (extraction_confidence >= 0 and extraction_confidence <= 1));

alter table raw_contents
    add column political_accountability_link boolean not null default false,
    add column extracted_actor_role text,
    add column extracted_category_code text,
    add column extraction_confidence numeric(4,2),
    add column processed_incident_id uuid references incidents(id),
    add column processed_at timestamptz,
    add constraint raw_contents_extracted_actor_role_check
        check (extracted_actor_role is null or extracted_actor_role in ('GOVERNMENT', 'OPPOSITION', 'UNKNOWN')),
    add constraint raw_contents_extraction_confidence_check
        check (extraction_confidence is null or (extraction_confidence >= 0 and extraction_confidence <= 1));

create index idx_raw_contents_ai_processing_status
    on raw_contents(ai_processing_status, fetched_at);

create index idx_raw_contents_processed_incident_id
    on raw_contents(processed_incident_id);
