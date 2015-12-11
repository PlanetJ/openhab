package org.openhab.binding.isy.internal.response;

import javax.xml.bind.annotation.XmlAttribute;

public class Property {
	
	@XmlAttribute
	private String id;
	
	@XmlAttribute
	private String value;
	
	@XmlAttribute
	private String formatted;
	
	@XmlAttribute
	private String uom;
	
	public String getId() {
		return id;
	}
	
	public String getValue() {
		return value;
	}
	
	public String getUom() {
		return uom;
	}
	
	public String getFormatted() {
		return formatted;
	}
	
}