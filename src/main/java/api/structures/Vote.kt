package api.structures

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ed on 10.06.17.
 */
data class Vote(@param:JsonProperty("nickname") val nickname: String,
                @param:JsonProperty("voice") var voice: Int)