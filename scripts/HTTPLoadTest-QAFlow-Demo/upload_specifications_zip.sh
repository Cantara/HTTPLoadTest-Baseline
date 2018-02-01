#!/bin/sh

curl -F "file=@specifications.zip;filename=specifications.zip" http://localhost:8086/HTTPLoadTest-baseline/loadTest/zip

#wget --header="Content-type: multipart/form-data boundary=FILEUPLOAD" --post-file specifications.zip http://localhost:8086/HTTPLoadTest-baseline/loadTest/zip