package com.hz.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class EnphaseJWTVerifier {
	private EnphaseJWTVerifier() {
		throw new IllegalStateException("Utility class");
	}

	private static PublicKey buildPublicKey(String publicKey) throws GeneralSecurityException {
		byte[] encoded = Decoders.BASE64.decode(publicKey);
		KeyFactory kf = KeyFactory.getInstance("EC");
		return kf.generatePublic(new X509EncodedKeySpec(encoded));
	}

	public static Claims verifyClaims(String userName, String publicKey, String serialNumber, String jws) throws GeneralSecurityException {
		return Jwts.parser()
				.verifyWith(EnphaseJWTVerifier.buildPublicKey(publicKey))
				.requireAudience(serialNumber)
				.requireIssuer("Entrez")
				.require("username", userName)
				.require("enphaseUser", "owner")
				.build()
				.parseSignedClaims(jws)
				.getPayload();
	}

}
