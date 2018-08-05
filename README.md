```
Computer Networks - IDC
```
# Lab: HTTP Download Manager

## Overview

Download managers, popularized in the mid-late 90s, have changed the way we download files from the
internet. While today most of these features are integrated into modern browsers, you can still find them handy,
e.g to download files faster from the command line (see: ​aria2​ and ​axel​).
In this lab you’ll get a glance into the implementation of download managers, you’ll learn how to resume
broken downloads, why using multiple HTTP connections accelerate some downloads, and how to rate limit
your application.

### Resume Downloads

Your Download Manager needs to be able to recover from a previously stopped download (e.g. process stopped
with a signal, power outage, network disconnection, etc.). Make sure to reliably store your progress to a file,
and use HTTP Range request (see: ​https://tools.ietf.org/html/rfc7233​) in order to resume broken downloads.

### Concurrent Connections

You’ll need to support downloading a file using multiple HTTP connection. This download acceleration method
is commonly used to circumvent per-connection server side limitations. Use the multithreading Java
programming techniques acquired last semester in order to manage multiple downloaders and a single disk
writer threads. Use HTTP Range request to split the load between downloader threads.

### Download Rate Limiting

In order to prevent your new download habits to interfere with your video streaming, use the token bucket
algorithm (​https://en.wikipedia.org/wiki/Token_bucket​) to enforce downloading a specific amount of bytes (i.e.
tokens) per second.

### Usage Example

```
$ ​ java IdcDm "https://archive.org/download/Mario1_500/Mario1_500.avi" 8 10000
Downloading using 8 connections limited to 10000 Bps...
Downloaded 0%
Downloaded 1%
^C
$ ​ java IdcDm "https://archive.org/download/Mario1_500/Mario1_500.avi" 8
Downloading using 8 connections...
Downloaded 1%
Downloaded 2%
```

```
Computer Networks - IDC
Downloaded 3%
Downloaded 4%
Downloaded 5%
...
Downloaded 95%
Downloaded 96%
Downloaded 97%
Downloaded 98%
Downloaded 99%
Download succeeded
```
## Implementation Details

Create a command line application which accepts up to 3 parameters (in that order):

1. URL.
2. (optional) Maximum number of concurrent HTTP connections.
3. (optional) Maximum download rate in bytes-per-second.
The program will download the file specified in the URL (following redirects) into the current directory, e.g.
“​https://archive.org/download/Mario1_500/Mario1_500.avi​” will be downloaded to “Mario1_500.avi”.
The program may create additional files during download (i.e. metadata and temporary files), all of which:
● Should start with the same name as the downloaded file, e.g. “Mario1_500.avi.tmp”.
● Should be deleted after a successful download.
● Should be smaller than ​ **n/1024** ​ in size, where ​ **n** ​ the size of the downloaded file.
The program should be able to properly resume download after previous invocation was terminated due to a
signal (any signal) or network disconnection (you should define relevant timeouts and document that in the
code).
The program can decide to use less than specified maximum number of concurrent HTTP connections, if it
considered the file to be too small (you should define “too small” and document that in the code).
The program should not exceed maximum download rate in bytes-per-second, if specified, on average over 10
seconds (i.e. don’t be alarmed if you exceed a little above the rate in some samples of your system monitor).
The program should print it’s progress in terms of “percentage completed” similar to the example above (in
case of a small file, it may print less than 100 times). A resumed download should continue from the last printed
percentage. When finishing without getting a signal the program should either print “download succeeded” or
“download failed”.
