package com.kyumall.kyumallcommon.auth.argumentResolver;

import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.auth.authentication.UserContext;
import com.kyumall.kyumallcommon.member.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    log.info("## LoginUserArgumentResolver supportsParameter");
    boolean hasAnnotation = parameter.hasParameterAnnotation(LoginUser.class);
    boolean hasParameter = AuthenticatedUser.class.isAssignableFrom(parameter.getParameterType());
    return hasAnnotation && hasParameter;
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
    log.info("## LoginUserArgumentResolver resolveArgument");
    AuthenticatedUser user = UserContext.getUser();
    return user;
  }
}
