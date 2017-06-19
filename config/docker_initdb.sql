DROP SEQUENCE forum_id_seq;
DROP SEQUENCE post_id_seq;
DROP SEQUENCE thread_id_seq;
DROP SEQUENCE users_id_seq;

DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS forum;
DROP TABLE IF EXISTS post;
DROP TABLE IF EXISTS thread;
DROP TABLE IF EXISTS vote;

CREATE EXTENSION IF NOT EXISTS citext;

CREATE SEQUENCE forum_id_seq;
CREATE SEQUENCE post_id_seq;
CREATE SEQUENCE thread_id_seq;
CREATE SEQUENCE users_id_seq;

create table thread
(
  id serial not null
    constraint thread_pkey
    primary key,
  title text not null,
  forum text not null,
  message text,
  votes integer,
  slug text,
  created timestamp with time zone,
  author text
)
;

create index index_thread__slug
  on thread (lower(slug))
;

create table users
(
  id serial not null
    constraint users_pkey
    primary key,
  nickname citext not null
    constraint users_nickname_key
    unique,
  fullname varchar(40) not null,
  about text,
  email citext not null
    constraint users_email_key
    unique
)
;

create index index_user__email
  on users (lower(email::text))
;

create index index_user__nickname
  on users (lower(nickname::text))
;

create table forum
(
  id serial not null
    constraint forum_pkey
    primary key,
  title text not null,
  "user" text not null,
  slug text,
  posts integer default 0 not null,
  threads integer default 0 not null
)
;

create index index_forum__slug
  on forum (slug)
;

create table post
(
  id serial not null
    constraint post_pkey
    primary key,
  parent integer default 0,
  author varchar(255),
  message text,
  isedited boolean,
  forum varchar(255),
  created timestamp with time zone default now(),
  thread integer,
  path varchar(255)
)
;

create index index_post__parent_thread
  on post (parent, thread)
;

create index index_post__thread
  on post (thread)
;

create index index_post__created
  on post (created)
;

create table vote
(
  thread_id integer,
  nickname varchar(255),
  voice integer,
  id serial not null
    constraint vote_pkey
    primary key
)
;

create index index_vote__id_nickname
  on vote (id, lower(nickname::text))
;

create table link_user_forum
(
  id serial not null
    constraint link_user_forum_pkey
    primary key,
  userid integer,
  forum_slug citext,
  constraint link_user_forum_userid_forum_slug_key
  unique (userid, forum_slug)
)
;

create table count_parent_zero
(
  thread_id integer
    constraint count_parent_zero_thread_id_fkey
    references thread,
  count integer default 0 not null
)
;


