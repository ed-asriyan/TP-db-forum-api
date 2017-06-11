package api

import api.database.ForumDbManager
import api.database.ThreadDbManager
import api.database.UserDbManager
import api.models.Forum
import api.models.Thread
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
                    @param:Autowired val forumDb: ForumDbManager,
                    @param:Autowired val threadDb: ThreadDbManager) {
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

    @RequestMapping(value = "api/forum/{slug}/details", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun getForumDetails(@PathVariable("slug") slug: String): ResponseEntity<Any> {
        val result = forumDb.get(slug)
        return ResponseEntity.status(result.status).body(result.body)
    }

    @RequestMapping(value = "api/forum/{slug}/create", method = arrayOf(RequestMethod.POST),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun createThread(@PathVariable("slug") slug: String, @RequestBody content: Thread): ResponseEntity<Any> {
        val result = threadDb.create(content.author, content.created, slug, content.message, content.slug, content.title)
        return ResponseEntity.status(result.status).body(result.body)
    }

    @RequestMapping(value = "api/forum/{slug}/threads", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun getForumThreads(@RequestParam(value = "limit", required = false, defaultValue = "100") limit: Int,
                        @RequestParam(value = "since", required = false) since: String?,
                        @RequestParam(value = "desc", required = false, defaultValue = "false") desc: Boolean,
                        @PathVariable("slug") slug: String): ResponseEntity<Any> {
        var result = forumDb.get(slug)
        if (result.body != null) {
            result = threadDb.getAllByForum(limit, since, desc, slug)
        }
        return ResponseEntity.status(result.status).body(result.body)
    }
}