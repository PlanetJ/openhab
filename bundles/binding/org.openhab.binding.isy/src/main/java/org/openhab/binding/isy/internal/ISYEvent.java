package org.openhab.binding.isy.internal;

import org.openhab.core.events.EventPublisher;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface ISYEvent {
	
	String getControlString();

	void handleEvent(Element node, EventPublisher event);
	
}
