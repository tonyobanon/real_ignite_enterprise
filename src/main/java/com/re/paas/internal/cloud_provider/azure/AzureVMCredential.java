package com.re.paas.internal.cloud_provider.azure;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.re.paas.internal.utils.RSAKeyPair;

public class AzureVMCredential {

	private String username;
	private String password;

	private RSAKeyPair keyPair;

	public AzureVMCredential(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public final String toString() {
		
		JsonObjectBuilder builder = Json.createObjectBuilder()
				.add("username", username)
				.add("password", password);
		
		if(keyPair != null){
			builder.add("keyPair", keyPair.getJSON());
		}

		JsonObject o = builder.build();
		return o.toString();
	}

	public RSAKeyPair getKeyPair() {
		return keyPair;
	}

	public void withKeyPair(RSAKeyPair keyPair) {
		this.keyPair = keyPair;
	}

}
