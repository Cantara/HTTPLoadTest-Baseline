#!/bin/sh

curl -F "file=@demo_loadtest_setup.zip;filename=demo_loadtest_setup.zip" http://localhost:8086/HTTPLoadTest-baseline/loadTest/zip

#wget --header="Content-type: multipart/form-data boundary=FILEUPLOAD" --post-file demo_loadtest_setup.zip http://localhost:8086/HTTPLoadTest-baseline/loadTest/zip