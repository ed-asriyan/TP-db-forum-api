package api.models

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ed on 10.06.17.
 */
data class Forum(@param:JsonProperty("posts") var posts: Int?,
            @param:JsonProperty("slug") var slug: String,
            @param:JsonProperty("threads") var threads: Int?,
            @param:JsonProperty("title") var title: String,
            @param:JsonProperty("user") var user: String)