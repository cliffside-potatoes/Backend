package com.potatoes.Naengu.oauth.kakao.service;

import com.potatoes.Naengu.auth.token.TokenReader;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider implements TokenReader {
    //로깅용 나중에 사용할 예정
    //private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Key key;

    public JwtTokenProvider(@Value("${custom.jwt.secretKey}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        //Hmac 용 key 객체로 생성
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null) return null;
        if (header.startsWith("Bearer ")) return header.substring(7);
        return null;
    }

    public String accessTokenGenerate(String subject, Date expiredAt) {
        return Jwts.builder()
                .setSubject(subject)	//uid
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    public String refreshTokenGenerate(String subject, Date expiredAt) {
        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public void validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            //throw new GlobalException(GlobalErrorCode.AUTH_EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            //throw new GlobalException(GlobalErrorCode.AUTH_INVALID_TOKEN);
        }
    }

    public Long extractProviderId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)              // 서명 검증 키
                    .build()
                    .parseSignedClaims(token)     // 서명된 JWT 파싱
                    .getPayload();

            String subject = claims.getSubject();
            if (subject == null || subject.isBlank()) {
                //throw new GlobalException(GlobalErrorCode.AUTH_INVALID_TOKEN);
            }
            return Long.valueOf(subject);

        } catch (ExpiredJwtException e) {
            //throw new GlobalException(GlobalErrorCode.AUTH_EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            //throw new GlobalException(GlobalErrorCode.AUTH_INVALID_TOKEN);
        }
        return null;
    }

    @Override
    public Long extractSubjectAsLong(String token) {
        return extractProviderId(token);
    }

}
