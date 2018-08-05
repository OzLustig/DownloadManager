import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Describes a file's metadata: URL, file name, size, and which parts already
 * downloaded to disk.
 *
 * The metadata (or at least which parts already downloaded to disk) is
 * constantly stored safely in disk. When constructing a new metadata object, we
 * first check the disk to load existing metadata.
 *
 * CHALLENGE: try to avoid metadata disk footprint of O(n) in the average case
 * HINT: avoid the obvious bitmap solution, and think about ranges...
 */
/**
 * @author Oz
 *
 */
class DownloadableMetadata {

	private long numberOfWorkersAndRanges;
	private int currentRangeToReadIndex;
	private long totalFileSize;
	public transient Range[] LeftToReadPartialRangeArray;
	private long rangeSize;
	private DownloadableMetadataObject downloadableMetadataObject;
	private final String MD_FileName;

	/**
	 * @param url
	 * @param numberOfWorkersAndRanges
	 * @param downloadableMetadataObject
	 * @throws Exception
	 */
	public DownloadableMetadata(String url, int numberOfWorkersAndRanges,
			DownloadableMetadataObject downloadableMetadataObject) throws Exception {
		this.numberOfWorkersAndRanges = numberOfWorkersAndRanges;
		this.currentRangeToReadIndex = 0;
		this.MD_FileName = getMetadataName(getName(url));
		this.setFileSize(new URL(url));
		this.rangeSize = this.totalFileSize / numberOfWorkersAndRanges;
		this.LeftToReadPartialRangeArray = new Range[numberOfWorkersAndRanges];
		// If file doesn't exist already => first time downloading it
		if (downloadableMetadataObject == null) {
			this.downloadableMetadataObject = new DownloadableMetadataObject(getName(url), url);
			this.downloadableMetadataObject.setAlreadyReadPartialRangeArray(numberOfWorkersAndRanges, this.rangeSize);
		} else {
			this.downloadableMetadataObject = downloadableMetadataObject;
		}

		setLeftToReadPartialRangeArray();
	}

	/**
	 * @param url
	 * @param numberOfWorkersAndRanges
	 * @throws Exception
	 */
	public DownloadableMetadata(String url, int numberOfWorkersAndRanges) throws Exception {
		this(url, numberOfWorkersAndRanges, null);
	}

	/**
	 * Initializes the ranges left to read array in case the downloaded was pausted and then re-started.
	 */
	public void setLeftToReadPartialRangeArray() {
		for (int range = 0; range < numberOfWorkersAndRanges; range++) {
			long end;
			long start = this.downloadableMetadataObject.getAlreadyReadPartialRangeArray()[range].getEnd();
			if (range + 1 == numberOfWorkersAndRanges)
				end = this.totalFileSize;
			else
				end = (range + 1) * this.rangeSize;
			this.LeftToReadPartialRangeArray[range] = new Range(start, end - 1);
		}
	}

	/**
	 * @param fileSize
	 */
	public void setFileSize(long fileSize) {
		this.totalFileSize = fileSize;
	}

	/**
	 * @return
	 */
	public long getNumberOfWorkersAndRanges() {
		return numberOfWorkersAndRanges;
	}

	/**
	 * @return
	 */
	public DownloadableMetadataObject getDownloadableMetadataObject() {
		return downloadableMetadataObject;
	}

	/**
	 * @param downloadableMetadataObject
	 */
	public void setDownloadableMetadataObject(DownloadableMetadataObject downloadableMetadataObject) {
		this.downloadableMetadataObject = downloadableMetadataObject;
	}

	/**
	 * @param totalNumberOfRanges
	 */
	public void setTotalNumberOfRanges(int totalNumberOfRanges) {
		this.numberOfWorkersAndRanges = totalNumberOfRanges;
	}

	/**
	 * @return
	 */
	public long getFileSize() {
		return totalFileSize;
	}

