package com.example.oauthjwt.oauth;

import com.example.oauthjwt.entity.Member;
import com.example.oauthjwt.entity.MemberRepository;
import com.example.oauthjwt.token.Token;
import com.example.oauthjwt.token.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final TokenService tokenService;
    private final UserRequestMapper userRequestMapper;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        UserDto userDto = userRequestMapper.toDto(oAuth2User);

        // 최초 로그인이라면 회원가입 처리를 한다.
        if (memberRepository.findByEmail(userDto.getEmail()).isEmpty()) {
            Member newMember = Member.builder()
                                .name(userDto.getName())
                                .email(userDto.getEmail())
                                .role(Member.Role.USER)
                                .build();
            memberRepository.save(newMember);
        }

        // 토큰 생성
        Token token = tokenService.generateToken(userDto.getEmail());
        log.info("{}", token);

        // Redis에 Refresh Token 저장
        tokenService.storeRefreshToken(userDto.getEmail(), token);

        log.info("Redis 저장 완료");

        // Header 에 토큰 추가
        writeTokenResponse(response, token);
    }

    private void writeTokenResponse(HttpServletResponse response, Token token)
            throws IOException {

        response.addHeader("Auth", token.getToken());
        response.addHeader("Refresh", token.getRefreshToken());
        response.setContentType("application/json;charset=UTF-8");

        // Httpbody에 json 형태로 토큰 내용 추가
        var writer = response.getWriter();
        writer.println(objectMapper.writeValueAsString(token));
        writer.flush();
    }
}
