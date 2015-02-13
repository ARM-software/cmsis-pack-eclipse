/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack.utils;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Class for different utilities  
 */
public class Utils {

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
            	if(!name.startsWith(".") && name.endsWith(".pdsc")){  
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
            if( f.isDirectory() && !f.isHidden() && !f.getName().startsWith(".")) {
            	findPdscFiles(f,  files, depth-1);
            }
        }
       	return  files;
	}
	
	
}
