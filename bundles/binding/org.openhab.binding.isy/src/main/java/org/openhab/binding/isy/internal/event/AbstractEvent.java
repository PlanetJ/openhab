package org.openhab.binding.isy.internal.event;

import org.openhab.binding.isy.internal.ISYEvent;
import org.openhab.core.events.EventPublisher;
import org.w3c.dom.Element;

public abstract class AbstractEvent implements ISYEvent {
	
	private Element eventElement;
	
	@Override
	public final void handleEvent(Element node, EventPublisher event) {
		eventElement = node;
		publishEvent(event);
	}
	
	protected String getAction(){
		return eventElement.getElementsByTagName("action").item(0).getTextContent();
	}
	
	protected String getNode(){
		return eventElement.getElementsByTagName("node").item(0).getTextContent();
	}
	
	protected Element getEventElement() {
		return eventElement;
	}
	
	protected abstract void publishEvent(EventPublisher eventPublisher);

}
