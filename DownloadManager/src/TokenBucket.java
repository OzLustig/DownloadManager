import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A Token Bucket (https://en.wikipedia.org/wiki/Token_bucket)
 *
 * This thread-safe bucket should support the following methods:
 *
 * - take(n): remove n tokens from the bucket (blocks until n tokens are available and taken)
 * - set(n): set the bucket to contain n tokens (to allow "hard" rate limiting)
 * - add(n): add n tokens to the bucket (to allow "soft" rate limiting)
 * - terminate(): mark the bucket as terminated (used to communicate between threads)
 * - terminated(): return true if the bucket is terminated, false otherwise
 *
 */
class TokenBucket {

	private ReentrantLock lock;
	private long availableTokens;
	private boolean terminated;
    
    /**
     * @param totalNumberOfTokens
     * @param availableTokens
     */
    public TokenBucket(long totalNumberOfTokens, long availableTokens) {
		this.availableTokens = availableTokens;
		this.lock = new ReentrantLock();
		this.terminated = false;
	}

    /**
     * @param tokens
     * @throws InterruptedException
     */
    void take(long tokens) throws InterruptedException {
    	if(tokens<=0)
    		return;
    	try {
    		lock.lock();
        	while(tokens > this.availableTokens)
        	{
        		this.lock.newCondition().await(500, TimeUnit.MILLISECONDS);
        	}
        	this.availableTokens-=tokens;
    	}
    	finally {
    		lock.unlock();	
		}
    }

    /**
     * 
     */
    void terminate() {
        this.terminated=true;    }

    /**
     * @return
     */
    boolean terminated() {
        return this.terminated;
    }

    /**
     * @param tokens
     */
    void set(long tokens) {
        try {
        	lock.lock();
        }
        finally {
			lock.unlock();
		}
    }

	/**
	 * @param maxBytesPerSecond
	 */
	public void add(Long maxBytesPerSecond) {
		try {
			lock.lock();
			this.availableTokens = maxBytesPerSecond;
		}
		finally {
			lock.unlock();
		}
		
	}
}
