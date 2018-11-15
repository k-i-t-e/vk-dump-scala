# public schema

# --- !Ups

ALTER TABLE vk_user ADD COLUMN last_accessed TIMESTAMP WITH TIME ZONE;

CREATE TABLE vk_group(
  id BIGINT PRIMARY KEY,
  domain VARCHAR(1024) NOT NULL,
  name VARCHAR(1024) NOT NULL,
  alias VARCHAR(1024) NOT NULL,
  fetched BOOLEAN DEFAULT FALSE,
  offset INTEGER
);

CREATE TABLE vk_user_group(
  user_id BIGINT NOT NULL REFERENCES vk_user(id),
  group_id BIGINT NOT NULL REFERENCES vk_group(id),
  CONSTRAINT pk_vk_user_group PRIMARY KEY (user_id, group_id)
);

# --- !Downs

ALTER TABLE vk_user DROP COLUMN last_accessed;

DROP TABLE vk_group;
DROP TABLE vk_user_group;