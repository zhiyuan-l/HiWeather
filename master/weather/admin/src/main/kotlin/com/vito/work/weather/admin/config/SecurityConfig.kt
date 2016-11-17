package com.vito.work.weather.admin.config

import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

/**
 * Created by lingzhiyuan.
 * Date : 16/4/25.
 * Time : 下午3:57.
 * Description:
 *
 */

@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
open class SecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
                .antMatchers("/static/**").permitAll()
                .anyRequest().access("hasRole('ADMIN')")
                .and()
                .formLogin().defaultSuccessUrl("/")
                .loginPage("/login")
                .usernameParameter("username").passwordParameter("password")
                .failureUrl("/login?error")
                .permitAll()
                .and()
                .logout().logoutUrl("/logout").logoutSuccessUrl("/login").permitAll()
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication().withUser("admin").password("admin")
                .roles("ADMIN", "USER")
        //.and().withUser("user").password("user").roles("USER")
    }
}