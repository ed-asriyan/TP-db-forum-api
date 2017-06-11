package api.structures

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ed on 10.06.17.
 */
data class Thread(@param:JsonProperty("author") var author: String?,
                  @param:JsonProperty("created") var created: String?,
                  @param:JsonProperty("forum") var forum: String?,
                  @param:JsonProperty("id") var id: Int?,
                  @param:JsonProperty("message") var message: String,
                  @param:JsonProperty("slug") var slug: String?,
                  @param:JsonProperty("title") var title: String,
                  @param:JsonProperty("votes") var votes: Int?)