/*******************************************************************************
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *
 * OS type detection is based on algorithm published here:
 * http://stackoverflow.com/questions/228477/how-do-i-programmatically-determine-operating-system-in-java
 *
 *******************************************************************************/

package com.arm.cmsis.pack.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Class for different utilities
 */
public class Utils {

    public static final String QUOTE = "\""; //$NON-NLS-1$
    private static String host = null; // name of a running host OS : win, mac, or linux

    private static final char[] NUMERIC_SUFFIXES = new char[] { 'K', 'M', 'G' };
    private static final int[] NUMERIC_SHIFTS = new int[] { 10, 20, 30 };

    /**
     * Private constructor to prevent instantiating the utility class
     */
    private Utils() {
        throw new IllegalStateException("Utility class"); //$NON-NLS-1$
    }

    /**
     * Find files recursively from given directory (hidden files are exculed)
     *
     * @param dir   directory to start search
     * @param ext   file extension to consider, or null to list all files
     * @param files list to collect items, if null the list will be allocated
     * @param depth number of sub-directory levels to search for: 0 - search current
     *              directory only
     * @return list of found files
     */
    public static Collection<String> findFiles(File dir, String ext, Collection<String> files, int depth) {
        if (files == null) {
            files = new ArrayList<>();
        }

        File[] list = dir.listFiles();
        if (list == null) {
            return files;
        }

        // search directory for files with given extension
        for (File f : list) {
            if (!f.isFile() || f.isHidden()) {
                continue;
            }
            String name = f.getName();
            if (name.startsWith(CmsisConstants.DOT))
                continue;
            if (ext != null) {
                String fileExt = extractFileExtension(name);
                if (!ext.equals(fileExt)) {
                    continue;
                }
            }
            files.add(f.getAbsolutePath());
        }

        if (depth <= 0) {
            return files;
        }
        // search sub-directories
        for (File f : list) {
            if (f.isDirectory() && !f.isHidden() && !f.getName().startsWith(".")) { //$NON-NLS-1$
                findFiles(f, ext, files, depth - 1);
            }
        }
        return files;
    }

    /**
     * Find all pdsc recursively from given directory
     *
     * @param dir   directory to start search
     * @param files list to collect items, if null the list will be allocated
     * @param depth number of sub-directory levels to search for: 0 - search current
     *              directory only
     * @return list of found pdsc files
     */
    public static Collection<String> findPdscFiles(File dir, Collection<String> files, int depth) {
        return findFiles(dir, CmsisConstants.PDSC, files, depth);
    }

