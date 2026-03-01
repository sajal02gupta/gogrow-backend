alter table users alter column created_at type timestamp with time zone;
alter table users alter column modified_at type timestamp with time zone;
alter table users alter column deleted_at type timestamp with time zone;

alter table otp_challenge alter column expires_at type timestamp with time zone;
alter table otp_challenge alter column consumed_at type timestamp with time zone;
alter table otp_challenge alter column created_at type timestamp with time zone;

alter table auth_session alter column created_at type timestamp with time zone;
alter table auth_session alter column last_active_at type timestamp with time zone;
alter table auth_session alter column revoked_at type timestamp with time zone;
