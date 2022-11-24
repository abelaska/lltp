package me.belaska.lltp.core.event.transformer;

import java.nio.ByteBuffer;
import java.util.Arrays;

import me.belaska.lltp.core.LltpEvent;
import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.LltpEventRegistry;
import me.belaska.lltp.core.LltpEventTransformer;

/**
 * Single-thread use only.
 */
public class BinaryEventTransformer<D extends LltpEventDispatcher> implements LltpEventTransformer<byte[], D> {

	private final static int MAX_MESSAGE_SIZE = 65536;

	private ByteBuffer buffer = ByteBuffer.allocate(MAX_MESSAGE_SIZE);

	private LltpEventRegistry<D> eventRegistry;

	public BinaryEventTransformer(LltpEventRegistry<D> eventRegistry) {
		this.eventRegistry = eventRegistry;
	}

	@Override
	public byte[] marshal(LltpEvent<D> event) {

		Integer eventId = eventRegistry.findByClass(event.getClass());
		if (eventId == null) {
			throw new RuntimeException("Event class '" + event.getClass() + "' not registered");
		}

		buffer.clear();
		buffer.putInt(eventId);

		event.write(buffer);

		buffer.flip();

		byte[] buf = Arrays.copyOf(buffer.array(), buffer.limit());
		return buf;
	}

	@Override
	public LltpEvent<D> unmarshal(byte[] data) {

		ByteBuffer b = ByteBuffer.wrap(data);

		int eventId = b.getInt();

		Class<? extends LltpEvent<D>> eventClass = eventRegistry.findById(eventId);
		if (eventClass == null) {
			throw new RuntimeException("Event id '" + eventId + "' not registered");
		}

		LltpEvent<D> obj;
		try {
			obj = eventClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create new instance of class '" + eventClass + "'", e);
		}
		
		obj.read(b);

		return obj;
	}
}
