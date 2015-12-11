package org.openhab.binding.isy.internal.response;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import jersey.repackaged.com.google.common.collect.Lists;

@XmlRootElement(name="nodes")
public class Nodes {

	@XmlElement(name="node")
	private List<Node> nodes = Lists.newArrayList();

	public List<Node> getNodes() {
		return nodes;
	}

	public static class Node {
		
		@XmlAttribute
		private String id;
		
		@XmlElement
		private Property property;
		
		public String getId() {
			return id;
		}
		
		public Property getProperty() {
			return property;
		}
		
	}
	

}
