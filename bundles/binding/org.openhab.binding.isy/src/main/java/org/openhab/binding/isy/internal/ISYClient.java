package org.openhab.binding.isy.internal;

import java.util.Map;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.openhab.binding.isy.internal.response.Nodes;
import org.openhab.binding.isy.internal.response.Nodes.Node;
import org.openhab.binding.isy.internal.response.Properties;
import org.openhab.binding.isy.internal.response.Property;
import org.openhab.binding.isy.internal.response.RestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jersey.repackaged.com.google.common.base.Preconditions;
import jersey.repackaged.com.google.common.collect.Maps;

public class ISYClient {

	private Logger logger = LoggerFactory.getLogger(ISYClient.class);

	private static enum Commands {
		DON, DOF
	}

	private final String host;
	private final int port;
	private final String username;
	private final String password;

	private ISYEventSubscriber eventSubscriber;
	
	private WebTarget restBase;

	public ISYClient(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;

		ClientConfig cc = new ClientConfig();
		if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
			HttpAuthenticationFeature auth = HttpAuthenticationFeature.basic(username, password);
			cc.register(auth);
		}

		restBase = ClientBuilder.newClient(cc).target("http://" + host + ":" + port).path("rest");
	}

	public Map<String, String> statusAll() {
		Map<String, String> status = Maps.newHashMap();
		Nodes nodes = restBase.path("status").request(MediaType.TEXT_XML_TYPE).get(Nodes.class);
		for (Node node : nodes.getNodes()) {
			status.put(node.getId(), node.getProperty().getValue());
		}
		return status;
	}

	public int status(String address) throws ISYException {

		Properties properties = restBase.path("status").path(address).request(MediaType.TEXT_XML_TYPE)
				.get(Properties.class);
		for (Property prop : properties.getProperties()) {
			if (prop.getId().equals("ST")) {
				try {
					return Integer.valueOf(prop.getValue());
				} catch (NumberFormatException e) {
					throw new ISYException("Error parsing status value " + prop.getValue());
				}
			}
		}

		throw new ISYException("No status available");
	}

	public boolean on(String address) {
		return dim(address, 255);
	}

	public boolean off(String address) {
		return restBase.path("nodes").path(address).path("cmd").path(Commands.DOF.toString())
				.request(MediaType.TEXT_XML_TYPE).get(RestResponse.class).getStatus().equals("200");
	}

	public boolean dim(String address, int level) {
		Preconditions.checkArgument(level >= 0 && level <= 255, "Valid dim levels are 0-255");
		if (level == 0)
			return off(address);

		return restBase.path("nodes").path(address).path("cmd").path(Commands.DON.toString())
				.path(String.valueOf(level)).request(MediaType.TEXT_XML_TYPE).get(RestResponse.class).getStatus()
				.equals("200");
	}

	public synchronized void subscribe() {
		if(eventSubscriber == null){
			eventSubscriber = new ISYEventSubscriber(host, port, username, password);
			eventSubscriber.start();
		}else{
			//No-op
		}
	}
	
	public void unsubscribe(){
		if(eventSubscriber != null){
			eventSubscriber.stop();
			eventSubscriber = null;
		}
	}

}
