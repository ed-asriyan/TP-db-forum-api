package api.structures

/**
 * Created by ed on 10.06.17.
 */
data class PostExtended(val author: String,
                        val created: String,
                        val forum: String,
                        val id: Int,
                        val message: String,
                        val parent: Int,
                        val thread: Int,
                        val path: java.sql.Array?,
                        val root_id: Int)