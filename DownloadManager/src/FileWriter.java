import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

/**
 * This class takes chunks from the queue, writes them to disk and updates the
 * file's metadata.
 *
 * NOTE: make sure that the file interface you choose writes every update to the
 * file's content or metadata synchronously to the underlying storage device.
 */
public class FileWriter implements Runnable {
	private int completedPercentage = 0;
	private final BlockingQueue<Chunk> chunkQueue;
	private DownloadableMetadata downloadableMetadata;

	/**
	 * @param downloadableMetadata
	 * @param chunkQueue
	 */
	FileWriter(DownloadableMetadata downloadableMetadata, BlockingQueue<Chunk> chunkQueue) {
		this.chunkQueue = chunkQueue;
		this.downloadableMetadata = downloadableMetadata;
	}

	/**
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void writeChunks() throws IOException, InterruptedException {
		while (!downloadableMetadata.isCompleted()) {
			Chunk nextChunkToWrite = chunkQueue.take();
			long fileOffset = nextChunkToWrite.getOffset();
			long fileSize = nextChunkToWrite.getSize_in_bytes();
			RandomAccessFile randomAccessFile = null;
			try {
				 randomAccessFile = new RandomAccessFile(downloadableMetadata.getDownloadableMetadataObject().getFilename(), "rw");
				 randomAccessFile.seek(fileOffset);
				 randomAccessFile.write(nextChunkToWrite.getData(), 0, (int) fileSize);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if(randomAccessFile!= null) randomAccessFile.close();
			}
			
			downloadableMetadata.extendRange(new Range(fileOffset, fileOffset + fileSize - 1));
			downloadableMetadata.getDownloadableMetadataObject()
					.setCurrentAlreadyDownloadedNumberOfBytes((int) (downloadableMetadata
							.getDownloadableMetadataObject().getCurrentAlreadyDownloadedNumberOfBytes() + fileSize));

			progressPercent();
			downloadableMetadata.writeDownloadableMetadataObjToDisk();
		}
	}

	@Override
	public void run() {
		try {
			this.writeChunks();
		} catch (IOException | InterruptedException e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			} finally {
				run();
			}
		}
	}

	/**
	 * Prints the current percentage of the download every time it changes.
	 */
	public void progressPercent() {
		long bytesDownloaded = this.downloadableMetadata.getDownloadableMetadataObject()
				.getCurrentAlreadyDownloadedNumberOfBytes();
		long fileSize = this.downloadableMetadata.getFileSize();
		int currPercent = (int) Math.floor((bytesDownloaded * 100) / fileSize);
		if (this.completedPercentage != currPercent) {
			System.err.println("Downloaded so far: " + currPercent + "%");
			this.completedPercentage = currPercent;
		}
	}
}
