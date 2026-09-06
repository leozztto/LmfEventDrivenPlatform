package com.lmf.auth.authservice.infrastructure.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

/**
 * Fábrica do par RSA usado para assinar/validar os JWT. Gera um par efêmero ou reconstrói um a
 * partir de chaves PEM (base64 do DER, sem cabeçalhos {@code -----BEGIN...}).
 */
public final class RsaKeys {

    private RsaKeys() {
    }

    public static RSAKey generate() {
        try {
            return new RSAKeyGenerator(2048)
                    .keyID(UUID.randomUUID().toString())
                    .keyUse(KeyUse.SIGNATURE)
                    .generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Falha ao gerar par RSA para assinatura de JWT", e);
        }
    }

    public static RSAKey fromPem(String publicKeyBase64, String privateKeyBase64) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] publicDer = Base64.getMimeDecoder().decode(stripPem(publicKeyBase64));
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory
                    .generatePublic(new X509EncodedKeySpec(publicDer));

            byte[] privateDer = Base64.getMimeDecoder().decode(stripPem(privateKeyBase64));
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory
                    .generatePrivate(new PKCS8EncodedKeySpec(privateDer));

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.nameUUIDFromBytes(publicKey.getEncoded()).toString())
                    .keyUse(KeyUse.SIGNATURE)
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao carregar par RSA a partir de PEM", e);
        }
    }

    private static String stripPem(String pem) {
        return pem
                .replaceAll("-----BEGIN (RSA )?(PUBLIC|PRIVATE) KEY-----", "")
                .replaceAll("-----END (RSA )?(PUBLIC|PRIVATE) KEY-----", "")
                .replaceAll("\\s", "");
    }
}
