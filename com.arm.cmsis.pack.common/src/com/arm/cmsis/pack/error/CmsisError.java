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

package com.arm.cmsis.pack.error;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.item.ICmsisItem;


/**
 * CMSIS specific errors 
 */
public class CmsisError extends Error implements ICmsisError {

	/**
	 * default serialVersionUID required by Serializable 
	 */
	private static final long serialVersionUID = 1L;
	protected ESeverity fSeverity = ESeverity.None;
	protected String fId = CmsisConstants.EMPTY_STRING;
	protected ICmsisItem fItem = null;  // item associated with the error (not the same as file)
	protected String fDetail = null;

	// associated file, line and column (if available) 
	protected String fFile = null;
	protected int fLine = -1;
	protected int fColumn = -1;
	
	public static final String COLON_SPACE = ": "; //$NON-NLS-1$
	
	/**
	 * Default constructor
	 */
	public CmsisError() {
		super();
	}

	/**
	 * Filed constructor: ID only 
	 */
	public CmsisError(String id) {
		super();
		this.fId = id;
	}
	
	/**
	 * Filed constructor: severity and ID 
	 */
	public CmsisError(ESeverity severity, String id) {
		this(id);
		this.fSeverity = severity;
	}

	/**
	 * Filed constructor: file severity and ID 
	 */
	public CmsisError(String file, ESeverity severity, String id) {
		this(severity, id);
		setFile(file);
	}
	
	/**
	 * Filed constructor: message only
	 */
	public CmsisError(ESeverity severity, String id, String message) {
		super(message);
		this.fSeverity = severity;
		this.fId = id;
	}

	
	/**
	 * Filed constructor: dat, severiry and id 
	 */
	public CmsisError( ICmsisItem item, ESeverity severity, String id) {
		this(severity, id);
		this.fItem = item;
	}


	
	/**
	 * Filed constructor: message and data
	 */
	public CmsisError( ICmsisItem item, ESeverity severity, String id, String message) {
		this(severity, id, message);
		this.fItem = item;
	}

	
	/**
	 * Constructor from Throwable
	 */
	public CmsisError( Throwable cause, ESeverity severity, String id) {
		super(cause);
		fSeverity = severity;
		fId = id;
	}

	/**
	 * Constructor from file and Throwable
	 */
	public CmsisError(String file, Throwable cause, ESeverity severity, String id) {
		this(cause, severity, id);
		setFile(file);
	}
	
	/**
	 * Constructor from Throwable
	 */
	public CmsisError( Throwable cause, ESeverity severity, String id, String message) {
		super(message, cause);
		fSeverity = severity;
		fId = id;
	}

	
	/**
	 * Filed constructor: file 
	 */
	public CmsisError(ESeverity severity, String id, String message, String file, int line, int col) {
		this(severity, id, message);
		this.fFile = file;
		this.fLine = line;
		this.fColumn = col;
	}
	
	// getters
	@Override
	public String getId() {
		return fId == null? CmsisConstants.EMPTY_STRING : fId;
	}

	@Override
	public ESeverity getSeverity() {
		if(fSeverity != null && fSeverity != ESeverity.None)
			return fSeverity;
		return getSeverityFromId();
	}


	/**
	 * Returns Severity based on ID
	 * @return ESeverity
	 */
	protected ESeverity getSeverityFromId() {
		return ESeverity.None; // default returns None 
	}

	@Override
	public String getDetail() {
		return fDetail;
	}

	@Override
	public void setDetail(String detail) {
		fDetail = detail;
	}
	
	
	@Override
	public ICmsisItem getItem() {
		return fItem;
	}

	@Override
	public String getFile() {
		return fFile;
	}

	@Override
	public int getLine() {
		return fLine;
	}
	
	@Override
	public int getColumn() {
		return fColumn;
	}
	

	public static String nullToEmpty(String s) {
		if(s == null)
			return CmsisConstants.EMPTY_STRING;
		return s;
	}

	public void setSeverity(ESeverity severity) {
		if(severity == null)
			fSeverity = ESeverity.None;
		else 
			fSeverity = severity;
	}

	public void setId(String id) {
		fId = nullToEmpty(id);
	}

	public void setItem(ICmsisItem item) {
		fItem = item;
	}


	public void setFile(String file) {
		fFile = file;
	}


	public void setLine(int line) {
		fLine = line;
	}

	public void setColumn(int col) {
		fColumn = col;
	}

	
	
	@Override
	public String getFormattedMessage() {
		String msg = CmsisConstants.EMPTY_STRING;
		ESeverity severity = getSeverity();
		if(severity != ESeverity.None) {
			msg += severity.toString();
		}
		String id = getId();
		
		if(!id.isEmpty()) {
			msg += CmsisConstants.SPACE + id;
		}
		String message = getLocalizedMessage();
		if(message != null) {
			if(!msg.isEmpty()) {
				msg += COLON_SPACE;
			}
			msg +=  message;
		}
		String detail = getDetail();
		if(detail != null) {
			if(!msg.isEmpty()) {
				msg += COLON_SPACE;
			}
			msg +=  detail;
		}
		
		return msg; 
	}
	
	/**
	 * Returns message based on ID
	 * @return message if any or empty string 
	 */
	protected String getMessageFromId() {
		return CmsisConstants.EMPTY_STRING;
	}
	

	@Override
	public String getLocalizedMessage() {
		String msg = CmsisConstants.EMPTY_STRING;
		ICmsisItem item = getItem();
		if(item != null) {
			msg += item.getId(); 	
		}
		
		String idMsg = getMessageFromId();
		if(idMsg != null && !idMsg.isEmpty()) {
			msg += COLON_SPACE + idMsg;
		} 
			
		String superMsg = super.getLocalizedMessage();
		if(superMsg == null && getCause() != null) {
			superMsg = getCause().getLocalizedMessage();
		}
		if(superMsg != null) {
			if(msg.isEmpty())
				return superMsg;
			msg += COLON_SPACE + superMsg;
		}
		
		return msg;
	}

	public String getLineColumnString() {
		String s = CmsisConstants.EMPTY_STRING;
		if(getLine() >=0 ) {
			s += " ("; //$NON-NLS-1$
			s += getLine();
			if(getColumn() >= 0) {
				s += ", "; //$NON-NLS-1$
				s += getColumn();
			}
			s += ")"; //$NON-NLS-1$
		}
		return s;
	}
	
	@Override
	public String toString() {
		String s = CmsisConstants.EMPTY_STRING; 
		String file = getFile();
		if(file != null) {
			s += file + getLineColumnString(); 
			s += COLON_SPACE;
		}
		s += getFormattedMessage();
		return s;
	}	
}
