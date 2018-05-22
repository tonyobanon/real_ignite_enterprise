package com.re.paas.internal.clustering.odyssey;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class JsonEncoder{

	public static void encode(Map<String, String> obj, Consumer<Integer> byteConsumer){
		
		JsonObjectBuilder o = Json.createObjectBuilder();
		
		for(Entry<String, String> e : obj.entrySet()){
			o.add(e.getKey(), e.getValue());
		}
		
		Json.createWriter(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				byteConsumer.accept(b);
			}
		}).writeObject(o.build());		
	}

}
