package api.database

import api.Result
import api.structures.Post
import api.structures.PostExtended
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ed on 10.06.17.
 */
@Service
class PostDbManager(@param:Autowired val jdbcTemplate: JdbcTemplate) {
    fun postsFlatSortSql(limit: Int, offset: Int, desc: Boolean, slugOrId: String): String {
        var sql = "SELECT author, created, forum, id, isEdited, message, parent, thread FROM posts WHERE thread = "
        if (slugOrId.matches("\\d+".toRegex())) {
            val id = slugOrId.toInt()
            sql += "$id"
        } else {
            sql += "(SELECT id FROM threads WHERE slug = '$slugOrId')"
        }
        sql += " ORDER BY created"
        if (desc) {
            sql += " DESC"
        }
        sql += ", id"
        if (desc) {
            sql += " DESC"
        }
        sql += " LIMIT $limit OFFSET $offset"
        return sql
    }

    fun postsTreeSortSql(limit: Int, offset: Int, desc: Boolean, slugOrId: String): String {
        var sql = "SELECT author, created, forum, id, isEdited, message, parent, thread FROM posts WHERE thread = "
        if (slugOrId.matches("\\d+".toRegex())) {
            val id = slugOrId.toInt()
            sql += "$id"
        } else {
            sql += "(SELECT id FROM threads WHERE slug = '$slugOrId')"
        }
        sql += " ORDER BY path"
        if (desc) {
            sql += " DESC"
        }
        sql += " LIMIT $limit OFFSET $offset"
        return sql
    }

    fun postsParentTreeSortSql(limit: Int, offset: Int, desc: Boolean, slugOrId: String): String {
        var sql = "SELECT author, created, forum, id, isEdited, message, parent, thread FROM posts " +
                "WHERE root_id IN (SELECT id FROM posts WHERE thread = "
        if (slugOrId.matches("\\d+".toRegex())) {
            val id = slugOrId.toInt()
            sql += "$id"
        } else {
            sql += "(SELECT id FROM threads WHERE slug = '$slugOrId')"
        }
        sql += " AND parent = 0 ORDER BY id"
        if (desc) {
            sql += " DESC"
        }
        sql += " LIMIT $limit OFFSET $offset)"
        sql += " ORDER BY path"
        if (desc) {
            sql += " DESC"
        }
        return sql
    }

    private val read = { rs: ResultSet, _: Int ->
        val timestamp = rs.getTimestamp("created")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        Post(rs.getString("author"), dateFormat.format(timestamp),
                rs.getString("forum"), rs.getInt("id"), rs.getBoolean("isEdited"),
                rs.getString("message"), rs.getInt("parent"), rs.getInt("thread"))
    }

    fun create(posts: ArrayList<PostExtended>): HttpStatus {
        val sql = "INSERT INTO posts (author, created, forum, message, parent, thread, path, root_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, array_append(?, ?), ?)"
        try {
            jdbcTemplate.dataSource.connection.use { connection ->
                connection.autoCommit = false
                try {
                    connection.prepareStatement(sql, Statement.NO_GENERATED_KEYS).use { preparedStatement ->
                        for (post in posts) {
                            preparedStatement.setString(1, post.author)
                            preparedStatement.setString(2, post.created)
                            preparedStatement.setString(3, post.forum)
                            preparedStatement.setString(4, post.message)
                            preparedStatement.setInt(5, post.parent)
                            preparedStatement.setInt(6, post.thread)
                            preparedStatement.setArray(7, post.path)
                            preparedStatement.setInt(8, post.id)
                            preparedStatement.setInt(9, post.root_id)
                            preparedStatement.addBatch()
                        }
                        preparedStatement.executeBatch()
                        connection.commit()
                    }
                } catch (e: SQLException) {
                    connection.rollback()
                } finally {
                    connection.autoCommit = true
                }
            }
        } catch (e: DuplicateKeyException) {
            return HttpStatus.CONFLICT
        } catch (e: DataAccessException) {
            return HttpStatus.NOT_FOUND
        }
        if (!posts.isEmpty()) {
            val count = posts.size
            val forum = posts[0].forum
            jdbcTemplate.update("UPDATE forums SET posts = posts + $count WHERE slug = '$forum'")
        }
        return HttpStatus.CREATED
    }

    fun getSeqId(): Int {
        val sql = "SELECT nextval('posts_id_seq')"
        return jdbcTemplate.queryForObject(sql, Int::class.java)
    }

    fun getPath(id: Int): java.sql.Array {
        val sql = "SELECT path FROM posts WHERE id = $id"
        return jdbcTemplate.queryForObject(sql, java.sql.Array::class.java)
    }

    fun get(id: Int): Result {
        val sql = "SELECT * FROM posts WHERE id = $id"
        try {
            return Result(jdbcTemplate.queryForObject(sql, read), HttpStatus.OK)
        } catch (e: DataAccessException) {
            return Result(null, HttpStatus.NOT_FOUND)
        }
    }

    fun sort(limit: Int, offset: Int, sort: String, desc: Boolean, slugOrId: String): List<Post> {
        when (sort) {
            "flat" -> return jdbcTemplate.query(postsFlatSortSql(limit, offset, desc, slugOrId), read)
            "tree" -> return jdbcTemplate.query(postsTreeSortSql(limit, offset, desc, slugOrId), read)
            "parent_tree" -> return jdbcTemplate.query(postsParentTreeSortSql(limit, offset, desc, slugOrId), read)
            else -> throw NoSuchFieldError()
        }
    }
}