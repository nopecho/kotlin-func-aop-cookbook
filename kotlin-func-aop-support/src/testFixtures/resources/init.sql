CREATE UNLOGGED TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    email      TEXT,
    created_at timestamptz
);

INSERT INTO users (id, email, created_at)
SELECT generate_series(1, 10000000)        AS seq,
       md5(random()::text) || '@email.com' AS dummy_email, -- 랜덤 문자열
       now()                               AS created_at
;