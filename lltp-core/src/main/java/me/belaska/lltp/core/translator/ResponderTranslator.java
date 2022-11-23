package me.belaska.lltp.core.translator;

import com.lmax.disruptor.EventTranslator;

import me.belaska.lltp.core.LltpEvent;
import me.belaska.lltp.core.LltpEventBuffer;
import me.belaska.lltp.core.LltpEventDispatcher;

public class ResponderTranslator<D extends LltpEventDispatcher> implements EventTranslator<LltpEventBuffer<D>> {

	private volatile LltpEvent<D> event;

	public void setEvent(LltpEvent<D> event) {
		this.event = event;
	}

	@Override
	public LltpEventBuffer<D> translateTo(LltpEventBuffer<D> eventBuffer, long sequence) {
		eventBuffer.clearBuffer();
		eventBuffer.setEvent(event);
		return eventBuffer;
	}
}
