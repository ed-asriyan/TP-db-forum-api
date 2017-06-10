package api

import api.database.UserDbManager
import api.models.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Created by ed on 10.06.17.
 */
@RestController
class AppController(@param:Autowired val userDb: UserDbManager) {
    @RequestMapping(value = "api/user/{nickname}/create", method = arrayOf(RequestMethod.POST),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun createUser(@PathVariable("nickname") nickname: String, @RequestBody content: User): ResponseEntity<Any> {
        val result = userDb.create(content.about, content.email, content.fullname, nickname)
        return ResponseEntity.status(result.status).body(result.body)
    }
}