package org.openhab.binding.isy.internal.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="Event")
public class Event {
	
	@XmlElement
	public String control;
	@XmlElement
	public String action;
	@XmlElement
	public String node;
	@XmlElement
	public String eventInfo;

	public String getControl() {
		return control;
	}
	
	public String getAction() {
		return action;
	}
	
	public String getNode() {
		return node;
	}
	
	public String getEventInfo() {
		return eventInfo;
	}
	
}
