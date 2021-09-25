
create table email_verifications (
    id serial primary key,
    user_id INT NOT NULL,
    email_address varchar(500)  NOT NULL,
    token varchar(300)  NOT NULL,
    date_issued INT NOT NULL,
    expiry_date INT NOT NULL,
    token_used INT NOT NULL
)
