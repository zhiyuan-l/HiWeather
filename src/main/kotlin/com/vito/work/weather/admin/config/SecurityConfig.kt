package com.vito.work.weather.admin.config

import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User

/**
 * Created by lingzhiyuan.
 * Date : 16/4/25.
 * Time : 下午3:57.
 * Description:
 *
 */

@Order(SecurityProperties.IGNORED_ORDER)
open class SecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests().mvcMatchers("/admin/**").hasRole("ADMIN").and().formLogin().defaultSuccessUrl("/admin/").loginPage("/admin/login").usernameParameter("username").passwordParameter("password").failureUrl("/admin/login?error").permitAll().and().logout().logoutUrl("/admin/logout").logoutSuccessUrl("/admin/login").permitAll()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        val admin = User.withUsername("admin").password("{noop}admin").roles("ADMIN").build()
        auth.inMemoryAuthentication().withUser(admin)
    }
}