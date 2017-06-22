DROP SEQUENCE post_id_seq;
DROP SEQUENCE forum_id_seq;
DROP SEQUENCE thread_id_seq;

DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS forum;
DROP TABLE IF EXISTS post;
DROP TABLE IF EXISTS thread;
DROP TABLE IF EXISTS vote;

CREATE SEQUENCE forum_id_seq;
CREATE SEQUENCE post_id_seq;
CREATE SEQUENCE thread_id_seq;
CREATE SEQUENCE users_id_seq;

CREATE EXTENSION IF NOT EXISTS citext;

create table client
(
  nickname citext not null
    constraint users_pkey
    primary key,
  fullname varchar(128) not null,
  about text,
  email citext not null
    constraint users_email_key
    unique
)
;

create table forum
(
  slug citext not null
    constraint forums_pkey
    primary key,
  title varchar(128) not null,
  user citext not null
    constraint forums_author_fkey
    references client,
  posts integer default 0 not null,
  threads integer default 0 not null
)
;

create table thread
(
  id serial not null
    constraint threads_pkey
    primary key,
  title varchar(128) not null,
  user citext not null
    constraint threads_author_fkey
    references client,
  forum citext not null
    constraint threads_forum_fkey
    references forum,
  message text not null,
  votes integer default 0 not null,
  slug citext
    constraint threads_slug_key
    unique,
  created timestamp with time zone default now() not null
)
;

create table post
(
  id serial not null
    constraint post_pkey
    primary key,
  parent integer default 0 not null,
  user citext not null
    constraint post_author_fkey
    references client,
  message text not null,
  isedited boolean default false not null,
  forum citext not null
    constraint post_forum_fkey
    references forum,
  thread_id integer not null
    constraint post_thread_id_fkey
    references thread,
  created timestamp with time zone default now() not null,
  path varchar(255)
)
;

create table votes
(
  user citext not null
    constraint votes_author_fkey
    references client
    on delete cascade,
  thread_id integer not null
    constraint votes_thread_id_fkey
    references thread
    on delete cascade,
  voice integer not null,
  constraint votes_author_thread_id_key
  unique (user, thread_id)
)
;

create table forum_user
(
  user citext not null,
  forum citext not null
)
;

create table thread_parent_zero
(
  thread_id integer
    constraint thread_parent_zero_thread_id_fkey
    references thread,
  count integer default 0
)
;



