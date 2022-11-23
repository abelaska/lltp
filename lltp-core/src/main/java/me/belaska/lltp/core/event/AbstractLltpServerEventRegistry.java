package me.belaska.lltp.core.event;

import java.util.HashMap;
import java.util.Map;

import me.belaska.lltp.core.LltpEvent;
import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.LltpEventRegistry;

public abstract class AbstractLltpServerEventRegistry<D extends LltpEventDispatcher> implements LltpEventRegistry<D> {

	private Map<Integer, Class<? extends LltpEvent<D>>> EVENT_ID_MAP = new HashMap<Integer, Class<? extends LltpEvent<D>>>();
	private Map<Class<? extends LltpEvent<D>>, Integer> EVENT_CLASS_MAP = new HashMap<Class<? extends LltpEvent<D>>, Integer>();

	@SuppressWarnings("unchecked")
	public AbstractLltpServerEventRegistry() {
		register(-1, (Class<? extends LltpEvent<D>>) SnapshotLltpEvent.class);
		register(-2, (Class<? extends LltpEvent<D>>) SystemErrorLltpEventResponse.class);
	}

	protected void register(int eventId, Class<? extends LltpEvent<D>> eventClass) {
		EVENT_ID_MAP.put(eventId, eventClass);
		EVENT_CLASS_MAP.put(eventClass, eventId);
	}

	@Override
	public Integer findByClass(Class<?> eventClass) {
		return EVENT_CLASS_MAP.get(eventClass);
	}

	@Override
	public Class<? extends LltpEvent<D>> findById(int eventId) {
		return EVENT_ID_MAP.get(eventId);
	}
}
