package api

import api.database.ForumDbManager
import api.database.PostDbManager
import api.database.ThreadDbManager
import api.database.UserDbManager
import api.structures.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ed on 10.06.17.
 */
@RestController
class AppController(@param:Autowired val userDb: UserDbManager,
                    @param:Autowired val forumDb: ForumDbManager,
                    @param:Autowired val threadDb: ThreadDbManager,
                    @param:Autowired val postDb: PostDbManager) {
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

    @Suppress("UNCHECKED_CAST")
    @RequestMapping(value = "api/thread/{slug_or_id}/create", method = arrayOf(RequestMethod.POST),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun createPosts(@PathVariable("slug_or_id") slugOrId: String, @RequestBody posts: List<Post>): ResponseEntity<Any> {
        if (posts.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        val thread = threadDb.get(slugOrId).body as? Thread
        thread ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        var id = postDb.getSeqId()
        val data = arrayListOf<PostExtended>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val created = dateFormat.format(java.sql.Timestamp(System.currentTimeMillis()))
        for (post in posts) {
            ++id
            if (post.parent == 0) {
                data.add(PostExtended(post.author, created, thread.forum!!, id, post.message, post.parent, thread.id!!, null, id))
            } else {
                val parentPost = postDb.get(post.parent).body as? Post
                if (parentPost == null || thread.id != parentPost.thread) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(null)
                }
                val path = postDb.getPath(post.parent)
                data.add(PostExtended(
                        post.author, created, thread.forum!!, id, post.message, post.parent, thread.id!!, path, (path.getArray() as Array<Int>)[0]))
            }
            post.created = created
            post.forum = thread.forum
            post.id = id
            post.thread = thread.id
        }
        val code = postDb.create(data)
        if (code == HttpStatus.CREATED) {
            return ResponseEntity.status(code).body(posts)
        }
        return ResponseEntity.status(code).body(null)
    }

    @RequestMapping(value = "api/thread/{slug_or_id}/vote", method = arrayOf(RequestMethod.POST),
            produces = arrayOf(MediaType.APPLICATION_JSON_VALUE), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun voteForThread(@PathVariable("slug_or_id") slugOrId: String, @RequestBody content: Vote): ResponseEntity<Any> {
        val result = threadDb.updateVote(content.nickname, content.voice, slugOrId)
        return ResponseEntity.status(result.status).body(result.body)
    }

    @RequestMapping(value = "api/thread/{slug_or_id}/details", produces = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun getThreadDetails(@PathVariable("slug_or_id") slugOrId: String): ResponseEntity<Any> {
        val result = threadDb.get(slugOrId)
        return ResponseEntity.status(result.status).body(result.body)
    }
}