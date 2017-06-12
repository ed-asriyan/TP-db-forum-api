SET SYNCHRONOUS_COMMIT = 'off';

CREATE EXTENSION IF NOT EXISTS CITEXT;

--

DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS forums CASCADE;
DROP TABLE IF EXISTS threads CASCADE;
DROP TABLE IF EXISTS posts CASCADE;
DROP TABLE IF EXISTS forum_users CASCADE;
DROP TABLE IF EXISTS votes CASCADE;

--

CREATE TABLE IF NOT EXISTS users (
  id       SERIAL PRIMARY KEY,
  about    TEXT DEFAULT NULL,
  email    CITEXT UNIQUE,
  fullname TEXT DEFAULT NULL,
  nickname CITEXT COLLATE ucs_basic UNIQUE
);

--

CREATE TABLE IF NOT EXISTS forums (
  "user"  CITEXT REFERENCES users (nickname) ON DELETE CASCADE  NOT NULL,
  posts   INTEGER DEFAULT 0,
  threads INTEGER DEFAULT 0,
  slug    CITEXT UNIQUE                                         NOT NULL,
  title   TEXT                                                  NOT NULL
);

--

CREATE TABLE IF NOT EXISTS threads (
  author  CITEXT REFERENCES users (nickname) ON DELETE CASCADE  NOT NULL,
  created TIMESTAMPTZ DEFAULT NOW(),
  forum   CITEXT REFERENCES forums (slug) ON DELETE CASCADE     NOT NULL,
  id      SERIAL PRIMARY KEY,
  message TEXT        DEFAULT NULL,
  slug    CITEXT UNIQUE,
  title   TEXT                                                  NOT NULL,
  votes   INTEGER     DEFAULT 0
);

--

CREATE TABLE IF NOT EXISTS posts (
  author   CITEXT REFERENCES users (nickname) ON DELETE CASCADE      NOT NULL,
  created  TIMESTAMPTZ DEFAULT NOW(),
  forum    CITEXT REFERENCES forums (slug) ON DELETE CASCADE         NOT NULL,
  id       SERIAL PRIMARY KEY,
  isEdited BOOLEAN     DEFAULT FALSE,
  message  TEXT        DEFAULT NULL,
  parent   INTEGER     DEFAULT 0,
  thread   INTEGER REFERENCES threads (id) ON DELETE CASCADE         NOT NULL,
  path     INTEGER [],
  root_id  INTEGER
);

--

CREATE TABLE IF NOT EXISTS forum_users (
  user_id INTEGER REFERENCES users (id) ON DELETE CASCADE,
  forum   CITEXT REFERENCES forums (slug) ON DELETE CASCADE
);

--

CREATE TABLE IF NOT EXISTS votes (
  nickname CITEXT REFERENCES users (nickname) ON DELETE CASCADE,
  thread   INTEGER REFERENCES threads (id) ON DELETE CASCADE,
  voice    INTEGER DEFAULT 0,
  CONSTRAINT unique_pair UNIQUE (nickname, thread)
);