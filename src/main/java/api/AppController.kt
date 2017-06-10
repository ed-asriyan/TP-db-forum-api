package api

import api.database.ForumDbManager
import api.database.UserDbManager
import api.models.Forum
import api.models.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Created by ed on 10.06.17.
 */
@RestController
class AppController(@param:Autowired val userDb: UserDbManager,
                    @param:Autowired val forumDb: ForumDbManager) {
    @RequestMapping(value = "api/user/{nickname}/create", method = arrayOf(RequestMethod.POST),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun createUser(@PathVariable("nickname") nickname: String, @RequestBody content: User): ResponseEntity<Any> {
        val result = userDb.create(content.about!!, content.email!!, content.fullname!!, nickname)
        return ResponseEntity.status(result.status).body(result.body)
    }

    @RequestMapping(value = "api/user/{nickname}/profile", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun getUserProfile(@PathVariable("nickname") nickname: String): ResponseEntity<Any> {
        val result = userDb.getOne(nickname)
        return ResponseEntity.status(result.status).body(result.body)
    }

    @RequestMapping(value = "api/user/{nickname}/profile", method = arrayOf(RequestMethod.POST),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun updateUserProfile(@PathVariable("nickname") nickname: String, @RequestBody content: User): ResponseEntity<Any> {
        val result = userDb.update(content.about, content.email, content.fullname, nickname)
        return ResponseEntity.status(result.status).body(result.body)
    }

    @RequestMapping(value = "api/forum/create", method = arrayOf(RequestMethod.POST),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun createForum(@RequestBody content: Forum): ResponseEntity<Any> {
        val result = forumDb.create(content.user, content.slug, content.title)
        return ResponseEntity.status(result.status).body(result.body)
    }
}