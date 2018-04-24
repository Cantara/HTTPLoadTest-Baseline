#!/bin/sh

curl -F "file=@loadtest_setup.zip;filename=loadtest_setup.zip" http://localhost:8086/HTTPLoadTest-baseline/loadTest/zip

#wget --header="Content-type: multipart/form-data boundary=FILEUPLOAD" --post-file loadtest_setup.zip http://localhost:8086/HTTPLoadTest-baseline/loadTest/zip