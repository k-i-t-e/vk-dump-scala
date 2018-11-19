# public schema

# --- !Ups

CREATE SEQUENCE s_image;

CREATE TABLE image (
  id BIGINT DEFAULT nextval('s_image') PRIMARY KEY,
  group_id BIGINT NOT NULL REFERENCES vk_group(id),
  urls TEXT NOT NULL,
  post_id BIGINT NOT NULL,
  thumbnail VARCHAR(2048) NOT NULL,
  created_date TIMESTAMP WITH TIME ZONE NOT NULL,
  CONSTRAINT unique_thumbnail_album_id UNIQUE (group_id, thumbnail)
);

# --- !Downs

DROP SEQUENCE s_image;
DROP TABLE image;