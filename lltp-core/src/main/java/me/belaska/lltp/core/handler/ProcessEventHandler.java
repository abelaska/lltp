package me.belaska.lltp.core.handler;

import me.belaska.lltp.core.LltpEventBuffer;
import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.LltpEventResponse;
import me.belaska.lltp.core.translator.ResponderTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventPublisher;
import com.lmax.disruptor.RingBuffer;

public class ProcessEventHandler<D extends LltpEventDispatcher> implements EventHandler<LltpEventBuffer<D>> {

	private static final Logger LOG = LoggerFactory.getLogger(ProcessEventHandler.class);

	private D eventDispatcher;

	private ResponderTranslator<D> responderTranslator = new ResponderTranslator<D>();

	private EventPublisher<LltpEventBuffer<D>> responderPublisher;

	public ProcessEventHandler(D eventDispatcher, RingBuffer<LltpEventBuffer<D>> responderRingBuffer) {
		this.responderPublisher = new EventPublisher<LltpEventBuffer<D>>(responderRingBuffer);
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public void onEvent(final LltpEventBuffer<D> event, final long sequence, final boolean endOfBatch) throws Exception {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Process seq '{}' endOfBatch '{}' event '{}'", new Object[] { sequence, endOfBatch, event });
		}

		try {
			LltpEventResponse<D> response = event.getEvent().process(eventDispatcher);

			if (event.isNotReplica()) {
				if (response == null) {
					if (LOG.isTraceEnabled()) {
						LOG.trace("No response to event '{}'", event);
					}
				} else {
					responderTranslator.setEvent(response);
					responderPublisher.publishEvent(responderTranslator);
				}
			}
		} finally {
			// uvolneni deserializovane udalosti z pameti
			event.clearEvent();
		}
	}
}
