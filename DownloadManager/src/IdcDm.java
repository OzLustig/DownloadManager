import java.io.File;
import java.util.concurrent.*;

public class IdcDm {

	/**
	 * Receive arguments from the command-line, provide some feedback and start the
	 * download.
	 *
	 * @param args
	 *            command-line arguments
	 */

	/**
	 * The program should be able to properly resume download after previous
	 * invocation was terminated due to a signal (any signal) or network
	 * disconnection (you should define relevant timeouts and document that in the
	 * code).
	 */
	final static long TIMEOUT = 10000; // default time out for network disconnection.
	final static long QUEUE_EMPTY_TIMEOUT = 100000;
	final static long EXECUTER_AWAIT_TERMINATION_TIMER = 20L;
	final static long MAXIMUM_DOWNLOAD_RATE = 1000000;

	public static void main(String[] args) {
		int numberOfWorkers = 1;
		Long maxBytesPerSecond = null;

		if (args.length < 1 || args.length > 3) {
			System.err.printf("usage:\n\tjava IdcDm URL [MAX-CONCURRENT-CONNECTIONS] [MAX-DOWNLOAD-LIMIT]\n");
			System.exit(1);
		} else if (args.length >= 2) {
			numberOfWorkers = Integer.parseInt(args[1]);
			if (args.length == 3)
				maxBytesPerSecond = Long.parseLong(args[2]);
		}

		String url = args[0];

		System.err.printf("Downloading");
		if (numberOfWorkers > 1)
			System.err.printf(" using %d connections", numberOfWorkers);
		if (maxBytesPerSecond != null)
			System.err.printf(" limited to %d Bps", maxBytesPerSecond);
		System.err.printf("...\n");

		DownloadURL(url, numberOfWorkers, maxBytesPerSecond);
	}

	/**
	 * Initiate the file's metadata, and iterate over missing ranges. For each: 1.
	 * Setup the Queue, TokenBucket, DownloadableMetadata, FileWriter, RateLimiter,
	 * and a pool of HTTPRangeGetters 2. Join the HTTPRangeGetters, send finish
	 * marker to the Queue and terminate the TokenBucket 3. Join the FileWriter and
	 * RateLimiter
	 *
	 * Finally, print "Download succeeded/failed" and delete the metadata as needed.
	 *
	 * @param url
	 *            URL to download
	 * @param numberOfWorkersAndRanges
	 *            number of concurrent connections
	 * @param maxBytesPerSecond
	 *            limit on download bytes-per-second
	 */
	private static void DownloadURL(String url, int numberOfWorkersAndRanges, Long maxBytesPerSecond) {
		// If 3rd argument is missing.
		if (maxBytesPerSecond == null)
			maxBytesPerSecond = MAXIMUM_DOWNLOAD_RATE;

		DownloadableMetadata downloadableMetadata = null;
		try {
			String MD_FileName = DownloadableMetadata.getMetadataName(DownloadableMetadata.getName(url));
			File MD_File = new File(MD_FileName);
			if (MD_File.exists()) {
				DownloadableMetadataObject downloadableMetadataObject = DownloadableMetadata
						.readObjectFromDisk(MD_FileName);
				numberOfWorkersAndRanges = downloadableMetadataObject.getAlreadyReadPartialRangeArray().length;
				downloadableMetadata = new DownloadableMetadata(url, numberOfWorkersAndRanges,
						downloadableMetadataObject);
			} else {
				downloadableMetadata = new DownloadableMetadata(url, numberOfWorkersAndRanges);
			}

			BlockingQueue<Chunk> outQueue = new LinkedTransferQueue<Chunk>();
			TokenBucket tokenBucket = new TokenBucket(maxBytesPerSecond, maxBytesPerSecond);
			FileWriter fileWriter = new FileWriter(downloadableMetadata, outQueue);
			Thread fileWriterThread = new Thread(fileWriter);
			RateLimiter rateLimiter = new RateLimiter(tokenBucket, maxBytesPerSecond);
			Thread rateLimiterThread = new Thread(rateLimiter);

			downloadableMetadata.writeDownloadableMetadataObjToDisk();
			ExecutorService executor = Executors.newFixedThreadPool(numberOfWorkersAndRanges + 1);
			rateLimiterThread.start();
			executor.execute(fileWriterThread);
			Range newRange;
			while ((newRange = downloadableMetadata.getMissingRange()) != null) {
				HTTPRangeGetter httpRangeGettergetter = new HTTPRangeGetter(url, newRange, outQueue, tokenBucket);
				executor.execute(httpRangeGettergetter);
			}

			executor.shutdown();
			// Wait for as long as the exectuer is not done running the HTTPGetters +
			// FileWriter.
			while (!executor.isTerminated()) {
			}

			tokenBucket.terminate();
			rateLimiterThread.join();
			System.err.println("Download succeeded");

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed to download file");
		} finally {
			if (downloadableMetadata != null)
				downloadableMetadata.delete();
		}
	}
}
