package com.potatoes.Naengu.auth.token;

import jakarta.servlet.http.HttpServletRequest;

public interface TokenReader {

    String resolveBearerToken(HttpServletRequest request);

    Long extractSubjectAsLong(String token);
}
