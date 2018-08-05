/**
 * A token bucket based rate-limiter.
 *
 * This class should implement a "soft" rate limiter by adding maxBytesPerSecond
 * tokens to the bucket every second, or a "hard" rate limiter by resetting the
 * bucket to maxBytesPerSecond tokens every second.
 */
public class RateLimiter implements Runnable {
	private final TokenBucket tokenBucket;
	private final Long maxBytesPerSecond;

	/**
	 * @param tokenBucket
	 * @param maxBytesPerSecond
	 */
	RateLimiter(TokenBucket tokenBucket, Long maxBytesPerSecond) {
		this.tokenBucket = tokenBucket;
		this.maxBytesPerSecond = maxBytesPerSecond;
	}

	@Override
	public void run() {
		try {
			while (!tokenBucket.terminated()) {
				// Add maxBytesPerSecond seconds to the Bucket in order to limit the maximum
				// download speed.
				tokenBucket.add(maxBytesPerSecond);
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.err.println("Failed to add token to bucket => Download failed");
		}
	}
}
