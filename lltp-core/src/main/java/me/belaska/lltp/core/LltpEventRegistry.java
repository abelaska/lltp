package me.belaska.lltp.core;

public interface LltpEventRegistry<D extends LltpEventDispatcher> {

	Integer findByClass(Class<?> eventClass);

	Class<? extends LltpEvent<D>> findById(int eventId);
}
