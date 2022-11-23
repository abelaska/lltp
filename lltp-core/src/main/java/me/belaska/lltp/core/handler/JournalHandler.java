package me.belaska.lltp.core.handler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import me.belaska.lltp.core.LltpEventBuffer;
import me.belaska.lltp.core.LltpEventDispatcher;
import me.belaska.lltp.core.translator.ReceiverTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;

/**
 * TODO journal force provadet v pravidelnem intervalu
 */
public class JournalHandler<D extends LltpEventDispatcher> implements EventHandler<LltpEventBuffer<D>> {

	private static final Logger LOG = LoggerFactory.getLogger(JournalHandler.class);

	private int journalPageSize = 10 * 1024 * 1024;

	private String journalFileName = "journal";

	private RandomAccessFile journalFile;

	private FileChannel journalChannel;

	private MappedByteBuffer journal;

	public JournalHandler() throws IOException {

		openJournal();

		long lastId = seekJournalEnd();

		LOG.info("Last journaled event id '{}'", lastId);

		ReceiverTranslator.INITIAL_SEQUENCE_VALUE = ++lastId;
	}

	@Override
	public void onEvent(final LltpEventBuffer<D> event, final long sequence, final boolean endOfBatch) throws Exception {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Journal seq '{}' endOfBatch '{}' event '{}'", new Object[] { sequence, endOfBatch, event });
		}

		int requiredSize = 8 + 4 + event.getBuffer().length;

		if (journal.remaining() < requiredSize) {
			createNewJournal();
		}

		journal.putLong(event.getId()); // >0=active record, 0=empty record
		journal.putInt(event.getBuffer().length);
		journal.put(event.getBuffer());
	}

	public long seekJournalEnd() {

		journal.position(0);

		long latestId = 0;

		while (true) {
			if (journal.remaining() >= 8) {
				int pos = journal.position();
				long id = journal.getLong();
				if (id != 0) {
					int size = journal.getInt();
					pos = journal.position() + size;
				}
				journal.position(pos);
				if (id == 0) {
					break;
				} else {
					latestId = id;
				}
			} else {
				break;
			}
		}

		return latestId;
	}

	public void openJournal() throws IOException {
		journalFile = new RandomAccessFile(journalFileName, "rw");
		journalChannel = journalFile.getChannel();

		journalFile.setLength(journalPageSize);

		journal = journalChannel.map(FileChannel.MapMode.READ_WRITE, 0, journalFile.length());
	}

	public void closeJournal() throws IOException {

		long lastId = seekJournalEnd();

		journal.force();
		journal = null;

		journalChannel.close();
		journalChannel = null;

		journalFile.close();
		journalFile = null;

		new File(journalFileName).renameTo(new File(journalFileName + "." + lastId));
	}

	public void createNewJournal() throws IOException {
		closeJournal();
		openJournal();
	}
}
