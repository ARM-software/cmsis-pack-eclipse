/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.parser;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXParseException;

import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.error.CmsisError;

/**
 *  XML parser errors
 */
public class CpXmlParserError extends CmsisError {

	private static final long serialVersionUID = 1L;

	// parser error IDs 'X' is for Xml
	public static final String X201 = "X201"; // Warning //$NON-NLS-1$ 
	
	public static final String X401 = "X401"; // Error //$NON-NLS-1$ 
	public static final String X402 = "X402"; //$NON-NLS-1$
	public static final String X403 = "X403"; //$NON-NLS-1$
	public static final String X404 = "X404"; //$NON-NLS-1$
	
	public static final String X801 = "X801"; // Fatal //$NON-NLS-1$ 
	public static final String X802 = "X802"; // Fatal //$NON-NLS-1$ 
	public static final String X803 = "X803"; // Fatal //$NON-NLS-1$ 
	
	/**
	 * Default constructor
	 */
	public CpXmlParserError() {
		super();
	}

	/**
	 * ParserConfigurationException constructor
	 */
	public CpXmlParserError(ParserConfigurationException e) {
		super(e, ESeverity.FatalError, X802, "Error initializing XML parser"); //$NON-NLS-1$
	}

	
	/**
	 * Constructor out of SAXParseException
	 * @param file XML file being parsed or prefix to show instead of file name 
	 * @param id error ID string
	 * @param severity error severity ESeverity
	 * @param exception SAXException
	 */
	public CpXmlParserError(String file, String id, ESeverity severity, String message, Throwable e) {
		super(e, severity, id, message);
		setFile(file);
		if( e instanceof SAXParseException) {
			SAXParseException saxpe = (SAXParseException)e;
			setLine(saxpe.getLineNumber());
			setColumn(saxpe.getColumnNumber());
		}
		
	}
}
