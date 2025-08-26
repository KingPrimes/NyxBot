package com.nyx.bot.common.core;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expiration; // 单位：秒

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
        // 确保密钥长度足够（HMAC-SHA256至少需要256位/32字节）
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    /**
     * 生成JWT令牌
     *
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return generateToken(claims, username);
    }

    /**
     * 生成带自定义声明的JWT令牌
     *
     * @param claims   自定义声明
     * @param username 用户名
     * @return JWT令牌
     */
    public String generateToken(Map<String, Object> claims, String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .claims(claims)          // 设置自定义声明
                .subject(username)       // 设置主题（用户名）
                .issuer("NyxBot")        // 设置签发者
                .issuedAt(now)           // 设置签发时间
                .expiration(expirationDate) // 设置过期时间
                .signWith(key)           // 使用密钥签名
                .compact();
    }

    /**
     * 从JWT令牌中提取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从JWT令牌中提取过期时间
     *
     * @param token JWT令牌
     * @return 过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从JWT令牌中提取自定义声明
     *
     * @param token          JWT令牌
     * @param claimsResolver 声明解析器
     * @return 声明值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析JWT令牌中的所有声明
     *
     * @param token JWT令牌
     * @return 所有声明
     * @throws JwtException 如果令牌无效或已过期
     */
    private Claims extractAllClaims(String token) {
        try {
            // 使用最新API替代弃用方法
            return Jwts.parser()
                    .verifyWith(key)       // 替代 setSigningKey(key)
                    .build()
                    .parseSignedClaims(token)  // 替代 parseClaimsJws(token)
                    .getPayload();             // 替代 getBody()
        } catch (ExpiredJwtException e) {
            throw new JwtException("Token已过期", e);
        } catch (JwtException e) {
            throw new JwtException("无效的Token", e);
        }
    }

    /**
     * 验证JWT令牌有效性
     *
     * @param token    JWT令牌
     * @param username 用户名
     * @return 如果令牌有效则返回true，否则返回false
     */
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * 检查JWT令牌是否已过期
     *
     * @param token JWT令牌
     * @return 如果令牌已过期则返回true，否则返回false
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 获取令牌剩余过期时间（秒）
     *
     * @param token JWT令牌
     * @return 剩余过期时间（秒），如果已过期则返回0
     */
    public long getRemainingExpirationSeconds(String token) {
        Date expirationDate = extractExpiration(token);
        long remainingMillis = expirationDate.getTime() - System.currentTimeMillis();
        return Math.max(0, remainingMillis / 1000);
    }
}
