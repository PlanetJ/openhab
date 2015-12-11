package org.openhab.binding.isy.internal.event;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.isy.internal.ISYEvent;

public class ISYEvents {
	
	private static Map<String, Class<? extends ISYEvent>> events = new HashMap<String, Class<? extends ISYEvent>>();
	
	public static ISYEvent create(String controlString){
		if(events.containsKey(controlString)){
			try {
				return events.get(controlString).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	static void registerEvent(Class<? extends ISYEvent> eventClazz){
		try {
			events.put(eventClazz.newInstance().getControlString(), eventClazz);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	static {
		registerEvent(StatusEvent.class);
	}
	
}
