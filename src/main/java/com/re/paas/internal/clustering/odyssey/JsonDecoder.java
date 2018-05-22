package com.re.paas.internal.clustering.odyssey;

import java.io.IOException;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;

public class JsonDecoder  {

	protected JsonObject decode(byte[] b) {
		
		return Json.createReader(new InputStream() {
			
			private int index;
			
			@Override
			public int read() throws IOException {
				while(index < b.length){
					return b[index];
				}
				return -1;
			}
		}).readObject();
		
	}
}