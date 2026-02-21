package com.potatoes.Naengu.oauth.kakao.config;

import com.potatoes.Naengu.oauth.kakao.filter.JwtAuthenticationFilter;
import com.potatoes.Naengu.oauth.kakao.service.JwtTokenProvider;
import com.potatoes.Naengu.oauth.kakao.service.MemberDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtTokenProvider jwtTokenProvider,
                                                   MemberDetailsService memberDetailsService) throws Exception {

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider, memberDetailsService);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // stateless
                .httpBasic(Customizer.withDefaults());

        http
                .authorizeHttpRequests(auth -> auth
                        // 로그인/회원가입/카카오 콜백 열어두기
                        .requestMatchers("/oauth/**","/login", "/healthcheck").permitAll()
                        // 인증 필요
                        .anyRequest().authenticated()
                );

        // JWT 필터는 UsernamePasswordAuthenticationFilter 앞에
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
