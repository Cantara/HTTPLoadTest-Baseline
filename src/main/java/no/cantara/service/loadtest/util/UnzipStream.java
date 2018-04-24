package no.cantara.service.loadtest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipStream {
    private static final Logger log = LoggerFactory.getLogger(UnzipStream.class);


    public static void unxipFilename(String filename) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filename);
        unzip(fileInputStream);

    }

    public static void unzip(InputStream fileInputStream) throws IOException {
//        String fileZip = "compressed.zip";
        Map fileMap = new HashMap();
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(fileInputStream); //new FileInputStream(fileZip));
        int n = 1;
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            String fileName = zipEntry.getName();
            fileMap.put(n++, fileName);
            File newFile = new File("./" + fileName);
            newFile.getParentFile().mkdirs();
            if (!newFile.exists() && zipEntry.isDirectory()) {
                newFile.mkdir();
                // If you require it to make the entire directory path including parents,
            }
            if (!zipEntry.isDirectory()) {
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
        log.info("AddedFileMap( {})", fileMap);
    }
}

