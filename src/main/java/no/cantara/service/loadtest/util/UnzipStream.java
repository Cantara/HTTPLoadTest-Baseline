package no.cantara.service.loadtest.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UnzipStream {


    public static void unxipFilename(String filename) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(filename);
        unzip(fileInputStream);

    }

    public static void unzip(InputStream fileInputStream) throws IOException {
//        String fileZip = "compressed.zip";
        byte[] buffer = new byte[1024];
        ZipInputStream zis = new ZipInputStream(fileInputStream); //new FileInputStream(fileZip));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            String fileName = zipEntry.getName();
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
    }
}

