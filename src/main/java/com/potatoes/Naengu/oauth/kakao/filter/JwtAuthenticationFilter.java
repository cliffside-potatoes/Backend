package com.potatoes.Naengu.oauth.kakao.filter;

import com.potatoes.Naengu.oauth.kakao.service.JwtTokenProvider;
import com.potatoes.Naengu.oauth.kakao.service.MemberDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDetailsService memberDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   MemberDetailsService memberDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberDetailsService = memberDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = jwtTokenProvider.resolveBearerToken(request);

        // 토큰이 없으면 그냥 통과 (비로그인 요청)
        if (StringUtils.hasText(token)) {
            // 1) 유효성 검사 (여기서 만료/위조면 GlobalException 발생)
            jwtTokenProvider.validateToken(token);

            // 2) 토큰에서 memberId 추출 (subject에 memberId 넣는 방식)
            Long memberId = jwtTokenProvider.extractProviderId(token);

            // 3) DB에서 UserDetails 로드
            UserDetails userDetails = memberDetailsService.loadUserByUsername(memberId.toString());

            // 4) SecurityContext에 Authentication 세팅 (이게 "인가 준비"의 핵심)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
