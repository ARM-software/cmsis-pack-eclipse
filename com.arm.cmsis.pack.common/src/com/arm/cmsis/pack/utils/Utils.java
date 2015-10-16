/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.utils;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Class for different utilities  
 */
public class Utils {

	static public final String QUOTE = "\"";  //$NON-NLS-1$
	/**
	 * Find all pdsc recursively from given directory  
	 * @param dir directory to start search  
	 * @param files list to collect items, if null the list will be allocated
	 * @param depth number of sub-directory levels to search for: 0 - search current directory only 
	 * @return list of found pdsc files
	 */
	static public Collection<String> findPdscFiles(File dir, Collection<String> files, int depth){
		if( files == null)
			 files = new LinkedList<String>();
		
		File[] list = dir.listFiles();
        if (list == null) 
        	return  files;
        boolean found = false;
        // search dir for pdsc files 
        for ( File f : list ) {
            if( f.isFile() && !f.isHidden()) {
            	String name = f.getName();
            	if(!name.startsWith(".") && name.endsWith(".pdsc")){   //$NON-NLS-1$ //$NON-NLS-2$
            		files.add(f.getAbsolutePath());
            		found = true;
            	}
            }
        }
        if(found) // do not search sub-directories, because they cannot contain other pdsc files  
        	return files;
		
        if(depth <=0 ) // max depth is reached 
        	return files;
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
		if(s == null || s.isEmpty())
			return s;

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
		if(path == null || path.isEmpty() || path.endsWith("/")) //$NON-NLS-1$
			return path;
		if(path.endsWith(File.separator)) 
			path = removeTrailingSlash(path);
		return path + "/";  //$NON-NLS-1$
	}
	
	/**
	 * Removes trailing slash from path
	 * @param path path to remove slash 
	 * @return the result string 
	 */
	static public String removeTrailingSlash(String path) {
		if(path == null || path.isEmpty())
			return path;
		if(path.endsWith("/") || path.endsWith(File.separator))  //$NON-NLS-1$
			return path.substring(0, path.length() - 1);
		return path;
	}

	
	/**
	 * Extracts filename portion out of supplied pathname (leaves last segment only)  
	 * @param path absolute or relative path with forward slashes as delimiters
	 * @return the result filename 
	 */
	static public String extractFileName(String path) {
		if(path == null || path.isEmpty())
			return path;

		int pos = path.lastIndexOf('/');
		if(pos < 0)
			pos = path.lastIndexOf('\\');
		if(pos >= 0 )
			return path.substring(pos + 1);
		return path;
	}

	
	/**
	 * Extracts base filename portion (without extension) out of supplied pathname  
	 * @param path absolute or relative path with forward slashes as delimiters
	 * @return the result filename 
	 */
	static public String extractBaseFileName(String path) {
		if(path == null || path.isEmpty())
			return path;

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
		if(filename == null || filename.isEmpty())
			return filename;

		int pos = filename.lastIndexOf('.');
		if(pos >= 0 ) {
			return filename.substring(pos+1);
		}
		return null;
	}

	
	/**
	 * Extracts path portion out of supplied pathname (removes section out last slash)  
	 * @param path absolute or relative path with forward slashes as delimiters
	 * @return the result path
	 */
	static public String extractPath(String path, boolean keepSlash) {
		if(path == null || path.isEmpty())
			return path;

		int pos = path.lastIndexOf('/');
		if(pos < 0)
			pos = path.lastIndexOf('\\');
		if(pos >= 0 ) {
			if(keepSlash)
				pos++;
			return path.substring(0, pos);
		}
		return path;
	}
	
	/**
	 * Surround supplied path with quotes if needed
	 * @param path path to surround with quotes
	 * @return quoted string
	 */
	static public String addQuotes(String path) {
		if(path == null || path.isEmpty())
			return path;

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

	
}
