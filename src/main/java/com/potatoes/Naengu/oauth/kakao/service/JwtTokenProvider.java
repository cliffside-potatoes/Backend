package com.potatoes.Naengu.oauth.kakao.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    //로깅용 나중에 사용할 예정
    //private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Key key;

    public JwtTokenProvider(@Value("${custom.jwt.secretKey}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        //Hmac 용 key 객체로 생성
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String accessTokenGenerate(String subject, Date expiredAt) {
        return Jwts.builder()
                .setSubject(subject)	//uid
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
    public String refreshTokenGenerate(Date expiredAt) {
        return Jwts.builder()
                .setExpiration(expiredAt)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }
}
