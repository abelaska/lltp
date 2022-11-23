package me.belaska.lltp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventPublisher;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;

import me.belaska.lltp.core.event.SystemErrorLltpEventResponse;
import me.belaska.lltp.core.translator.ResponderTranslator;

public final class ReceiverExceptionHandler<D extends LltpEventDispatcher> implements ExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ReceiverExceptionHandler.class);

	private ResponderTranslator<D> responderTranslator = new ResponderTranslator<D>();

	private EventPublisher<LltpEventBuffer<D>> responderPublisher;

	public ReceiverExceptionHandler(RingBuffer<LltpEventBuffer<D>> responderRingBuffer) {
		this.responderPublisher = new EventPublisher<LltpEventBuffer<D>>(responderRingBuffer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleEventException(final Throwable ex, final long sequence, final Object event) {

		LOG.error("Exception processing: '" + sequence + "' '" + event + "'", ex);

		LltpEventBuffer<?> evBuf = (LltpEventBuffer<?>) event;

		if (evBuf.getEvent() != null && !evBuf.isReplica()) {
			// odeslani chybove odpovedi v pripade existujici dekodovane udalosti jejiz zpracovani selhalo pouze v
			// pripade ze node je MASTER
			responderTranslator
					.setEvent((LltpEvent<D>) new SystemErrorLltpEventResponse(evBuf.getEvent(), ex.getMessage()));
			responderPublisher.publishEvent(responderTranslator);
		}
	}

	@Override
	public void handleOnStartException(final Throwable ex) {
		LOG.error("Exception during onStart()", ex);
	}

	@Override
	public void handleOnShutdownException(final Throwable ex) {
		LOG.error("Exception during onShutdown()", ex);
	}
}
