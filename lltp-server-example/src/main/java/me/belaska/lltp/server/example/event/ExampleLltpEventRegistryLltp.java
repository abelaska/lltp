package me.belaska.lltp.server.example.event;

import me.belaska.lltp.core.event.AbstractLltpServerEventRegistry;
import me.belaska.lltp.server.example.event.dispatcher.ExampleLltpEventDispatcher;

public class ExampleLltpEventRegistryLltp extends AbstractLltpServerEventRegistry<ExampleLltpEventDispatcher> {

	public ExampleLltpEventRegistryLltp() {
		super();
		register(1, RechargeLltpEvent.class);
		register(2, RechargeLltpEventResponse.class);
		register(3, PaymentAuthorizationLltpEvent.class);
		register(4, PaymentAuthorizationLltpEventResponse.class);
	}
}
