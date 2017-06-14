DROP INDEX IF EXISTS forums_user_idx;
DROP INDEX IF EXISTS threads_author_idx;
DROP INDEX IF EXISTS threads_forum_idx;
DROP INDEX IF EXISTS posts_author_idx;
DROP INDEX IF EXISTS posts_forum_idx;
DROP INDEX IF EXISTS posts_thread_path_idx;
DROP INDEX IF EXISTS posts_root_id_path_idx;
DROP INDEX IF EXISTS votes_thread_idx;
DROP INDEX IF EXISTS forum_users_user_id_idx;
DROP INDEX IF EXISTS forum_users_forum_idx;

--

CREATE INDEX ON forums ("user");

--

CREATE INDEX ON threads (author);
CREATE INDEX ON threads (forum);
CREATE INDEX ON posts (thread, path);
CREATE INDEX ON posts (root_id, path);

--

CREATE INDEX ON posts (author);
CREATE INDEX ON posts (forum);

--

CREATE INDEX ON votes (thread);

--

CREATE INDEX ON forum_users (user_id);
CREATE INDEX ON forum_users (forum);
