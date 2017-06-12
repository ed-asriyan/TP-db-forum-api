package api.database

import api.Result
import api.structures.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.sql.ResultSet

/**
 * Created by ed on 10.06.17.
 */
@Service
class UserDbManager(@param:Autowired val jdbcTemplate: JdbcTemplate) {
    private val read = { rs: ResultSet, _: Int ->
        User(rs.getString("about"), rs.getString("email"),
                rs.getString("fullname"), rs.getString("nickname"))
    }

    fun create(about: String, email: String, fullname: String, nickname: String): Result {
        val sql = "INSERT INTO users (about, email, fullname, nickname) " +
                "VALUES ('$about', '$email', '$fullname', '$nickname') RETURNING *"
        try {
            return Result(jdbcTemplate.queryForObject(sql, read), HttpStatus.CREATED)
        } catch (e: DuplicateKeyException) {
            return Result(getMany(nickname, email).body, HttpStatus.CONFLICT)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }

    fun getOne(nickname: String): Result {
        val sql = "SELECT * FROM users WHERE nickname = '$nickname'"
        try {
            return Result(jdbcTemplate.queryForObject(sql, read), HttpStatus.OK)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }

    fun getMany(nickname: String, email: String): Result {
        val sql = "SELECT * FROM users WHERE nickname = '$nickname' OR email = '$email'"
        try {
            return Result(jdbcTemplate.query(sql, read), HttpStatus.OK)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }

    fun update(about: String?, email: String?, fullname: String?, nickname: String): Result {
        var sql = "UPDATE users SET" + if (about != null) " about = '$about'," else " about = about,"
        sql += if (email != null) " email = '$email'," else " email = email,"
        sql += if (fullname != null) " fullname = '$fullname'" else " fullname = fullname"
        sql += " WHERE nickname = '$nickname' RETURNING *"
        try {
            return Result(jdbcTemplate.queryForObject(sql, read), HttpStatus.OK)
        } catch (e: DuplicateKeyException) {
            return Result(null, HttpStatus.CONFLICT)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }

    fun getAllByForum(limit: Int, since: String?, desc: Boolean, forum: String): Result {
        var sql = "SELECT * FROM users WHERE id IN (SELECT user_id FROM forum_users WHERE forum = '$forum')"
        if (since != null) {
            sql += " AND nickname" + if (desc) " <" else " >"
            sql += " '$since'"
        }
        sql += " ORDER BY nickname COLLATE ucs_basic" + if (desc) " DESC" else ""
        sql += " LIMIT $limit"
        try {
            return Result(jdbcTemplate.query(sql, read), HttpStatus.OK)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }

    fun count(): Int {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Int::class.java)
    }

    fun clear() {
        jdbcTemplate.execute("DELETE FROM users")
    }
}