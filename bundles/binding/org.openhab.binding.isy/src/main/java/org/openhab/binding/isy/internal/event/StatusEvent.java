package org.openhab.binding.isy.internal.event;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.library.types.PercentType;

public class StatusEvent extends AbstractEvent {

	public static String CONTROL_STRING = "ST";
	
	StatusEvent() {}

	@Override
	protected void publishEvent(EventPublisher eventPublisher) {
		eventPublisher.postUpdate(getNode().replaceAll(" ", "."), PercentType.valueOf(getAction()));
	}

	@Override
	public String getControlString() {
		return CONTROL_STRING;
	}
	
}
