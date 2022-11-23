package me.belaska.lltp.core.handler;

import me.belaska.lltp.core.LltpEventBuffer;
import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.LltpEventTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;

public class UnmarshalHandler<D extends LltpEventDispatcher> implements EventHandler<LltpEventBuffer<D>> {

	private static final Logger LOG = LoggerFactory.getLogger(UnmarshalHandler.class);

	private LltpEventTransformer<byte[], D> eventTransformer;

	public UnmarshalHandler(LltpEventTransformer<byte[], D> eventTransformer) {
		this.eventTransformer = eventTransformer;
	}

	@Override
	public void onEvent(final LltpEventBuffer<D> event, final long sequence, final boolean endOfBatch) throws Exception {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Decode seq '{}' endOfBatch '{}' event '{}'", new Object[] { sequence, endOfBatch, event });
		}

		event.setEvent(eventTransformer.unmarshal(event.getBuffer()));

		// pokud serializovana zprava neobsahuje id
		if (event.getEvent().getId() == 0L) {
			event.getEvent().setId(event.getId());
		}
	}
}
