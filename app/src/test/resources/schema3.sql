CREATE TABLE "users" (
    "id" integer PRIMARY KEY,
    "username" varchar,
    "role" varchar,
    "created_at" timestamp
);

CREATE TABLE "posts" (
    "id" integer PRIMARY KEY,
    "title" varchar,
    "body" text CHECK (length(body) > 0) COMMENT 'Content of the post',
    "user_id" integer REFERENCES "users"("id"),
    "status" varchar,
    "created_at" timestamp
);

CREATE TABLE "follows" (
    "following_user_id" integer REFERENCES "users"("id"),
    "followed_user_id" integer REFERENCES "users"("id"),
    "created_at" timestamp
);