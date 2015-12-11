package org.openhab.binding.isy.internal.response;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/*
 * Response to /rest/status/<addr>
 * @author jmarchioni
 *
 */
@XmlRootElement(name="properties")
public class Properties {
	
	@XmlElement(name="property")
	private List<Property> properties;
	
	public List<Property> getProperties() {
		return properties;
	}
	
}
