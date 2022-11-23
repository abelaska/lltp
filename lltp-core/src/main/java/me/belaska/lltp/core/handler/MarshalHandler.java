package me.belaska.lltp.core.handler;

import me.belaska.lltp.core.LltpEventBuffer;
import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.LltpEventTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;

public class MarshalHandler<D extends LltpEventDispatcher> implements EventHandler<LltpEventBuffer<D>> {

	private static final Logger LOG = LoggerFactory.getLogger(MarshalHandler.class);

	private LltpEventTransformer<byte[], D> eventTransformer;

	public MarshalHandler(LltpEventTransformer<byte[], D> eventTransformer) {
		this.eventTransformer = eventTransformer;
	}

	@Override
	public void onEvent(final LltpEventBuffer<D> event, final long sequence, final boolean endOfBatch) throws Exception {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Encode seq '{}' endOfBatch '{}' event '{}'", new Object[] { sequence, endOfBatch, event });
		}

		event.setBuffer(eventTransformer.marshal(event.getEvent()));
	}
}
