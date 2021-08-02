package com.scb.authservice.config;

import com.scb.authservice.model.UserRole;
//import com.scb.authservice.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final String signingKey;

//    @Autowired
//    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    public WebSecurityConfiguration(@Value("${security.jwt.signing-key}") String signingKey) {
        this.signingKey = signingKey;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth.inMemoryAuthentication()
                .withUser("username").password(encoder.encode("password")).roles(UserRole.USER.name());
//        auth.userDetailsService(customUserDetailsService);
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
        .and()
                .addFilterAfter(new JwtAuthenticationFilter(authenticationManager(), signingKey), UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/v1/login").permitAll()
                .antMatchers("/v1/jwt/parse").permitAll()
                .anyRequest().authenticated();
    }
}