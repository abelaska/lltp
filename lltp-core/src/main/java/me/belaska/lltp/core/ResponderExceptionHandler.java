package me.belaska.lltp.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.ExceptionHandler;

public final class ResponderExceptionHandler implements ExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(ResponderExceptionHandler.class);

	@Override
	public void handleEventException(final Throwable ex, final long sequence, final Object event) {
		LOG.error("Exception processing: '" + sequence + "' '" + event + "'", ex);
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
