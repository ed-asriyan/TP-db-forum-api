package api.database

import api.Result
import api.structures.Thread
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ed on 10.06.17.
 */
@Service
class ThreadDbManager(@param:Autowired val jdbcTemplate: JdbcTemplate) {
    private val read = { rs: ResultSet, _: Int ->
        val timestamp = rs.getTimestamp("created")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        Thread(rs.getString("author"), dateFormat.format(timestamp.time),
                rs.getString("forum"), rs.getInt("id"), rs.getString("message"),
                rs.getString("slug"), rs.getString("title"), rs.getInt("votes"))
    }

    fun create(author: String, created: String?, forum: String, message: String, slug: String?, title: String): Result {
        var sql = "INSERT INTO threads (author," + if (created != null) " created," else ""
        sql += "  forum, message,"
        sql += if (slug != null) " slug," else ""
        sql += " title)"
        sql += " VALUES ((SELECT nickname FROM users WHERE nickname = '$author'),"
        sql += if (created != null) " '$created'," else ""
        sql += " (SELECT slug FROM forums WHERE slug = '$forum'), '$message',"
        sql += if (slug != null) " '$slug'," else ""
        sql += " '$title') RETURNING *"
        try {
            jdbcTemplate.update("UPDATE forums SET threads = threads + 1 WHERE slug = '$forum'")
            return Result(jdbcTemplate.queryForObject(sql, read), HttpStatus.CREATED)
        } catch (e: DuplicateKeyException) {
            return Result(get(slug!!).body, HttpStatus.CONFLICT)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }

    fun get(slugOrId: String): Result {
        val sql: String
        if (slugOrId.matches("\\d+".toRegex())) {
            sql = "SELECT * FROM threads WHERE id = $slugOrId"
        } else {
            sql = "SELECT * FROM threads WHERE slug = '$slugOrId'"
        }
        try {
            return Result(jdbcTemplate.queryForObject(sql, read), HttpStatus.OK)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }

    fun getAllByForum(limit: Int, since: String?, desc: Boolean, forum: String): Result {
        var sql = "SELECT * FROM threads WHERE forum = '$forum'"
        if (since != null) {
            sql += " AND created" + if (desc) " <=" else " >="
            sql += " '$since'"
        }
        sql += " ORDER BY created" + if (desc) " DESC" else ""
        sql += " LIMIT $limit"
        try {
            return Result(jdbcTemplate.query(sql, read), HttpStatus.OK)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }

    fun updateVote(nickname: String, voice: Int, slugOrId: String): Result {
        try {
            val threadId: Int
            if (slugOrId.matches("\\d+".toRegex())) {
                threadId = slugOrId.toInt()
            } else {
                threadId = jdbcTemplate.queryForObject("SELECT id FROM threads WHERE slug = '$slugOrId'", Int::class.java)
            }
            val sql = "SELECT update_or_insert_votes('$nickname', $threadId, $voice)"
            jdbcTemplate.execute(sql)
        } catch (e: DuplicateKeyException) {
            return Result(get(slugOrId).body, HttpStatus.CONFLICT)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.CONFLICT)
        }
        return Result(get(slugOrId).body, HttpStatus.OK)
    }

    fun update(message: String?, title: String?, slugOrId: String): Result {
        var sql = "UPDATE threads SET"
        sql += if (message != null) " message = '$message'," else " message = message,"
        sql += if (title != null) " title = '$title'" else " title = title"
        sql += " WHERE"
        if (slugOrId.matches("\\d+".toRegex())) {
            val id = slugOrId.toInt()
            sql += " id = $id"
        } else {
            sql += " slug = '$slugOrId'"
        }
        sql += " RETURNING *"
        try {
            return Result(jdbcTemplate.queryForObject(sql, read), HttpStatus.OK)
        } catch (e: DuplicateKeyException) {
            return Result(get(slugOrId).body, HttpStatus.CONFLICT)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }
}