
ALTER TABLE users
ADD COLUMN department VARCHAR(500) DEFAULT NULL;

ALTER TABLE users
ADD COLUMN unit VARCHAR(500) DEFAULT NULL;

ALTER TABLE users
ADD COLUMN position VARCHAR(500) DEFAULT NULL;