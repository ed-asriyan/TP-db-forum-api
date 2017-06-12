package api.structures

/**
 * Created by ed on 11.06.17.
 */
data class PostDetailed(val author: User?,
                        val forum: Forum?,
                        val post: Post,
                        val thread: Thread?)