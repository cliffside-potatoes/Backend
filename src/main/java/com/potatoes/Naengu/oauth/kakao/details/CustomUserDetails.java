package com.potatoes.Naengu.oauth.kakao.details;

import com.potatoes.Naengu.oauth.kakao.domain.model.UserEntity;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final UserEntity user;

    public CustomUserDetails(UserEntity user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 아직 권한이 user말고 필요 없기 때문에 임의로 지정
        // -> 모든 로그인 사용자는 ROLE_USER를 갖게 된다.
        return List.of(() -> "ROLE_USER");
    }

    @Override
    public @Nullable String getPassword() {
        return ""; //jwt 기반이므로 password를 쓰지 않는다.
    }

    @Override
    public String getUsername() {
        return user.getProviderId().toString(); //userEntity에서 providerId가 전달된다.
    }
}
