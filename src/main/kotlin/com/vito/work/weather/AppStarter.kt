package com.vito.work.weather

import com.vito.work.weather.admin.config.SecurityConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.CharacterEncodingFilter
import javax.servlet.Filter

/**
 * Created by lingzhiyuan.
 * Date : 16/3/31.
 * Time : 下午4:28.
 * Description:
 *
 */
@SpringBootApplication
class AppStarter {

    @Bean
    fun errorAttributes(): DefaultErrorAttributes
            = DefaultErrorAttributes()

    @Bean
    fun charsetFilter(): Filter {
        val charFilter = CharacterEncodingFilter()
        charFilter.encoding = "UTF-8"
        charFilter.setForceEncoding(true)
        return charFilter
    }

    @Bean
    fun securityConfig(): SecurityConfig
            = SecurityConfig()

}

fun main(args: Array<String>) {
    runApplication<AppStarter>(*args)
}