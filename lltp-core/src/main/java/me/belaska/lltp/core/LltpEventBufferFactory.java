package me.belaska.lltp.core;

import com.lmax.disruptor.EventFactory;

public class LltpEventBufferFactory<D extends LltpEventDispatcher> implements EventFactory<LltpEventBuffer<D>> {

	@Override
	public LltpEventBuffer<D> newInstance() {
		return new LltpEventBuffer<D>();
	}
}
