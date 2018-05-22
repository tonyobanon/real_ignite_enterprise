package com.re.paas.internal.cloud_provider.aws;

import javax.json.Json;
import javax.json.JsonObject;

public class Ec2InstanceCredential {

	private final String name;
	private final String keyFingerprint;
	private final String keyMaterial;

	public Ec2InstanceCredential(String name, String keyFingerprint, String keyMaterial) {
		this.name = name;
		this.keyFingerprint = keyFingerprint;
		this.keyMaterial = keyMaterial;
	}

	public String getName() {
		return name;
	}

	/**
	 * The SHA-1 digest of the DER encoded private key.
	 */
	public String getKeyFingerprint() {
		return keyFingerprint;
	}

	/**
	 * An unencrypted PEM encoded RSA private key
	 */
	public String getKeyMaterial() {
		return keyMaterial;
	}

	@Override
	public final String toString() {
		JsonObject o = Json.createObjectBuilder().add("name", name).add("fingerprint", keyFingerprint)
				.add("material", keyMaterial).build();
		return o.toString();
	}

}
