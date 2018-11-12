# public schema

# --- !Ups

CREATE TABLE vk_user (
  id BIGINT PRIMARY KEY,
  first_name VARCHAR(1024) NOT NULL,
  last_name VARCHAR(1024) NOT NULL,
  access_token VARCHAR(1024)
);

# --- !Downs

DROP TABLE vk_user;