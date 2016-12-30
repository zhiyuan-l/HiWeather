package com.vito.work.weather

import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.vito.work.weather.admin.config.SecurityConfig
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
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
open class AppStarter {

    @Bean
    open fun errorAttributes(): DefaultErrorAttributes
            = DefaultErrorAttributes()

    @Bean
    open fun charsetFilter(): Filter {
        val charFilter = CharacterEncodingFilter()
        charFilter.encoding = "UTF-8"
        charFilter.setForceEncoding(true)
        return charFilter
    }

    @Bean
    open fun securityConfig(): SecurityConfig
            = SecurityConfig()

    @Bean
    open fun objectMapperBuilder(): Jackson2ObjectMapperBuilder
            = Jackson2ObjectMapperBuilder().modulesToInstall(KotlinModule())

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val application = SpringApplication(AppStarter::class.java, *args)
            application.setAddCommandLineProperties(false)
            application.run()
        }
    }
}