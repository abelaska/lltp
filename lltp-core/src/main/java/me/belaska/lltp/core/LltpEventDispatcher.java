package me.belaska.lltp.core;

import me.belaska.lltp.core.event.SnapshotLltpEvent;
import me.belaska.lltp.core.event.SystemErrorLltpEventResponse;

public interface LltpEventDispatcher {

	void snapshot(SnapshotLltpEvent event);

	void systemError(SystemErrorLltpEventResponse eventResponse);
}
