create schema social_app;

create table users --пользователи
(
	id serial primary key,
	login text not null unique,
	password text not null,
	enabled boolean not null default true,
	"role" text not null default 'user'
);

create table profiles --профили пользователей. Данные используются на странице /profile 
(	
	user_id int primary key references users on delete cascade,
	name text not null,
	surname text not null,
	birth_date date not null,
	marital_status text not null,
	phone text not null,
	country text not null,
	city text not null,
	about text not null,
	image text not null	
);

create table wall_messages --сообщения на стене на странице профиля
(
	id serial primary key,
	txt text,
	image text,
	sender int not null references users  on delete cascade,
	receiver int not null references users  on delete cascade,
	dt timestamptz
);

create table photos --фотографии пользователя
(
	id serial primary key,
	user_id int not null references users on delete cascade,
	path text not null
);

create table channels( --каналы
	id serial primary key,
	title text not null,
	descr text not null,
	owner int not null references users on delete cascade
);

create table channel_wall_messages --сообщения на стене канала
(
	id serial primary key,
	txt text,
	image text,
	sender int not null references users on delete cascade,
	channel int not null references channels on delete cascade,
	dt timestamptz
);

create table channel_subscribers( --подписчики на канал
	id serial primary key,
	channel int not null references channels on delete cascade,
	user_id int not null references users on delete cascade
);

create table friends( --друзья пользователя
	id serial primary key,
	user1 int not null references users on delete cascade,
	user2 int not null references users on delete cascade,
	check(user1<>user2)
);

create table friend_request --запросы на дружбу
(
	id serial primary key,
	user_from int not null references users on delete cascade,
	user_to int not null references users on delete cascade
);

create table user_entities
(
    id           varchar(1000) not null,
    name         varchar(100)  not null,
    display_name varchar(200),
    primary key (id)
);

create table user_credentials
(
    credential_id                varchar(1000) not null,
    user_entity_user_id          varchar(1000) not null,
    public_key                   bytea         not null,
    signature_count              bigint,
    uv_initialized               boolean,
    backup_eligible              boolean       not null,
    authenticator_transports     varchar(1000),
    public_key_credential_type   varchar(100),
    backup_state                 boolean       not null,
    attestation_object           bytea,
    attestation_client_data_json bytea,
    created                      timestamp,
    last_used                    timestamp,
    label                        varchar(1000) not null,
    primary key (credential_id)
);

create table one_time_tokens(
    token_value varchar(36) not null primary key,
    username    varchar(50) not null,
    expires_at  timestamp   not null
);

create table user_sessions(
	id serial primary key,
	user_id int references users,
	session_id text not null,
	last_request timestamptz,
	ip text not null
);

create table persistent_logins (
	username varchar(64) not null,
	series varchar(64) primary key,
	token varchar(64) not null,
	last_used timestamp not null
);

create table user_activities(--UserActivityFilter хранит данные в этой таблице
	id serial primary key,
	user_id int references users not null,
	request text not null,
	dt timestamptz not null
);

create table request_cache( --DatabaseRequestCache хранит данные в этой таблице
	id serial primary key,
	session_id text not null,
	url text not null
);

create table user_privacy --настройки приватности, меняются на странице /settings
(
	id serial primary key,
	user_id int references users not null,
	birth_date text not null,
	marital_status text not null,
	phone text not null,
	about text not null
);

--настройка списков доступа
create table acl_sid(
    id bigserial not null primary key,
    principal boolean not null,
    sid varchar(100) not null,
    constraint unique_uk_1 unique(sid,principal)
);

create table acl_class(
    id bigserial not null primary key,
    class varchar(100) not null,
    class_id_type varchar(100),
    constraint unique_uk_2 unique(class)
);

create table acl_object_identity(
    id bigserial primary key,
    object_id_class bigint not null,
    object_id_identity varchar(36) not null,
    parent_object bigint,
    owner_sid bigint,
    entries_inheriting boolean not null,
    constraint unique_uk_3 unique(object_id_class,object_id_identity),
    constraint foreign_fk_1 foreign key(parent_object)references acl_object_identity(id),
    constraint foreign_fk_2 foreign key(object_id_class)references acl_class(id),
    constraint foreign_fk_3 foreign key(owner_sid)references acl_sid(id)
);

create table acl_entry(
    id bigserial primary key,
    acl_object_identity bigint not null,
    ace_order int not null,
    sid bigint not null,
    mask integer not null,
    granting boolean not null,
    audit_success boolean not null,
    audit_failure boolean not null,
    constraint unique_uk_4 unique(acl_object_identity,ace_order),
    constraint foreign_fk_4 foreign key(acl_object_identity) references acl_object_identity(id),
    constraint foreign_fk_5 foreign key(sid) references acl_sid(id)
);

create table auth_logs( --события аутентификации и выхода из системы
	id serial primary key,
	login text not null,
	description text not null,
	ts timestamptz not null default now(),
	is_success boolean not null
);

create table authz_logs( -- события авторизации
	id serial primary key,
	login text not null,
	description text not null,
	ts timestamptz not null default now(),
	is_success boolean not null
);