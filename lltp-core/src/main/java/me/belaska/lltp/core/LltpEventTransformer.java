package me.belaska.lltp.core;

public interface LltpEventTransformer<T, D extends LltpEventDispatcher> {

	T marshal(LltpEvent<D> event);

	LltpEvent<D> unmarshal(T data);
}
