package api

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

/**
 * Created by ed on 10.06.17.
 */
@SpringBootApplication
open class Application {
    companion object {
        @Throws(Exception::class)
        @JvmStatic fun main(args: Array<String>) {
            SpringApplication.run(Application::class.java, *args)
        }
    }
}