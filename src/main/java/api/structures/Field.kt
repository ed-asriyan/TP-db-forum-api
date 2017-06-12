package api.structures

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Created by ed on 11.06.17.
 */
data class Field(@param:JsonProperty("message") val message: String?)