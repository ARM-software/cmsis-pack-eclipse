/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Class for different utilities
 */
public class Utils {

	static public final String QUOTE = "\"";  //$NON-NLS-1$
	static private String host = null;  	  // name of a running host OS : win, mac, or linux

	/**
	 * Find all pdsc recursively from given directory
	 * @param dir directory to start search
	 * @param files list to collect items, if null the list will be allocated
	 * @param depth number of sub-directory levels to search for: 0 - search current directory only
	 * @return list of found pdsc files
	 */
	static public Collection<String> findPdscFiles(File dir, Collection<String> files, int depth){
		if( files == null) {
			files = new LinkedList<String>();
		}

		File[] list = dir.listFiles();
        if (list == null) {
			return  files;
		}

        // search dir for pdsc files
        for ( File f : list ) {
            if( f.isFile() && !f.isHidden()) {
            	String name = f.getName();
            	if(!name.startsWith(".") && name.endsWith(CmsisConstants.EXT_PDSC)){   //$NON-NLS-1$
            		files.add(f.getAbsolutePath());
            	}
            }
        }

        if(depth <= 0) {
			return files;
		}
        // search sub-directories
        // search dir for pdsc files
        for ( File f : list ) {
            if( f.isDirectory() && !f.isHidden() && !f.getName().startsWith(".")) { //$NON-NLS-1$
            	findPdscFiles(f,  files, depth-1);
            }
        }
       	return  files;
	}

	/**
	 * Replaces all '*' and '?' to 'x'  and all non-alphanumeric chars to '_' in supplied string
	 * @param s source string
	 * @return the resulting string
	 */
	static public String wildCardsToX(String s)	{
		if(s == null || s.isEmpty()) {
			return s;
		}

		StringBuilder res = new StringBuilder();
		for(int i = 0; i < s.length(); i++){
			char ch = s.charAt(i);

			if(Character.isLetterOrDigit(ch) || ch== '-' || ch =='.') { // allowed characters
				// do nothing
			} else if(ch == '*' || ch == '?') { // wildcards
				ch = 'x';
			} else {// any other character
				ch = '_';
			}
			res.append(ch);
		}
		return res.toString();
	}

	/**
	 * Adds trailing slash to path
	 * @param path path to add slash
	 * @return the result string
	 */
	static public String addTrailingSlash(String path) {
		if(path == null || path.isEmpty() || path.endsWith("/")) { //$NON-NLS-1$
			return path;
		}
		if(path.endsWith(File.separator)) {
			path = removeTrailingSlash(path);
		}
		return path + "/";  //$NON-NLS-1$
	}

	/**
	 * Removes trailing slash from path
	 * @param path path to remove slash
	 * @return the result string
	 */
	static public String removeTrailingSlash(String path) {
		if(path == null || path.isEmpty()) {
			return path;
		}
		if(path.endsWith("/") || path.endsWith(File.separator)) { //$NON-NLS-1$
			return path.substring(0, path.length() - 1);
		}
		return path;
	}


	/**
	 * Extracts filename portion out of supplied pathname (leaves last segment only)
	 * @param path absolute or relative path with forward slashes as delimiters
	 * @return the result filename
	 */
	static public String extractFileName(String path) {
		if(path == null || path.isEmpty()) {
			return path;
		}

		int pos = path.lastIndexOf('/');
		if(pos < 0) {
			pos = path.lastIndexOf('\\');
		}
		if(pos >= 0 ) {
			return path.substring(pos + 1);
		}
		return path;
	}


	/**
	 * Extracts base filename portion (without extension) out of supplied pathname
	 * @param path absolute or relative path with forward slashes as delimiters
	 * @return the result filename
	 */
	static public String extractBaseFileName(String path) {
		if(path == null || path.isEmpty()) {
			return path;
		}

		String filename = extractFileName(path);
		int pos = filename.lastIndexOf('.');
		if(pos >= 0 ) {
			return filename.substring(0, pos);
		}
		return filename;
	}

	/**
	 * Extracts file extension (leaves last segment only)
	 * @param filename absolute or relative filename with forward slashes as delimiters
	 * @return the result filename
	 */
	static public String extractFileExtension(String filename) {
		if(filename == null || filename.isEmpty()) {
			return filename;
		}

		int pos = filename.lastIndexOf('.');
		if(pos >= 0 ) {
			return filename.substring(pos+1);
		}
		return null;
	}