    /**
     * Replaces all '*' and '?' to 'x' and all non-alphanumeric chars to '_' in
     * supplied string
     *
     * @param s source string
     * @return the resulting string
     */
    public static String wildCardsToX(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        StringBuilder res = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '.') { // allowed characters
                // do nothing
            } else if (ch == '*' || ch == '?') { // wildcards
                ch = 'x';
            } else {// any other character
                ch = '_';
            }
            res.append(ch);
        }
        return res.toString();
    }

    /**
     * Replaces all non-alphanumeric characters (slashes, spaces, wildcards, etc.)
     * to '_' in supplied string
     *
     * @param s source string
     * @return the resulting string
     */
    public static String nonAlnumToUndersore(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        StringBuilder res = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (!Character.isLetterOrDigit(ch)) {
                ch = '_';
            }
            res.append(ch);
        }
        return res.toString();
    }

    /**
     * Adds trailing slash to path
     *
     * @param path path to add slash
     * @return the result string
     */
    public static String addTrailingSlash(String path) {
        if (path == null || path.isEmpty() || path.endsWith("/")) { //$NON-NLS-1$
            return path;
        }
        if (path.endsWith(File.separator)) {
            path = removeTrailingSlash(path);
        }
        return path + CmsisConstants.SLASH;
    }

    /**
     * Removes trailing slash from path
     *
     * @param path path to remove slash
     * @return the result string
     */
    public static String removeTrailingSlash(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        if (path.endsWith(CmsisConstants.SLASH) || path.endsWith(File.separator)) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Extracts filename portion out of supplied pathname (leaves last segment only)
     *
     * @param path absolute or relative path with forward slashes as delimiters
     * @return the result filename
     */
    public static String extractFileName(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        int pos = path.lastIndexOf('/');
        if (pos < 0) {
            pos = path.lastIndexOf('\\');
        }
        if (pos >= 0) {
            return path.substring(pos + 1);
        }
        return path;
    }

    /**
     * Extracts base filename portion (without extension) out of supplied pathname
     *
     * @param path absolute or relative path with forward slashes as delimiters
     * @return the result filename
     */
    public static String extractBaseFileName(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        String filename = extractFileName(path);
        int pos = filename.lastIndexOf('.');
        if (pos >= 0) {
            return filename.substring(0, pos);
        }
        return filename;
    }

    /**
     * Extracts absolute base filename portion (without extension) out of supplied
     * pathname
     *
     * @param path absolute or relative path
     * @return the result filename
     */
    public static String removeFileExtension(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        int pos = path.lastIndexOf('.');
        if (pos >= 0) {
            return path.substring(0, pos);
        }
        return path;
    }

    /**
     * Extracts file extension (leaves last segment only)
     *
     * @param filename absolute or relative filename
     * @return file extension or null if file has no dot (file. will return an empty
     *         string)
     */
    public static String extractFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return filename;
        }

        int pos = filename.lastIndexOf('.');
        if (pos >= 0) {
            return filename.substring(pos + 1);
        }
        return null;
    }

    /**
     * Replaces file extension with the new one
     *
     * @param path         absolute or relative filename
     * @param newExtension new file extension
     * @return the result filename
     */
    public static String changeFileExtension(String path, String newExtension) {
        String newfileName = removeFileExtension(path);
        if (newfileName != null && newExtension != null)
            return newfileName + '.' + newExtension;
        return path;
    }

    /**
     * Extracts path portion out of supplied pathname (removes section out last
     * slash)
     *
     * @param path      absolute or relative path with forward slashes as delimiters
     * @param keepSlash flag if to keep or remove trailing slash
     * @return the result path
     */
    public static String extractPath(String path, boolean keepSlash) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        int pos = path.lastIndexOf('/');
        if (pos < 0) {
            pos = path.lastIndexOf('\\');
        }
        if (pos >= 0) {
            if (keepSlash) {
                pos++;
            }
            return path.substring(0, pos);
        }
        return path;
    }

    /**
     * Returns number of path segments in the path
     *
     * @param path absolute or relative path with forward slashes as delimiters
     * @return number of segments
     */
    public static int getSegmentCount(String path) {
        if (path == null || path.isEmpty())
            return 0;
        path = path.replace('\\', '/');
        int count = 1;
        for (int pos = path.indexOf('/'); pos >= 0; pos = path.indexOf('/', pos + 1)) {
            count++;
        }
        return count;
    }

    /**
     * Removes last path segments
     *
     * @param path      absolute or relative path with forward slashes as delimiters
     * @param nSegments number of segments to remove
     * @return the result path
     */
    public static String removeTrailingPathSegments(String path, int nSegments) {
        for (int i = 0; i < nSegments; i++) {
            path = extractPath(path, false);
        }
        return path;
    }

    /**
     * Check if a URL string is valid
     *
     * @param urlStr the URL string
     * @return true if the URL is valid
     */
    public static boolean isValidURL(String urlStr) {
        if (urlStr == null) {
            return false;
        }
        try {
            new URL(urlStr);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Surround supplied path with quotes if needed
     *
     * @param path path to surround with quotes
     * @return quoted string
     */
    public static String addQuotes(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        String quoted = CmsisConstants.EMPTY_STRING;

        if (!path.startsWith(QUOTE)) {
            quoted = QUOTE;
        }
        quoted += path;

        if (!path.endsWith(QUOTE)) {
            quoted += QUOTE;
        }
        return quoted;
    }

    /**
     * Returns index of string in a string collection
     *
     * @param stringCollection collection of strings
     * @param str              string to search for
     * @return index of the string if found, otherwise -1
     */
    public static int indexOf(Collection<String> stringCollection, String str) {
        if (str != null && stringCollection != null) {
            int i = 0;
            for (String s : stringCollection) {
                if (s.equals(str)) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    /**
     * Returns index of string in a string array
     *
     * @param stringArray collection of strings
     * @param str         string to search for
     * @return index of the string if found, otherwise -1
     */
    public static int indexOf(String[] stringArray, String str) {
        if (str != null && stringArray != null) {
            int i = 0;
            for (String s : stringArray) {
                if (s.equals(str)) {
                    return i;
                }
                i++;
            }
        }
        return -1;
    }

    /**
     * Returns clock value with suffix
     *
     * @param dclock String representing decimal clock frequency
     * @return scaled string
     */
    public static String getScaledClockFrequency(String dclock) {
        if (dclock == null || dclock.isEmpty()) {
            return CmsisConstants.EMPTY_STRING;
        }

        int len = dclock.length();
        if (len > 6) {
            return (dclock.substring(0, len - 6) + " MHz"); //$NON-NLS-1$
        } else if (len > 3) {
            return (dclock.substring(0, len - 3) + " kHz"); //$NON-NLS-1$
        } else {
            return (dclock + " Hz"); //$NON-NLS-1$
        }
    }

    /**
     * Returns readable representation of memory size
     *
     * @param size memory size in bytes
     * @return readable memory size string
     */
    public static String getMemorySizeString(long size) {
        if (size == 0) {
            return CmsisConstants.EMPTY_STRING;
        }

        if (size < 1024) {
            return Long.toString(size) + " Byte"; //$NON-NLS-1$
        }

        size >>= 10; // Scale to kByte
        if (size < 1024 || (size % 1024) != 0) {
            // Less than a MByte or division with rest => show kByte
            return Long.toString(size) + " KB"; //$NON-NLS-1$
        }

        size >>= 10; // Scale to MByte
        return Long.toString(size) + " MB"; //$NON-NLS-1$
    }

    /**
     * Returns readable representation of memory size
     *
     * @param size memory size in bytes
     * @return readable memory size string
     */
    public static String getFormattedMemorySizeString(Long size) {
        if (size == null) {
            return "??????"; //$NON-NLS-1$
        }

        if (size == 0) {
            return CmsisConstants.EMPTY_STRING;
        }

        if (size < 1024 || (size % 1024) != 0) {
            return String.format("   %4d B", size); //$NON-NLS-1$
        }

        size >>= 10; // Scale to kByte
        if (size < 1024 || (size % 1024) != 0) {
            return String.format("   %4d KB", size); //$NON-NLS-1$
        }

        size >>= 10; // Scale to MByte
        return String.format("   %4d MB", size); //$NON-NLS-1$
    }

    /**
     * Converts supplied string to long value, respects Byte, KB, MB and GB suffixes
     *
     * @param value string to convert
     * @return Long value if successful, otherwise null
     * @see getFormattedMemorySizeString
     */
    public static Long stringToLong(String value) {
        if (value == null || value.isEmpty())
            return null;

        int shift = 0;
        value = value.trim().trim().toUpperCase();
        // find suffixes
        for (int i = 0; i < NUMERIC_SUFFIXES.length; i++) {
            int pos = value.indexOf(NUMERIC_SUFFIXES[i]);
            if (pos >= 0) {
                shift = NUMERIC_SHIFTS[i];
                value = value.substring(0, pos).trim();
                break;
            }
        }
        // for all other suffixes just extract numeric before space
        int delimiter = value.indexOf(' ');
        if (delimiter >= 0) {
            value = value.substring(0, delimiter);
        }
        Long result = null;
        try {
            result = Long.decode(value);
        } catch (NumberFormatException e) {
            return null;
        }
        if (shift > 0) {
            result <<= shift;
        }
        return result;
    }

    /**
     * Copy from one directory to another overwriting existing entries
     *
     * @param sourceLocation source directory
     * @param destLocation   destination directory
     * @throws IOException
     */
    public static void copyDirectory(File sourceLocation, File destLocation) throws IOException {
        copyDirectory(sourceLocation, destLocation, true);
    }

    /**
     * Copy from one directory to another
     *
     * @param sourceLocation source directory
     * @param destLocation   destination directory
     * @param overwrite      boolean flag specifies if to overwrite existing entries
     * @throws IOException
     */
    public static void copyDirectory(File sourceLocation, File destLocation, boolean overwrite) throws IOException {
        if (sourceLocation == null) {
            return;
        }
        if (sourceLocation.isDirectory()) {
            String[] children = sourceLocation.list();
            if (children == null) {
                return;
            }
            for (String child : children) {
                copyDirectory(new File(sourceLocation, child), new File(destLocation, child), overwrite);
            }
        } else {
            if (!destLocation.getParentFile().exists()) {
                destLocation.getParentFile().mkdirs();
            }
            copy(sourceLocation, destLocation, overwrite);
        }
    }

    /**
     * Get the String of current date in the format of "dd-mm-yyyy"
     *
     * @return String of current date in the format of "dd-mm-yyyy"
     */
    public static String getCurrentDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy"); //$NON-NLS-1$
        LocalDate localDate = LocalDate.now();
        String[] date = dtf.format(localDate).split("/"); //$NON-NLS-1$
        return String.valueOf(Integer.parseInt(date[0])) + '-' + String.valueOf(Integer.parseInt(date[1])) + '-'
                + String.valueOf(Integer.parseInt(date[2]));
    }

    /**
     * Copy sourceFile to destFile overwriting existing file
     *
     * @param source source file
     * @param dest   destination file
     */
    public static void copy(File source, File dest) throws IOException {
        copy(source, dest, true);
    }

    /**
     * Copy sourceFile to destFile
     *
     * @param source    source file
     * @param dest      destination file
     * @param overwrite boolean flag specifies if to overwrite the existing file
     */
    public static void copy(File source, File dest, boolean overwrite) throws IOException {

        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        } else if (dest.exists()) {
            if (!overwrite)
                return; // keep existing file
            if (dest.equals(source))
                return; // do not copy to itself
            if (!dest.delete()) {
                return; // cannot overwrite
            }
        }

        try (InputStream input = new FileInputStream(source); OutputStream output = new FileOutputStream(dest);) {
            byte[] buf = new byte[4096]; // 4096 is a common NTFS block size
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        }
        dest.setWritable(true, true);
    }

    /**
     * Delete the folder recursively: first file, then folder
     *
     * @param folder the folder
     */
    public static void deleteFolderRecursive(File folder) {
        if (folder == null) {
            return;
        }
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolderRecursive(f);
                    f.setWritable(true, false);
                    f.delete();
                } else {
                    f.setWritable(true, false);
                    f.delete();
                }
            }
            folder.setWritable(true, false);
            folder.delete();
        }
    }

    /**
     * Count the number of files in specific folder
     *
     * @param folder the root folder
     * @return the number of files in folder
     */
    public static int countFiles(File folder) {
        if (folder == null) {
            return 0;
        }

        if (folder.isFile()) {
            return 1;
        }

        int count = 0;
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files == null) {
                return 0;
            }
            for (File f : files) {
                if (f.isDirectory()) {
                    count += countFiles(f);
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Clear the read-only flag
     *
     * @param folder    the root folder
     * @param extension extension of the files whose read-only flag should be
     *                  cleared, use an empty string to clear the read-only flag on
     *                  all the files
     */
    public static void clearReadOnly(File folder, String extension) {
        if (folder == null) {
            return;
        }

        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (f.isDirectory()) {
                    clearReadOnly(f, extension);
                    f.setWritable(true, false);
                } else if (extension == null || extension.isEmpty() || f.getName().endsWith(extension)) {
                    f.setWritable(true, false);
                }
            }
            folder.setWritable(true, false);
        }
    }

    /**
     * Set the read-only flag
     *
     * @param folder the folder
     */
    public static void setReadOnly(File folder) {
        if (folder == null) {
            return;
        }

        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files == null) {
                return;
            }
            for (File f : files) {
                if (f.isDirectory()) {
                    setReadOnly(f);
                    f.setReadOnly();
                } else {
                    f.setReadOnly();
                }
            }
            folder.setReadOnly();
        }
    }

    /**
     * Checks if two sets intersect
     *
     * @param set1 first set
     * @param set2 second set
     * @return true if both sets contain at least one common member
     */
    public static <T> boolean checkIfIntersect(Set<T> set1, Set<T> set2) {
        if (set1 == null || set2 == null) {
            return false;
        }
        if (set1.size() > set2.size()) {
            return checkIfIntersect(set2, set1);
        }
        for (T o : set1) {
            if (set2.contains(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns host type: win, mac or linux
     *
     * @return host type
     */
    public static String getHostType() {
        if (host == null) {
            String os = System.getProperty("os.name", CmsisConstants.EMPTY_STRING).toLowerCase(); //$NON-NLS-1$
            if (os.contains(CmsisConstants.MAC) || os.contains("darwin")) { //$NON-NLS-1$
                host = CmsisConstants.MAC;
            } else if (os.contains("nux") || os.contains("nix")) { //$NON-NLS-1$ //$NON-NLS-2$
                host = CmsisConstants.LINUX;
            } else if (os.contains(CmsisConstants.WIN)) {
                host = CmsisConstants.WIN;
            } else {
                host = CmsisConstants.EMPTY_STRING;
            }
        }
        return host;
    }

    /**
     * Returns current time as string
     *
     * @return time stamp string
     */
    public static String getCurrentTimeStamp() {
        long startTime = System.currentTimeMillis();
        return new SimpleDateFormat("HH:mm:ss").format(new Date(startTime)); //$NON-NLS-1$
    }

    /**
     * Returns suffix of string with given delimiter
     *
     * @param string    s
     * @param delimiter determines how to split string
     * @return suffix or null if no delimiter is found
     */
    public static String getSuffix(String s, String delimiter) {
        int index = s.indexOf(delimiter);
        if (index == -1) {
            return null;
        }
        return s.substring(index + delimiter.length());
    }

    /**
     * Returns prefix of string with given delimiter
     *
     * @param string    s
     * @param delimiter determines how to split string
     * @return prefix or null if no delimiter is found
     */
    public static String getPrefix(String s, String delimiter) {
        int index = s.indexOf(delimiter);
        if (index == -1) {
            return null;
        }
        return s.substring(0, index);
    }

    /**
     * Strips prefix if present
     *
     * @param string    s
     * @param delimiter
     * @return suffix or same string if delimiter not found
     */
    public static String stripPrefix(String s, String delimiter) {
        String suffix = getSuffix(s, delimiter);
        if (suffix == null) {
            return s;
        }
        return suffix;
    }
}
