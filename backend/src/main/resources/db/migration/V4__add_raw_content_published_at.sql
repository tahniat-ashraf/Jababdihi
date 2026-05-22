alter table raw_contents
    add column published_at timestamptz;

create index idx_raw_contents_published_at
    on raw_contents(published_at);
