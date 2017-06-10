package api

import org.springframework.http.HttpStatus

/**
 * Created by ed on 10.06.17.
 */
data class Result(val body: Any?, val status: HttpStatus)