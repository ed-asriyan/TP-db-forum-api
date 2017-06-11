package api.structures

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ed on 10.06.17.
 */
data class User(@param:JsonProperty("about") var about: String?,
           @param:JsonProperty("email") var email: String?,
           @param:JsonProperty("fullname") var fullname: String?,
           @param:JsonProperty("nickname") var nickname: String?)