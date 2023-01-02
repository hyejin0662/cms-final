package com.zerobase.domain.config;

import com.zerobase.domain.common.UserType;
import com.zerobase.domain.common.UserVo;
import com.zerobase.domain.util.Aes256Util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Objects;

public class JwtAuthenticationProvider {
    private String secretKey = "secretKey";

    private long tokenValidTime = 1000 * 60 * 60 * 24;  // 토큰만료기간 1 day

    /**
     * 토큰 발행
     */
    public String createToken(String userPk, Long id, UserType userType) {
        Claims claims = Jwts.claims()
                .setSubject(Aes256Util.encrypt(userPk)).setId(Aes256Util.encrypt(id.toString()));
        claims.put("roles", userType);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * 토큰 유효성 검사
     */
    public boolean validateToken(String jwtToken) {
        try{
            Jws<Claims> claimsJws =
                    Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken);
            return !claimsJws.getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰에서 사용자 정보 반환
     */
    public UserVo getUserVO(String token) {
        Claims c = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        return new UserVo(Long.valueOf(Objects.requireNonNull(Aes256Util.decrypt(c.getId()))),
                Aes256Util.decrypt(c.getSubject()));
    }
}