	/**
	 * @param url
	 * @throws Exception
	 */
	/**
	 * @param url
	 * @throws Exception
	 */
	public void setFileSize(URL url) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		long fileSize = connection.getContentLength();
		if (fileSize < 0) {
			throw new Exception("Failed to get content length");
		}
		this.totalFileSize = fileSize;
	}

	/**
	 * @return
	 */
	public String getMetadataFileName() {
		return MD_FileName;
	}

	/**
	 * @param filename
	 * @return
	 */
	public static String getMetadataName(String filename) {
		return filename + ".metadata";
	}

	/**
	 * @param path
	 * @return
	 */
	public static String getName(String path) {
		return path.substring(path.lastIndexOf('/') + 1, path.length());
	}

	/**
	 * @return
	 */
	/**
	 * @return
	 */
	Range getMissingRange() {
		Range range = null;
		if (currentRangeToReadIndex < this.numberOfWorkersAndRanges) {
			range = this.LeftToReadPartialRangeArray[this.currentRangeToReadIndex];
			this.currentRangeToReadIndex++;
		}
		return range;

	}

	/**
	 * @param extendingRange
	 * @param extandableRange
	 * @return
	 */
	private boolean IsRangeContaines(Range extendingRange, Range extandableRange) {
		boolean afterStart = extendingRange.getStart() >= extandableRange.getStart();
		boolean beforeEnd = extendingRange.getEnd() <= extandableRange.getEnd();
		return afterStart && beforeEnd;
	}

	/**
	 * @param extandingRange
	 */
	void extendRange(Range extandingRange) {

		Range[] rangesAlreadyRead = downloadableMetadataObject.getAlreadyReadPartialRangeArray();
		for (int i = 0; i < this.LeftToReadPartialRangeArray.length; i++) {
			if (IsRangeContaines(extandingRange, this.LeftToReadPartialRangeArray[i])) {
				rangesAlreadyRead[i] = new Range(rangesAlreadyRead[i].getStart(), extandingRange.getEnd());
				break;
			}
		}
	}

	/**
	 * @return
	 */
	String getMetadataFilename() {
		return this.MD_FileName;
	}

	/**
	 * 
	 */
	void delete() {
		try {
			new File(this.MD_FileName).delete();
		} catch (SecurityException e) {
			e.printStackTrace();
			System.err.println("delte metadataFile failed => Download failed");
			return;
		}
	}

	/**
	 * @return
	 */
	public long getTotalFileSize() {
		return totalFileSize;
	}

	/**
	 * @param totalFileSize
	 */
	public void setTotalFileSize(long totalFileSize) {
		this.totalFileSize = totalFileSize;
	}

	/**
	 * @return
	 */
	public String getMD_FileName() {
		return MD_FileName;
	}

	/**
	 * @param leftToReadPartialRangeArray
	 */
	public void setLeftToReadPartialRangeArray(Range[] leftToReadPartialRangeArray) {
		LeftToReadPartialRangeArray = leftToReadPartialRangeArray;
	}

	/**
	 * @throws IOException
	 */
	public void writeDownloadableMetadataObjToDisk() throws IOException {
		String metadataFileName = this.getMetadataFileName();
		File metaFile = new File(metadataFileName);
		File metaFileTemp = new File(metadataFileName + "_temp");
		try (FileOutputStream fileOut = new FileOutputStream(metaFileTemp);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);) {
			out.writeObject(this.downloadableMetadataObject);
		}
		metaFile.delete();
		boolean success = metaFileTemp.renameTo(metaFile);

		if (!success) {
			throw new java.io.IOException("Fail to write metadata obect to disk");
		}
	}

	/**
	 * @param metadataFileName
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static DownloadableMetadataObject readObjectFromDisk(String metadataFileName)
			throws IOException, ClassNotFoundException {
		DownloadableMetadataObject downloadableMetadataObject = null;
		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;
		try {
			fileInputStream = new FileInputStream(new File(metadataFileName));
			objectInputStream = new ObjectInputStream(fileInputStream);
			downloadableMetadataObject = (DownloadableMetadataObject) objectInputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Download Failed");
		} finally {
			if (fileInputStream != null)
				fileInputStream.close();
			if (objectInputStream != null)
				objectInputStream.close();
		}

		return downloadableMetadataObject;
	}

	/**
	 * @return
	 */
	public boolean isCompleted() {
		return this.downloadableMetadataObject.getCurrentAlreadyDownloadedNumberOfBytes() >= this.totalFileSize;
	}

	/**
	 * @param numberOfWorkersAndRanges
	 */
	public void setNumberOfWorkersAndRanges(long numberOfWorkersAndRanges) {
		this.numberOfWorkersAndRanges = numberOfWorkersAndRanges;
	}

	/**
	 * @return
	 */
	public int getCurrentRangeToReadIndex() {
		return currentRangeToReadIndex;
	}

	/**
	 * @param currentRangeToReadIndex
	 */
	public void setCurrentRangeToReadIndex(int currentRangeToReadIndex) {
		this.currentRangeToReadIndex = currentRangeToReadIndex;
	}

	/**
	 * @return
	 */
	public Range[] getLeftToReadPartialRangeArray() {
		return LeftToReadPartialRangeArray;
	}

	/**
	 * @return
	 */
	public long getRangeSize() {
		return rangeSize;
	}

	/**
	 * @param rangeSize
	 */
	public void setRangeSize(long rangeSize) {
		this.rangeSize = rangeSize;
	}

}
