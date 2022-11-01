package com.example.oauthjwt.token;

import com.example.oauthjwt.entity.Member;
import com.example.oauthjwt.entity.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {

    @Value("${jwt.token.refresh-token-expire-length}")
    private long refresh_token_expire_time;
    private String secretKey;
    
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    protected void init(
            @Value("${jwt.token.secret}") String secretKey) {
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public Token generateToken(String uid) {
        // Access : 10분 / Refresh : 3주
        long tokenPeriod = 1000L * 60L * 10L;
        long refreshPeriod = 1000L * 60L * 60L * 24L * 21L;

        // claim 에 email 정보 추가
        Claims claims = Jwts.claims().setSubject(uid);

        // claim 에 권한 정보 추가
        Member member = memberRepository.findByEmail(uid).orElseThrow(()-> new RuntimeException());
        claims.put("role", member.getRole());

        // Access, Refresh 토큰 생성 후 반환
        Date now = new Date();
        return new Token(
                Jwts.builder()
                        .setClaims(claims)
                        .setIssuedAt(now)
                        .setExpiration(new Date(now.getTime() + tokenPeriod))
                        .signWith(SignatureAlgorithm.HS256, secretKey)
                        .compact(),
                Jwts.builder()
                        .setClaims(claims)
                        .setIssuedAt(now)
                        .setExpiration(new Date(now.getTime() + refreshPeriod))
                        .signWith(SignatureAlgorithm.HS256, secretKey)
                        .compact());
    }

    public boolean verifyToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
            return claims.getBody()
                    .getExpiration()
                    .after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public Token tokenReissue(String token) {
        String email = getUid(token);
        // email 에 해당하는 refreshToken 가져오기
        String storedRefreshToken = redisTemplate.opsForValue().get(email);
        // email 에 해당하는 refreshToken 이 없거나 일치하지 않을 때
        if(storedRefreshToken == null || !storedRefreshToken.equals(token)) throw new RuntimeException();

        // Token 생성
        Token newToken = generateToken(email);

        Date expireDate = getExpiration(token);
        Date currentDate = new Date();
        // refreshToken 기간이 얼마남지 않았을 경우 (3일 미만)
        log.info("remain time = {} < {}", expireDate.getTime() - currentDate.getTime(), 1000 * 60 * 60 * 24 * 3);
        if (expireDate.getTime() - currentDate.getTime() < 1000 * 60 * 60 * 24 * 3) storeRefreshToken(email, newToken);
        // refreshToken 의 유효기간이 3일 이상 남았을 경우 (refreshToken NULL 값으로 설정함으로써 전송하지 않음)
        else newToken.setRefreshToken(null);

        return newToken;
    }

    private Date getExpiration(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getExpiration();
    }

    public String getUid(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }

    public void storeRefreshToken(String email, Token token) {
        redisTemplate.opsForValue().set(
                email,
                token.getRefreshToken(),
                refresh_token_expire_time,
                TimeUnit.SECONDS
        );
    }
}
