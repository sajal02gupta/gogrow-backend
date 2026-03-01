create schema if not exists core;
create schema if not exists auth;

alter table users set schema core;
alter table otp_challenge set schema auth;
alter table auth_session set schema auth;
