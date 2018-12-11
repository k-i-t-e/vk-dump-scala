# public schema

# --- !Ups

ALTER TABLE vk_group ADD CONSTRAINT u_vk_group_domain UNIQUE (domain);

# --- !Downs

ALTER TABLE vk_group DROP CONSTRAINT u_vk_group_domain;