	/**
	 * Extracts path portion out of supplied pathname (removes section out last slash)
	 * @param path absolute or relative path with forward slashes as delimiters
	 * @param keepSlash flag if to keep or remove trailing slash
	 * @return the result path
	 */
	static public String extractPath(String path, boolean keepSlash) {
		if(path == null || path.isEmpty()) {
			return path;
		}
		int pos = path.lastIndexOf('/');
		if(pos < 0) {
			pos = path.lastIndexOf('\\');
		}
		if(pos >= 0 ) {
			if(keepSlash) {
				pos++;
			}
			return path.substring(0, pos);
		}
		return path;
	}

	/**
	 * Returns a path equivalent to this path, but relative to the given base path if possible.
	 * @param path path to make relative
	 * @param basePath absolute base directory
	 * @return A path relative to the base path, or this path if it could not be made relative to the given base
	 */
	static public String makePathRelative(String path, String basePath) {
		if(path == null || basePath ==null || basePath.isEmpty()) {
			return path;
		}
		IPath p = new Path(path);
		IPath base = new Path(basePath);
		p = p.makeRelativeTo(base);
		return p.toString();
	}

	/**
	 * Check if a URL string is valid
	 * @param urlStr the URL string
	 * @return true if the URL is valid
	 */
	static public boolean isValidURL(String urlStr) {
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
	 * @param path path to surround with quotes
	 * @return quoted string
	 */
	static public String addQuotes(String path) {
		if(path == null || path.isEmpty()) {
			return path;
		}

		String quoted = CmsisConstants.EMPTY_STRING;

		if(!path.startsWith(QUOTE)) {
			quoted = QUOTE;
		}
		quoted += path;

		if(!path.endsWith(QUOTE)) {
			quoted += QUOTE;
		}
		return quoted;
	}

	/**
	 * Returns index of string in a string collection
	 * @param stringCollection collection of strings
	 * @param str string to search for
	 * @return index of the string if found, otherwise -1
	 */
	static public int indexOf(Collection<String> stringCollection, String str) {
		if (str != null && stringCollection != null) {
			int i = 0;
			for(String s : stringCollection) {
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
	 * @param stringArray collection of strings
	 * @param str string to search for
	 * @return index of the string if found, otherwise -1
	 */
	static public int indexOf(String[] stringArray, String str) {
		if (str != null && stringArray != null) {
			int i = 0;
			for(String s : stringArray) {
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
	 * @param dclock String representing decimal clock frequency
	 * @return scaled string
	 */
	static public String getScaledClockFrequency(String dclock)
	{
	  if (dclock == null || dclock.isEmpty()) {
	    return CmsisConstants.EMPTY_STRING;
	  }

	  int len = dclock.length();
	  if (len > 6) {
	    return (dclock.substring(0, len - 6) + " MHz"); //$NON-NLS-1$
	  } else if (len > 3) {
	    return (dclock.substring(0, len - 3) + " kHz"); //$NON-NLS-1$
	  } else {
	    return (dclock.substring(0, len - 6) + " Hz"); //$NON-NLS-1$
	  }
	}


	/**
	 * Returns readable representation of memory size
	 * @param size memory size in bytes
	 * @return readable memory size string
	 */
	static public String getMemorySizeString(long size)
	{
	  if (size == 0) {
		  return CmsisConstants.EMPTY_STRING;
	  }

	  if (size < 1024) {
	    return Long.toString(size) + " Byte"; //$NON-NLS-1$
	  }

	  size >>= 10; // Scale to kByte
	  if (size < 1024 || (size % 1024) != 0) {
	    // Less than a MByte or division with rest => show kByte
	    return Long.toString(size) + " kB"; //$NON-NLS-1$
	  }

	  size >>= 10; // Scale to MByte
	  return Long.toString(size) + " MB"; //$NON-NLS-1$
	}

	/**
	 * Copy from one directory to another
	 *
	 * @param sourceLocation source directory
	 * @param destLocation destination directory
	 * @throws IOException
	 */
	public static void copyDirectory(File sourceLocation, File destLocation) throws IOException {
		if(sourceLocation == null) {
			return;
		}
		if (sourceLocation.isDirectory()) {
			String[] children = sourceLocation.list();
			if(children == null) {
				return;
			}
			for (String child : children) {
				copyDirectory(new File(sourceLocation, child), new File(destLocation, child));
			}
		} else {
			if (!destLocation.getParentFile().exists()) {
				destLocation.getParentFile().mkdirs();
			}
			copy(sourceLocation, destLocation);
		}
	}

	/**
	 * Copy from one directory to another
	 *
	 * @param srcDir source directory
	 * @param dstDir destination directory
	 * @param ignoreDir directories that should ignore during copy (directories w/ absolute path)
	 * @throws IOException
	 */
	public static void copyDirectoryWithProgress(File srcDir, File dstDir, Set<String> ignoreDir, IProgressMonitor monitor) throws IOException {
		if(srcDir == null || (ignoreDir != null && ignoreDir.contains(srcDir.getAbsolutePath()))) {
			return;
		}
		if (srcDir.isDirectory()) {
			String[] children = srcDir.list();
			if(children == null) {
				return;
			}
			for (String child : children) {
				copyDirectoryWithProgress(new File(srcDir, child), new File(dstDir, child), ignoreDir, monitor);
			}
		} else {
			if (!dstDir.getParentFile().exists()) {
				dstDir.getParentFile().mkdirs();
			}
			copy(srcDir, dstDir);
			monitor.worked(1);
		}
	}

	/**
	 * Get the String of current date in the format of "dd-mm-yyyy"
	 * @return String of current date in the format of "dd-mm-yyyy"
	 */
	public static String getCurrentDate() {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy"); //$NON-NLS-1$
		LocalDate localDate = LocalDate.now();
		String[] date = dtf.format(localDate).split("/"); //$NON-NLS-1$
		return String.valueOf(Integer.parseInt(date[0])) + '-'
				+ String.valueOf(Integer.parseInt(date[1])) + '-'
				+ String.valueOf(Integer.parseInt(date[2]));
	}

	/**
	 * Copy sourceFile to destFile
	 *
	 * @param source source file
	 * @param dest destination file
	 */
	public static void copy(File source, File dest) throws IOException {
		InputStream input = null;
		OutputStream output = null;
		if (!dest.getParentFile().exists()) {
			dest.getParentFile().mkdirs();
		}
		try {
			input = new FileInputStream(source);
			if (dest.exists()) {
				dest.delete();
			}
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
			dest.setWritable(true, true);
		} finally {
			if (input != null) {
				input.close();
			}
			if (output != null) {
				output.close();
			}
		}
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
			if(files == null) {
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
	 * @param archiveFile the zip file
	 * @return the number of files contained in this zip file
	 * @throws IOException
	 */
	public static int getFilesCount(File archiveFile) throws IOException {
		ZipInputStream zipInput;
		zipInput = new ZipInputStream(new FileInputStream(archiveFile));
		ZipEntry zipEntry = zipInput.getNextEntry();
		int count = 0;
		while (zipEntry != null) {
			if (!zipEntry.isDirectory()) {
				count++;
			}
			zipEntry = zipInput.getNextEntry();
		}
		zipInput.closeEntry();
		zipInput.close();

		return count;
	}

	/**
	 * Delete the folder recursively with progress monitor: first file, then folder
	 *
	 * @param folder the folder
	 */
	public static void deleteFolderRecursiveWithProgress(File folder, IProgressMonitor monitor) {

		if (folder == null) {
			return;
		}

		if (folder.isFile()) {
			folder.setWritable(true, false);
			folder.delete();
			return;
		}

		if (folder.exists()) {
			File[] files = folder.listFiles();
			if(files == null) {
				return;
			}
			for (File f : files) {
				if (f.isDirectory()) {
					deleteFolderRecursiveWithProgress(f, monitor);
					f.setWritable(true, false);
					f.delete();
				} else {
					f.setWritable(true, false);
					f.delete();
					monitor.worked(1);
				}
			}
			folder.setWritable(true, false);
			folder.delete();
		}
	}

	/**
	 * Count the number of files in specific folder
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
			if(files == null) {
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
	 * @param folder the root folder
	 * @param extension extension of the files whose read-only flag should be cleared,
	 * use an empty string to clear the read-only flag on all the files
	 */
	public static void clearReadOnly(File folder, String extension) {
		if (folder == null) {
			return;
		}

		if (folder.exists()) {
			File[] files = folder.listFiles();
			if(files == null) {
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
			if(files == null) {
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
	 * @param set1 first set
	 * @param set2 second set
	 * @return true if both sets contain at least one common member
	 */
	public static <T> boolean checkIfIntersect(Set<T> set1, Set<T> set2) {
		if(set1 == null || set2 == null) {
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
	 * @return host type
	 */
	public static String getHostType(){
		if(host == null) {
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
}
