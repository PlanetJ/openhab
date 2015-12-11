package org.openhab.binding.isy.internal.response;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="RestResponse")
public class RestResponse {
	
	@XmlElement
	private String status;
	
	public String getStatus() {
		return status;
	}
	
}
