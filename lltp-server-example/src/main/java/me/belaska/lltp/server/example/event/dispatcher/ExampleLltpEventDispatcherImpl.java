package me.belaska.lltp.server.example.event.dispatcher;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.belaska.lltp.core.event.SnapshotLltpEvent;
import me.belaska.lltp.core.event.SystemErrorLltpEventResponse;
import me.belaska.lltp.server.example.event.PaymentAuthorizationLltpEvent;
import me.belaska.lltp.server.example.event.PaymentAuthorizationLltpEventResponse;
import me.belaska.lltp.server.example.event.RechargeLltpEvent;
import me.belaska.lltp.server.example.event.RechargeLltpEventResponse;

public class ExampleLltpEventDispatcherImpl implements ExampleLltpEventDispatcher {

	private static final Logger LOG = LoggerFactory.getLogger(ExampleLltpEventDispatcherImpl.class);

	private Map<Long, Long> clnBalance = new HashMap<Long, Long>();

	private String snapshotFileName = "snapshot";

	public ExampleLltpEventDispatcherImpl() {
		try {
			loadSnapshot();
		} catch (IOException e) {
			LOG.error("Selhalo nacteni snapshotu ze souboru", e);
		}
	}

	@Override
	public void systemError(SystemErrorLltpEventResponse eventResponse) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Processing event response '{}'", eventResponse);
		}
	}

	@Override
	public PaymentAuthorizationLltpEventResponse paymentAuthorization(PaymentAuthorizationLltpEvent event) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Processing payment authorization event '{}'", event);
		}

		Long oldBalance = clnBalance.get(event.getCln());

		if (oldBalance == null) {
			oldBalance = 0L;
		}

		long newBalance = oldBalance;

		boolean authorized = false;

		if (oldBalance > 0 && oldBalance >= event.getAmount()) {
			newBalance = oldBalance - event.getAmount();

			authorized = true;

			clnBalance.put(event.getCln(), newBalance);
		}

		return new PaymentAuthorizationLltpEventResponse(event, authorized, newBalance);
	}

	@Override
	public void paymentAuthorizationResponse(PaymentAuthorizationLltpEventResponse eventResponse) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Processing payment authorization response '{}'", eventResponse);
		}
	}

	@Override
	public RechargeLltpEventResponse recharge(RechargeLltpEvent event) {

		Long oldBalance = clnBalance.get(event.getCln());

		long newBalance = (oldBalance == null ? 0 : oldBalance) + event.getAmount();

		clnBalance.put(event.getCln(), newBalance);

		return new RechargeLltpEventResponse(event, newBalance);
	}

	@Override
	public void rechargeResponse(RechargeLltpEventResponse response) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Processing recharge response '{}'", response);
		}
	}

	public void loadSnapshot() throws IOException {

		if (new File(snapshotFileName).exists()) {

			RandomAccessFile snapshotFile = null;
			FileChannel snapshotChannel = null;
			MappedByteBuffer snapshot = null;

			try {
				snapshotFile = new RandomAccessFile(snapshotFileName, "rw");
				snapshotChannel = snapshotFile.getChannel();
				snapshot = snapshotChannel.map(FileChannel.MapMode.READ_WRITE, 0, snapshotFile.length());

				clnBalance.clear();

				while (snapshot.hasRemaining()) {
					long cln = snapshot.getLong();
					long balance = snapshot.getLong();

					LOG.debug("Snapshot load cln '{}' balance '{}'", cln, balance);

					clnBalance.put(cln, balance);
				}
			} finally {
				try {
					if (snapshot != null) {
						snapshot.force();
					}
				} finally {
					if (snapshotChannel != null) {
						try {
							snapshotChannel.close();
						} finally {
							if (snapshotFile != null) {
								snapshotFile.close();
							}
						}
					}
				}
			}
		}
	}

	public void saveSnapshot() throws IOException {

		if (new File(snapshotFileName).exists()) {
			new File(snapshotFileName).renameTo(new File(snapshotFileName + "." + System.currentTimeMillis()));
		}

		RandomAccessFile snapshotFile = null;
		FileChannel snapshotChannel = null;
		MappedByteBuffer snapshot = null;

		try {
			snapshotFile = new RandomAccessFile(snapshotFileName, "rw");
			snapshotChannel = snapshotFile.getChannel();

			long snapshotSize = clnBalance.size() * (8 + 8);

			snapshotFile.setLength(snapshotSize);

			snapshot = snapshotChannel.map(FileChannel.MapMode.READ_WRITE, 0, snapshotFile.length());

			for (Entry<Long, Long> entry : clnBalance.entrySet()) {
				LOG.debug("Snapshot save cln '{}' balance '{}'", entry.getKey(), entry.getValue());

				snapshot.putLong(entry.getKey());
				snapshot.putLong(entry.getValue());
			}
		} finally {
			try {
				if (snapshot != null) {
					snapshot.force();
				}
			} finally {
				if (snapshotChannel != null) {
					try {
						snapshotChannel.close();
					} finally {
						if (snapshotFile != null) {
							snapshotFile.close();
						}
					}
				}
			}
		}
	}

	@Override
	public void snapshot(SnapshotLltpEvent event) {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Processing snapshot '{}'", event);
		}

		try {
			saveSnapshot();
		} catch (IOException e) {
			LOG.error("Selhalo ulozeni snapshotu do souboru", e);
		}
	}
}
