package api.structures

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ed on 10.06.17.
 */
class Post(@param:JsonProperty("author") var author: String,
           @param:JsonProperty("created") var created: String?,
           @param:JsonProperty("forum") var forum: String?,
           @param:JsonProperty("id") var id: Int?,
           @param:JsonProperty("isEdited", defaultValue = "false") var isEdited: Boolean,
           @param:JsonProperty("message") var message: String,
           @param:JsonProperty("parent", defaultValue = "0") var parent: Int,
           @param:JsonProperty("thread") var thread: Int?)