# public schema

# --- !Ups

ALTER TABLE image ADD COLUMN image_type INTEGER;

UPDATE image SET image_type = 1;

ALTER TABLE image ALTER COLUMN image_type SET NOT NULL;

# --- !Downs

ALTER TABLE image DROP COLUMN image_type;