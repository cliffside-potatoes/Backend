package com.potatoes.Naengu.auth.resolver;

import com.potatoes.Naengu.auth.annotation.AuthFridge;
import com.potatoes.Naengu.auth.service.AuthService;
import com.potatoes.Naengu.fridge.domain.model.Fridge;
import com.potatoes.Naengu.fridge.repository.FridgeRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthFridgeArgumentResolver implements HandlerMethodArgumentResolver {

    private final AuthService authService;
    private final FridgeRepository fridgeRepository;

    public AuthFridgeArgumentResolver(AuthService authService, FridgeRepository fridgeRepository) {
        this.authService = authService;
        this.fridgeRepository = fridgeRepository;
    }

    //어떤 파라미터에 발동할지
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthFridge.class)
                && Fridge.class.isAssignableFrom(parameter.getParameterType());
    }

    //실제로 Fridge 객체를 만들어서 주입
    @Override
    public @Nullable Object resolveArgument(
            MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory)
            throws Exception
    {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        Long fridgeId = authService.resolveFridgeId(request);
        return fridgeRepository.getReferenceById(fridgeId);
    }
}
