import java.io.Serializable;

public class DownloadableMetadataObject implements Serializable {

	private static final long serialVersionUID = 1L;
	private String filename;
    private String url;
    private Range [] alreadyReadPartialRangeArray;
    private int currentAlreadyDownloadedNumberOfBytes;
    
    /**
     * @param alreadyReadPartialRangeArray
     */
    public void setAlreadyReadPartialRangeArray(Range[] alreadyReadPartialRangeArray) {
		this.alreadyReadPartialRangeArray = alreadyReadPartialRangeArray;
	}

	/**
	 * @param filename
	 * @param url
	 */
	public DownloadableMetadataObject(String filename, String url) {
		this.filename = filename;
		this.url = url;
		this.currentAlreadyDownloadedNumberOfBytes = 0;
	}


	/**
	 * @return
	 */
	public String getFilename() {
		return filename;
	}


	/**
	 * @param filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}


	/**
	 * @return
	 */
	public String getUrl() {
		return url;
	}


	/**
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}


	/**
	 * @return
	 */
	public Range[] getAlreadyReadPartialRangeArray() {
		return alreadyReadPartialRangeArray;
	}


	/**
	 * @param numberOfWorkersAndRanges
	 * @param rangeSize
	 */
	public void setAlreadyReadPartialRangeArray(int numberOfWorkersAndRanges, long rangeSize) {
		this.alreadyReadPartialRangeArray = new Range[numberOfWorkersAndRanges];
		for (int range = 0; range < numberOfWorkersAndRanges; range++) {
			long start = range * rangeSize;
			long end = start;
			this.alreadyReadPartialRangeArray[range] = new Range(start,end);
		}
	}


	/**
	 * @return
	 */
	public int getCurrentAlreadyDownloadedNumberOfBytes() {
		return currentAlreadyDownloadedNumberOfBytes;
	}


	/**
	 * @param currentAlreadyDownloadedNumberOfBytes
	 */
	public void setCurrentAlreadyDownloadedNumberOfBytes(int currentAlreadyDownloadedNumberOfBytes) {
		this.currentAlreadyDownloadedNumberOfBytes = currentAlreadyDownloadedNumberOfBytes;
	}


	/**
	 * @return
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}


	/**
	 * @param rangeIndexer
	 * @param range
	 */
	public void extendRange(int rangeIndexer, Range range) {
		this.alreadyReadPartialRangeArray[rangeIndexer] = range;
		
	}
	
	
}
