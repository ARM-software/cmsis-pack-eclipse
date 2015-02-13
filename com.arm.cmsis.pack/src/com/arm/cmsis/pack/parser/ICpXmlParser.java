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

package com.arm.cmsis.pack.parser;

import java.util.List;
import java.util.Set;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpItemFactory;

/**
 * Interface to CMSIS pack description file (*.pdsc) parser
 */
public interface ICpXmlParser extends ICpItemFactory {
	/** 
	 * Initializes the parser 
	 * @return true if successful  
	 */
	boolean init();
	
	/**
	 * Clears internal data, error strings and resets builder 
	 */
	void clear();

	/**
	 * Sets schema file to use by the parser
	 * @param xsdFile schema file name to use with absolute path   
	 */
	void setXsdFile(String xsdFile);

	/**
	 * Returns schema file used by parser 
	 * @return absolute schema file name or null if not set 
	 */
	public String getXsdFile();
	
	/**
	 * @return the errorStrings
	 */
	List<String> getErrorStrings();

	/**
	 * @return number of errors 
	 */
	int getErrorCount();

	/**
	 * @return number of warnings
	 */
	int getWarningCount();

	/**
	 * Sets XML tags to ignore during parsing 
	 * @param ignoreTags set of tags to ignore
	 */
	void setIgnoreTags(Set<String> ignoreTags);

	/**
	 * Adjusts deprecated attribute values to modern ones  
	 * @param key attribute key
	 * @param value attribute value
	 * @return adjusted attribute value 
	 */
	String adjustAttributeValue(String key, String value);
	
	/**
	 * Parses supplied XML file 
	 * @param file XML file to parse 
	 * @return root ICpItem object
	 */
	ICpItem parseFile(String file);

	/**
	 * Parses supplied string in XML format
	 * @param xml XML string to parse 
	 * @return root ICpItem object
	 */
	ICpItem parseXmlString(String xml);
	
	
	/**
	 * Generates XML text out of ICpItem and saves it to an XML file  
	 * @param root ICpItem to save  
	 * @param file file to contain the generated XML   
	 * @return true if successful
	 */
	boolean writeToXmlFile(ICpItem root, String file);

	
	/**
	 * Generates XML text out of ICpItem and returns it as a string  
	 * @param root ICpItem to write  
	 * @return generate XML text if successful, null otherwise
	 */
	String writeToXmlString(ICpItem root);
	
	
}
