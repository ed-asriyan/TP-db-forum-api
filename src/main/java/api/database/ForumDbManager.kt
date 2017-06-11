package api.database

import api.Result
import api.structures.Forum
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
class ForumDbManager(@param:Autowired val jdbcTemplate: JdbcTemplate) {
    private val read = { rs: ResultSet, _: Int ->
        Forum(rs.getInt("posts"), rs.getString("slug"),
                rs.getInt("threads"), rs.getString("title"), rs.getString("user"))
    }

    fun create(user: String, slug: String, title: String): Result {
        val sql = "INSERT INTO forums (\"user\", slug, title) " +
                "VALUES ((SELECT nickname FROM users WHERE nickname = '$user'), '$slug', '$title') RETURNING *"
        try {
            return Result(jdbcTemplate.queryForObject(sql, read), HttpStatus.CREATED)
        } catch (e: DuplicateKeyException) {
            return Result(get(slug).body, HttpStatus.CONFLICT)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }

    fun get(slug: String): Result {
        val sql = "SELECT * FROM forums WHERE slug = '$slug'"
        try {
            return Result(jdbcTemplate.queryForObject(sql, read), HttpStatus.OK)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }
}