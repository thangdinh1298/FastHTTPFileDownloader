## Current categories that needs contribution

* [ ] An HTTP server that supports range header which in turns allow for multi-threaded downloads for testing
* [ ] Write download entries to file and reload old download data from file
* [ ] Test suite for core functionalities stated for Stable build v0.1

# Core functionalities in Stable build v0.1
* [ ] Add new download from client
* [ ] Get all download info (index, file name, directory)
* [ ] Pause a created download
* [ ] Resume a created download
* [ ] Automatic file backup and restore

# Test scenarios for each functionality:
## Pause download
- Pause a finished download -> Fail: download is already finished
- Sucessfully pause a running download -> Success notification
- Pause a download while it is merging file -> Fail: a download can't be paused during merging stage
- Pause completed downloads loaded from file -> Fail: download is already finished
- Pause incomplete downloads loaded from file -> Fail: download is already finished
- Pause a download with index out of bound -> Fail: index is not valid

## Resume download
- Resume a completed download -> Fail: download is already finished
- Successfully resume a paused download -> Success
- Resume a download already running -> Fail: download is already finished
- Resume incomplete downloads loaded from file -> Success 
- Resume completed downloads loaded from file -> Fail: download is already finished
- Resume download with index out of range -> Fail: index is not valid

## Delete download
