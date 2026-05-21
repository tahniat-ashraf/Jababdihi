create table categories (
    code text primary key,
    label_en text not null,
    label_bn text not null,
    default_visible boolean not null default true,
    display_order integer not null
);

create table incident_categories (
    incident_id uuid not null references incidents(id) on delete cascade,
    category_code text not null references categories(code),
    primary key (incident_id, category_code)
);

create index idx_incident_categories_category_code on incident_categories(category_code);

insert into categories (code, label_en, label_bn, default_visible, display_order) values
    ('POLITICAL_VIOLENCE', 'Political Violence', 'রাজনৈতিক সহিংসতা', true, 10),
    ('KILLING', 'Killing / Murder', 'হত্যা / খুন', true, 20),
    ('MOB_VIOLENCE', 'Mob Violence / Lynching', 'গণপিটুনি / জনতার সহিংসতা', true, 30),
    ('SEXUAL_VIOLENCE', 'Sexual Violence', 'যৌন সহিংসতা', true, 40),
    ('CORRUPTION_BRIBERY', 'Corruption / Bribery', 'দুর্নীতি / ঘুষ', true, 50),
    ('EXTORTION', 'Extortion / Chanda', 'চাঁদাবাজি', true, 60),
    ('LAND_GRABBING', 'Land Grabbing / Property Capture', 'জমি দখল / সম্পত্তি দখল', true, 70),
    ('ARMED_THREAT_ATTACK', 'Armed Threat / Attack', 'সশস্ত্র হুমকি / হামলা', true, 80),
    ('ABDUCTION_CONFINEMENT', 'Abduction / Confinement', 'অপহরণ / আটক', true, 90),
    ('ROBBERY_MUGGING', 'Robbery / Mugging', 'ডাকাতি / ছিনতাই', false, 100),
    ('INTIMIDATION', 'Threat / Intimidation', 'হুমকি / ভয়ভীতি', true, 110),
    ('ABUSE_OF_POWER', 'Abuse of Power', 'ক্ষমতার অপব্যবহার', true, 120),
    ('ATTACK_ON_INSTITUTION', 'Attack on Institution', 'প্রতিষ্ঠানে হামলা', true, 130),
    ('COMMUNAL_RELIGIOUS_VIOLENCE', 'Communal / Religious Violence', 'সাম্প্রদায়িক / ধর্মীয় সহিংসতা', true, 140),
    ('DRUG_ARMS_CRIME', 'Drug / Illegal Arms Nexus', 'মাদক / অবৈধ অস্ত্র সংশ্লিষ্ট অপরাধ', false, 150),
    ('ELECTION_VIOLENCE', 'Election Violence / Obstruction', 'নির্বাচন সহিংসতা / বাধা', false, 160);
