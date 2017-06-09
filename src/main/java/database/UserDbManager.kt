package database

import models.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.ResultSet

/**
 * Created by ed on 10.06.17.
 */
class UserDbManager(@param:Autowired var namedTemplate: JdbcTemplate) {
    var read = { rs: ResultSet, _: Int ->
        User(rs.getString("about"), rs.getString("email"),
                rs.getString("fullname"), rs.getString("nickname"))
    }

    fun create(content: User): User {
        val sql = "INSERT INTO users (about, email, fullname, nickname) VALUES (?, ?, ?, ?)"
        return namedTemplate.queryForObject(sql, arrayOf(content.about, content.email, content.fullname, content.nickname), read)
    }
}