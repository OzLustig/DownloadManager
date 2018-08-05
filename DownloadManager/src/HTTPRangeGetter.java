import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

/**
 * A runnable class which downloads a given url. It reads CHUNK_SIZE at a time
 * and writs it into a BlockingQueue. It supports downloading a range of data,
 * and limiting the download rate using a token bucket.
 */
public class HTTPRangeGetter implements Runnable {
	public static final int CHUNK_SIZE = 4096;
	private static final int CONNECT_TIMEOUT = 500;
	private static final int READ_TIMEOUT = 2000;
	private final String urlString;
	private Range range;
	private final BlockingQueue<Chunk> outQueue;
	private TokenBucket tokenBucket;
	private URL urlObj = null;
	private InputStream inputStream;
	private int totalBytesRead;
	private int currentBytesRead;

	/**
	 * @param url
	 * @param range
	 * @param outQueue
	 * @param tokenBucket
	 * @throws MalformedURLException
	 */
	HTTPRangeGetter(String url, Range range, BlockingQueue<Chunk> outQueue, TokenBucket tokenBucket)
			throws MalformedURLException {
		this.urlString = url;
		this.range = range;
		this.outQueue = outQueue;
		this.tokenBucket = tokenBucket;
		this.urlObj = new URL(this.urlString);
		this.currentBytesRead = 0;
		this.totalBytesRead = 0;
	}

	@Override
	public void run() {
		try {
			this.downloadRange();
		} catch (IOException | InterruptedException e) {

			this.range = new Range(this.range.getStart() + this.currentBytesRead, this.range.getEnd());
			this.currentBytesRead = 0;
			try {
				Thread.sleep(1000);
			} catch (Exception e1) {
				e1.printStackTrace();
			} finally {
				run();
			}
		}
	}

	/**
	 *      * Downloads a file from a URL
	 *       * @param fileURL HTTP URL of the file to be downloaded      
	 *  	 @param saveDir path of the directory to save the file
	 *      * @throws IOException      
	 */
	private void downloadRange() throws IOException, InterruptedException {
		HttpURLConnection httpConnection = (HttpURLConnection) urlObj.openConnection();
		httpConnection.setConnectTimeout(CONNECT_TIMEOUT);
		httpConnection.setReadTimeout(READ_TIMEOUT);
		httpConnection.setRequestProperty("Range", "bytes=" + this.range.getStart() + "-" + this.range.getEnd());

		// opens input stream from the HTTP connection
		httpConnection.connect();
		this.inputStream = httpConnection.getInputStream();

		for (long i = this.range.getStart(); i < this.range.getEnd(); i++) {
			byte[] chunk_buffer = new byte[CHUNK_SIZE];
			if ((this.currentBytesRead = inputStream.read(chunk_buffer)) != -1) {
				// Syncronize the download rate to the RateLimiter by using the TokenBucket.
				tokenBucket.take(this.currentBytesRead);
				outQueue.add(
						new Chunk(chunk_buffer, this.range.getStart() + this.totalBytesRead, this.currentBytesRead));
				this.totalBytesRead += this.currentBytesRead;
			}
		}
		
		inputStream.close();
		httpConnection.disconnect();
	}
}
