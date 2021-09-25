
CREATE TABLE users
(
     id SERIAL PRIMARY KEY,
     email VARCHAR(500) DEFAULT NULL,
     first_name VARCHAR(500) DEFAULT NULL,
     middle_name VARCHAR(500) DEFAULT NULL,
     last_name VARCHAR(500) DEFAULT NULL,
     dob date DEFAULT NULL,
     password VARCHAR(300) DEFAULT NULL,
     phone VARCHAR(50) DEFAULT NULL,
     gender VARCHAR(100) DEFAULT NULL,
     occupation VARCHAR(200) DEFAULT NULL,
     marital_status VARCHAR(100) DEFAULT NULL,
     address VARCHAR(500) DEFAULT NULL,
     country VARCHAR(70) DEFAULT NULL,
     city VARCHAR(100) DEFAULT NULL,
     postal_code VARCHAR(50) DEFAULT NULL,
     state_of_origin VARCHAR(200) DEFAULT NULL,
     lga_of_origin VARCHAR(200) DEFAULT NULL,
     last_change_password_date date DEFAULT NULL,
     first_time_login INT NOT NULL DEFAULT 1
);

CREATE TABLE permissions
(
     id SERIAL PRIMARY KEY,
     value VARCHAR(500) NOT NULL
);

CREATE TABLE roles
(
     id SERIAL PRIMARY KEY,
     value VARCHAR(500) NOT NULL
);

CREATE TABLE user_permissions
(
     id SERIAL PRIMARY KEY,
     permission_id INT NOT NULL,
     user_id INT NOT NULL
);

CREATE TABLE user_roles
(
     id SERIAL PRIMARY KEY,
     role_id INT NOT NULL,
     user_id INT NOT NULL
);

INSERT INTO roles(value)
VALUES ('ADMIN');

INSERT INTO roles(value)
VALUES ('USER');

