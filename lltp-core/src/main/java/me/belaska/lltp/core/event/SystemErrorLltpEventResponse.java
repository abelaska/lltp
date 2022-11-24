package me.belaska.lltp.core.event;

import java.nio.ByteBuffer;

import me.belaska.lltp.core.LltpEvent;
import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.LltpEventResponse;
import me.belaska.lltp.core.event.utils.ByteBufferHelper;

public class SystemErrorLltpEventResponse<D extends LltpEventDispatcher> extends AbstractLltpEventResponse<D> {

	private volatile String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public SystemErrorLltpEventResponse() {
	}

	public SystemErrorLltpEventResponse(LltpEvent<?> event, String value) {
		this(event.getId(), event.getCorrelationId(), value);
	}

	public SystemErrorLltpEventResponse(long id, long correlationId, String value) {
		super(id, correlationId);
		this.value = value;
	}

	@Override
	public void read(ByteBuffer byteBuffer) {
		super.read(byteBuffer);
		this.value = ByteBufferHelper.readString(byteBuffer);
	}

	@Override
	public void write(ByteBuffer byteBuffer) {
		super.write(byteBuffer);
		ByteBufferHelper.writeString(byteBuffer, value);
	}

	@Override
	public LltpEventResponse<D> process(D dispatcher) {
		dispatcher.systemError(this);
		return null;
	}
}
