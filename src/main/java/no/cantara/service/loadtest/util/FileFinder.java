package no.cantara.service.loadtest.util;
/*
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import static java.nio.file.FileVisitResult.CONTINUE;


/**
 * Sample code that finds files that
 * match the specified glob pattern.
 * For more information on what
 * constitutes a glob pattern, see
 * http://docs.oracle.com/javase/javatutorials/tutorial/essential/io/fileOps.html#glob
 * <p>
 * The file or directories that match
 * the pattern are printed to
 * standard out.  The number of
 * matches is also printed.
 * <p>
 * When executing this application,
 * you must put the glob pattern
 * in quotes, so the shell will not
 * expand any wild cards:
 * java Find . -name "*.java"
 */

public class FileFinder extends SimpleFileVisitor<Path> {

    private static final Logger log = LoggerFactory.getLogger(HTTPResultUtil.class);

    /**
     * A {@code FileVisitor} that finds
     * all files that match the
     * specified pattern.
     */

    private final PathMatcher matcher;
    private int numMatches = 0;
    private ArrayList<String> filenames = new ArrayList<>();

    public FileFinder(String pattern) {
        matcher = FileSystems.getDefault()
                             .getPathMatcher("glob:" + pattern);
    }

    // Compares the glob pattern against
    // the file or directory name.
    public void find(Path file) {
        Path name = file.getFileName();
        if (name != null && matcher.matches(name)) {
            numMatches++;
            filenames.add(file.toString());
            System.out.println(file);
        }
    }

    // Prints the total number of
    // matches to standard out.
    public ArrayList<String> done() {
        System.out.println("Matched: "
                + numMatches);
        return filenames;
    }

    // Invoke the pattern matching
    // method on each file.
    @Override
    public FileVisitResult visitFile(Path file,
                                     BasicFileAttributes attrs) {
        find(file);
        return CONTINUE;
    }

    // Invoke the pattern matching
    // method on each directory.
    @Override
    public FileVisitResult preVisitDirectory(Path dir,
                                             BasicFileAttributes attrs) {
        find(dir);
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file,
                                           IOException exc) {
        System.err.println(exc);
        return CONTINUE;
    }


    static void usage() {
        System.err.println("java Find <path>" +
                " -name \"<glob_pattern>\"");
        System.exit(-1);
    }

    public static void main(String[] args)
            throws IOException {

        if (args.length < 3 || !args[1].equals("-name"))
            usage();

        Path startingDir = Paths.get(args[0]);
        String pattern = args[2];

        FileFinder finder = new FileFinder(pattern);
        Files.walkFileTree(startingDir, finder);
        finder.done();
    }
}