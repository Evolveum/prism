/*
 * Copyright (C) 2010-2022 Evolveum and contributors
 *
 * This work is dual-licensed under the Apache License 2.0
 * and European Union Public License. See LICENSE file for details.
 */
package com.evolveum.midpoint.util;

import java.io.*;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.apache.commons.lang3.StringUtils;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;

/**
 * Various class path, class loading and class scanning utilities.
 *
 * For more info about class-path scanning: https://github.com/classgraph/classgraph
 */
public class ClassPathUtil {

    public static final Trace LOGGER = TraceManager.getTrace(ClassPathUtil.class);

    public static final String DEFAULT_PACKAGE_TO_SCAN = "com.evolveum.midpoint";

    public static Set<Class<?>> listClasses(Package pkg) {
        return listClasses(pkg.getName());
    }

    public static Set<Class<?>> listClasses(String... packageNames) {
        try (ScanResult scan = new ClassGraph()
                .acceptPackages(packageNames)
                .scan()) {
            List<Class<?>> classes = scan.getAllClasses().loadClasses();
            LOGGER.debug("Found {} classes in packages {}", classes.size(), packageNames);

            return new HashSet<>(classes);
        }
    }

    public static Collection<Class<?>> scanClasses(Class<? extends Annotation> annotationClass, String packageNames) {
        List<String> packages = new ArrayList<>();

        if (StringUtils.isNotEmpty(packageNames)) {
            String[] array = packageNames.split(",");
            for (String a : array) {
                String p = a.trim();
                if (StringUtils.isNotEmpty(p)) {
                    packages.add(p);
                }
            }
        }

        return scanClasses(annotationClass, packages.toArray(new String[0]));
    }

    public static Collection<Class<?>> scanClasses(Class<? extends Annotation> annotationClass, String... packageNames) {
        LOGGER.debug("Scanning classes for: {} with package scope: {}", annotationClass, packageNames);
        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(packageNames)
                .enableClassInfo()
                .enableAnnotationInfo()
                .scan()) {
            List<Class<?>> classes = scanResult
                    .getClassesWithAnnotation(annotationClass)
                    .loadClasses();
            LOGGER.debug("Found {} classes with annotation {}", classes.size(), annotationClass.getName());
            return classes;
        }
    }

    /**
     * Extract specified source on class path to file system dst
     *
     * @param src source
     * @param dst destination
     * @return successful extraction
     */
    public static boolean extractFileFromClassPath(String src, String dst) {
        InputStream is = ClassPathUtil.class.getClassLoader().getResourceAsStream(src);
        if (null == is) {
            LOGGER.error("Unable to find file {} for extraction to {}", src, dst);
            return false;
        }

        return copyFile(is, src, dst);
    }

    public static boolean copyFile(InputStream srcStream, String srcName, String dstPath) {
        OutputStream dstStream;
        try {
            dstStream = new FileOutputStream(dstPath);
        } catch (FileNotFoundException e) {
            LOGGER.error("Unable to open destination file " + dstPath + ":", e);
            return false;
        }
        return copyFile(srcStream, srcName, dstStream, dstPath);
    }

    public static boolean copyFile(InputStream srcStream, String srcName, File dstFile) {
        OutputStream dstStream;
        try {
            dstStream = new FileOutputStream(dstFile);
        } catch (FileNotFoundException e) {
            LOGGER.error("Unable to open destination file " + dstFile + ":", e);
            return false;
        }
        return copyFile(srcStream, srcName, dstStream, dstFile.toString());
    }

    public static boolean copyFile(InputStream srcStream, String srcName, OutputStream dstStream, String dstName) {
        byte[] buf = new byte[655360];
        int len;
        try {
            while ((len = srcStream.read(buf)) > 0) {
                try {
                    dstStream.write(buf, 0, len);
                } catch (IOException e) {
                    LOGGER.error("Unable to write file " + dstName + ":", e);
                    return false;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Unable to read file " + srcName + " from classpath", e);
            return false;
        }
        try {
            dstStream.close();
        } catch (IOException e) {
            LOGGER.error("Unable to close file " + dstName + ":", e);
            return false;
        }
        try {
            srcStream.close();
        } catch (IOException e) {
            LOGGER.error("This never happened:", e);
            return false;
        }

        return true;
    }

    /**
     * Extracts all files in a directory on a classPath (system resource) to
     * a directory on a file system.
     */
    public static boolean extractFilesFromClassPath(String srcPath, String dstPath, boolean overwrite) throws URISyntaxException, IOException {
        URL src = ClassPathUtil.class.getClassLoader().getResource(srcPath);
        if (src == null) {
            LOGGER.debug("No resource for {}", srcPath);
            return false;
        }
        URI srcUrl = src.toURI();

        String[] parts = srcUrl.toString().split("!/");
        if (parts.length == 3
                && parts[1].equals("WEB-INF/classes")) {
            // jar:file:<ABSOLUTE_PATH>/midpoint.war!/WEB-INF/classes!/initial-midpoint-home
            srcUrl = URI.create(parts[0] + "!/" + parts[1] + "/" + parts[2]);
        }

        LOGGER.trace("URL: {}", srcUrl);
        if (srcUrl.toString().contains("!/")) {
            String uri = srcUrl.toString().split("!/")[0].replace("jar:", "");
            // file:<ABSOLUTE_PATH>/midpoint.war
            URI srcFileUri = URI.create(uri);
            File srcFile = new File(srcFileUri);
            JarFile jar = new JarFile(srcFile);
            Enumeration<JarEntry> entries = jar.entries();
            JarEntry jarEntry;
            while (entries.hasMoreElements()) {
                jarEntry = entries.nextElement();

                // skip other files
                if (!jarEntry.getName().contains(srcPath)) {
                    LOGGER.trace("Not relevant: {}", jarEntry.getName());
                    continue;
                }

                // prepare destination file
                String entryName = jarEntry.getName();

                String filepath = entryName.substring(entryName.indexOf(srcPath) + srcPath.length());
                File dstFile = new File(dstPath, filepath);

                if (!overwrite && dstFile.exists()) {
                    LOGGER.debug("Skipping file {}: exists", dstFile);
                    continue;
                }

                if (jarEntry.isDirectory()) {
                    dstFile.mkdirs();
                    continue;
                }

                InputStream is = ClassLoader.getSystemResourceAsStream(jarEntry.getName());
                LOGGER.debug("Copying {} from {} to {} ", jarEntry.getName(), srcFile, dstFile);
                copyFile(is, jarEntry.getName(), dstFile);
            }
            jar.close();
        } else {
            try {
                File file = new File(srcUrl);
                File[] files = file.listFiles();
                for (File subFile : files) {
                    File dstFile = new File(dstPath, subFile.getName());
                    if (subFile.isDirectory()) {
                        LOGGER.debug("Copying directory {} to {} ", subFile, dstFile);
                        MiscUtil.copyDirectory(subFile, dstFile);
                    } else {
                        LOGGER.debug("Copying file {} to {} ", subFile, dstFile);
                        MiscUtil.copyFile(subFile, dstFile);
                    }
                }
            } catch (Exception ex) {
                throw new IOException(ex);
            }
        }
        return true;
    }
}
