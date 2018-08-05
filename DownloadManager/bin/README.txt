Idan Shuraty - 201580990
Oz Lustig - 203184858

Chunk - a basic measurment unit designed to allow writing chunks of the downloaded data to the downloaded file.
DownloadableMetadata - the metadata of the file being downloaded, allowing to restore all file + metadata related when download has been paused.
DownloadableMetadataObject - the actual object in which we store the file's data in the Disk.
FileWriter - responsible to deliver, write and manage the writing of chunks of data to the Disk.
HTTPRangeGetter - Supports downloading in ranges using HTTP, worker instance that downloads a specific range of bytes from the web.
IdcDm - contains the main method, creates, invokes, times and deletes all related proccessess and data of the file which is being downloaded.
Range - A basic measurement unit designed to support the the multiple threads downloading different, distinct parts of the file which is being downloaded.
RateLimiter - Supports the maximum download speed limitations of all the different threads which are downloading parts of the file.
TokenBucket - Stores, manages and times the threads which are responsible for downloading distinct parts of the file.