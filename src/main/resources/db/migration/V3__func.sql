CREATE OR REPLACE FUNCTION after_post_or_thread_insert()
  RETURNS TRIGGER AS $body$
BEGIN
  INSERT INTO forum_users (user_id, forum) VALUES ((SELECT id
                                                    FROM users
                                                    WHERE nickname = NEW.author), NEW.forum);
  RETURN NEW;
END;
$body$ LANGUAGE plpgsql;

CREATE TRIGGER post_trigger
AFTER INSERT ON posts
FOR EACH ROW EXECUTE PROCEDURE after_post_or_thread_insert();

CREATE TRIGGER thread_trigger
AFTER INSERT ON threads
FOR EACH ROW EXECUTE PROCEDURE after_post_or_thread_insert();

--

DROP FUNCTION IF EXISTS manage_votes( CITEXT, INTEGER, INTEGER );

CREATE OR REPLACE FUNCTION manage_votes(u_nickname CITEXT, thread_id INTEGER, vote INTEGER)
  RETURNS VOID AS $body$
BEGIN
  INSERT INTO votes (nickname, thread, voice) VALUES (u_nickname, thread_id, vote)
  ON CONFLICT (nickname, thread)
    DO UPDATE SET voice = vote;
  UPDATE threads
  SET votes = (SELECT SUM(voice)
               FROM votes
               WHERE thread = thread_id)
  WHERE id = thread_id;
END;
$body$ LANGUAGE plpgsql;