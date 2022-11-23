package me.belaska.lltp.core.event;

import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.LltpEventResponse;

public abstract class AbstractLltpEventResponse<D extends LltpEventDispatcher> extends AbstractLltpEvent<D> implements
		LltpEventResponse<D> {

	public AbstractLltpEventResponse() {
		super();
	}

	public AbstractLltpEventResponse(long id) {
		super(id);
	}

	public AbstractLltpEventResponse(long id, long correlationId) {
		super(id, correlationId);
	}
}
