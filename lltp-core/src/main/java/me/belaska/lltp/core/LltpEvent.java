package me.belaska.lltp.core;

import java.nio.ByteBuffer;

public interface LltpEvent<D extends LltpEventDispatcher> {

	long getId();

	void setId(long id);

	long getCorrelationId();

	LltpEventResponse<D> process(D dispatcher);

	void read(ByteBuffer byteBuffer);
	
	void write(ByteBuffer byteBuffer);
}
