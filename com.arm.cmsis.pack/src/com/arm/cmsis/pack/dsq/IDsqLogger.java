/**
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 */
package com.arm.cmsis.pack.dsq;

public interface IDsqLogger {

	/**
	 * Enable/Disable the logger
	 * @param enable
	 */
	void setEnabled(boolean enable);

	/**
	 * Return true if logger is enabled
	 * @return True if logger is enabled
	 */
	boolean isEnabled();

	/**
	 * Log the start of sequence
	 * @param seqName
	 */
	void logSeqStart(final String seqName);

	/**
	 * Log the end of sequence
	 * @param seqName
	 */
	void logSeqEnd(final String seqName);

	/**
	 * Log the start of block
	 * @param blockInfo
	 */
	void logBlockStart(boolean isAtomic, final String blockInfo);

	/**
	 * Log the end of block
	 */
	void logBlockEnd();

	/**
	 * Log the start of control
	 * @param controlInfo
	 */
	void logContorlStart(final String controlInfo);

	/**
	 * Log the end of control
	 */
	void logControlEnd();

	/**
	 * Log a statement
	 * @param stmt String of statement
	 * @param result Execution result of the statement
	 * @param errorCode Error code
	 */
	void logStatement(final String stmt, final Long result, int errorCode);

	/**
	 * Log an if condition statement
	 * @param stmt String of statement
	 * @param result Execution result of the statement
	 * @param errorCode Error code
	 */
	void logIfStatement(final String stmt, final Long result, int errorCode);

	/**
	 * Log a while condition statement
	 * @param stmt String of statement
	 * @param result Execution result of the statement
	 * @param errorCode Error code
	 */
	void logWhileStatement(final String stmt, final Long result, int errorCode);
}
