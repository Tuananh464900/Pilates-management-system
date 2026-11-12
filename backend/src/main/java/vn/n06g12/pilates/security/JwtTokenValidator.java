package vn.n06g12.pilates.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtTokenValidator {

    public static final String INSECURE_DEFAULT_SECRET = "default-insecure-secret-key-change-it-in-production-32chars";

    // Supabase Auth ES256 Public Key (P-256 coordinates from JWKS endpoint)
    private static final String DEFAULT_EC_X = "6oQ3pVhdrSa_2EVmEj5HFO46wpvljCNXvmrASWQUuPM";
    private static final String DEFAULT_EC_Y = "CGopQKJVfegfWNVoqs6i5ajuoNp2_sWJQ-_vXKKCFxQ";

    private final SecretKey secretKey;
    private final PublicKey ecPublicKey;

    public JwtTokenValidator(@Value("${supabase.jwt-secret:}") String jwtSecret, Environment environment) {
        boolean isDevOrTest = isDevOrTestEnvironment(environment);

        if (jwtSecret == null || jwtSecret.trim().isEmpty() || INSECURE_DEFAULT_SECRET.equals(jwtSecret.trim())) {
            if (!isDevOrTest) {
                throw new IllegalStateException(
                        "BẢO MẬT PRODUCTION: Biến môi trường SUPABASE_JWT_SECRET bắt buộc phải được thiết lập hợp lệ " +
                        "(tối thiểu 32 ký tự) và tuyệt đối không được dùng secret mặc định!"
                );
            }
            jwtSecret = "temporary-dev-test-secret-key-at-least-32-bytes-long!";
        }

        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            if (!isDevOrTest) {
                throw new IllegalStateException("BẢO MẬT PRODUCTION: SUPABASE_JWT_SECRET phải có độ dài tối thiểu 32 ký tự (256-bit)");
            }
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            this.secretKey = Keys.hmacShaKeyFor(padded);
        } else {
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        }

        this.ecPublicKey = buildEcPublicKey(DEFAULT_EC_X, DEFAULT_EC_Y);
    }

    private PublicKey buildEcPublicKey(String xBase64, String yBase64) {
        try {
            BigInteger x = new BigInteger(1, Base64.getUrlDecoder().decode(xBase64));
            BigInteger y = new BigInteger(1, Base64.getUrlDecoder().decode(yBase64));
            ECPoint ecPoint = new ECPoint(x, y);
            AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
            parameters.init(new ECGenParameterSpec("secp256r1"));
            ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
            ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(ecPoint, ecParameters);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePublic(pubKeySpec);
        } catch (Exception e) {
            log.warn("Could not build EC public key: {}", e.getMessage());
            return null;
        }
    }

    private boolean isDevOrTestEnvironment(Environment environment) {
        if (environment == null) return false;
        String[] profiles = environment.getActiveProfiles();
        if (profiles == null || profiles.length == 0) return false;
        for (String p : profiles) {
            if ("prod".equalsIgnoreCase(p) || "production".equalsIgnoreCase(p)) return false;
        }
        for (String p : profiles) {
            if ("dev".equalsIgnoreCase(p) || "development".equalsIgnoreCase(p) || "test".equalsIgnoreCase(p)) return true;
        }
        return false;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .keyLocator(header -> {
                    String alg = header.getAlgorithm();
                    if ("ES256".equalsIgnoreCase(alg) && ecPublicKey != null) {
                        return ecPublicKey;
                    }
                    return secretKey;
                })
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        try {
            Claims claims = parseClaims(token);
            String sub = claims.getSubject();
            return UUID.fromString(sub);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    public String extractEmail(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.get("email", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Cannot extract email from JWT: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }

    // Helper sinh token dùng cho kiểm thử (Integration Test)
    public String generateTokenForTest(UUID userId, String email, long expirationMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
}
