create table T_REGISTRATION_REQUEST (
    RRE_ID_C varchar(36) not null,
    RRE_EMAIL_C varchar(255) not null,
    RRE_USERNAME_C varchar(50) not null,
    RRE_PASSWORD_C varchar(255) not null,
    RRE_STATUS_C varchar(20) not null default 'PENDING',
    RRE_CREATEDATE_D datetime not null,
    RRE_UPDATEDATE_D datetime not null,
    RRE_REASON_C varchar(1000),
    primary key (RRE_ID_C)
);

create index IDX_RRE_EMAIL on T_REGISTRATION_REQUEST(RRE_EMAIL_C);
create index IDX_RRE_STATUS on T_REGISTRATION_REQUEST(RRE_STATUS_C);

update T_CONFIG set CFG_VALUE_C = '32' where CFG_ID_C = 'DB_VERSION'; 