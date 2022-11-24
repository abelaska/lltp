package me.belaska.lltp.core.event;

import me.belaska.lltp.core.LltpEvent;
import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.LltpEventResponse;

public class SnapshotLltpEvent<D extends LltpEventDispatcher> extends AbstractLltpEvent<D> {

	public SnapshotLltpEvent() {
	}

	public SnapshotLltpEvent(LltpEvent<?> event) {
		this(event.getId(), event.getCorrelationId());
	}

	public SnapshotLltpEvent(long id, long correlationId) {
		super(id, correlationId);
	}

	@Override
	public LltpEventResponse<D> process(D dispatcher) {
		dispatcher.snapshot(this);
		return null;
	}
}
