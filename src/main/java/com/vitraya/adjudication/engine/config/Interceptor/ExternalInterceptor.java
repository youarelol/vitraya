package com.vitraya.adjudication.engine.config.Interceptor;


import com.vitraya.adjudication.engine.dto.enums.VitrayaErrorCodes;
import com.vitraya.adjudication.engine.dto.request.UserRequestDto;
import com.vitraya.adjudication.engine.helper.VitrayaException;
import com.vitraya.adjudication.engine.mysql.entity.Users;
import com.vitraya.adjudication.engine.mysql.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ExternalInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    @Value("${dashboard.external.token}")
    private String externalToken;

    public ExternalInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception, VitrayaException {

        String accessTokenProvided = externalToken;
        String authToken = request.getHeader("X-Auth-Token");

        if (StringUtils.isEmpty(authToken)) {
            throw new VitrayaException(VitrayaErrorCodes.NO_TOKEN);
        }

        if (!accessTokenProvided.equalsIgnoreCase(authToken)) {
            throw new VitrayaException(VitrayaErrorCodes.INVALID_TOKEN);
        }

        Users user = userRepository.findByToken(authToken);
        UserRequestDto userRequestDTO = new UserRequestDto();
        userRequestDTO.setUserData(user);
        request.setAttribute("userDTO", userRequestDTO);
        return true;
    }

}
