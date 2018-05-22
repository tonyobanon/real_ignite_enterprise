package com.re.paas.gae_adapter.classes;

import java.util.List;

import com.google.common.collect.Lists;
import com.googlecode.objectify.stringifier.Stringifier;
import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.base.classes.ClientResources.ClientRBRefEntry;
import com.re.paas.internal.base.classes.ClientResources.ClientRBRefEntryType;

public class ClientRBRefStringifier implements Stringifier<ClientRBRef> {

	@Override
	public ClientRBRef fromString(String arg0) {
		
		if(arg0.contains("__")) {
			
			List<ClientRBRefEntry> values = Lists.newArrayList();
			
			for(String valueString : arg0.split("__")) {
				
				String[] arr = valueString.split("_");
				ClientRBRefEntry value = new ClientRBRefEntry(arr[0], ClientRBRefEntryType.valueOf(arr[1]));
				
				values.add(value);
			}
			
			 return new ClientRBRef().setValues(values);
			
		} else {
			
			String[] arr = arg0.split("_");
			ClientRBRefEntry value = new ClientRBRefEntry(arr[0], ClientRBRefEntryType.valueOf(arr[1]));
			
			return ClientRBRef.get(value) ;
		}
		
	}

	@Override
	public String toString(ClientRBRef arg0) {
		
		StringBuilder sb = new StringBuilder();
		
		List<ClientRBRefEntry> values = arg0.getValues();
		
		for(int i = 0; i < values.size(); i++) {
			ClientRBRefEntry value = values.get(i);
			
			if(i != 0) {
				sb.append("__");
			}

			String text = value.getText();
			String type = value.getType().name();
			
			sb.append(text).append("_").append(type);
		}

		return sb.toString();
	}

}
