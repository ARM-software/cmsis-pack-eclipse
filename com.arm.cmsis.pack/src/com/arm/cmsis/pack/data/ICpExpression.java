/*******************************************************************************
 * Copyright (c) 2014 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.data;

/**
 * Interface describing a condition expression 
 */
public interface ICpExpression extends ICpItem {

	final static char ACCEPT_EXPRESSION 	= 'a';
	final static char DENY_EXPRESSION 		= 'd';
	final static char REQUIRE_EXPRESSION 	= 'r';

	final static char DEVICE_EXPRESSION 	= 'D';
	final static char TOOLCHAIN_EXPRESSION 	= 'T';
	final static char COMPONENT_EXPRESSION 	= 'C';
	final static char REFERENCE_EXPRESSION 	= 'R';
	final static char EREOR_EXPRESSION 		= 'E';
	
	/**
	 * Returns expression domain: 
	 * <dl>	
	 * <dt>'D' <dd>Device 
	 * <dt>'C' <dd>Component 
     * <dt>'T' <dd>Toolchain 
	 * <dt>'R' <dd>Reference to condition
	 * <dt>'E' <dd>Error (mixed attribute types)
	 * <dt>'U' <dd>Unknown
	 * </dl>
	 * <p>
	 * Note: <code>char</code> type is used instead of an enum for performance reason 
	 * </p>
	 * @return the expression domain
	 */
	public char getExpressionDomain();
	
	
	/**
	 * Returns expression type: 
	 * <dl>	
	 * <dt>'a' <dd>accept 
	 * <dt>'d' <dd>deny 
     * <dt>'r' <dd>require 
	 * </dl>
	 * <p>
	 * Note: <code>char</code> type is used instead of an enum for performance reason 
	 * </p>
	 * @return expression type
	 */
	public char getExpressionType();
	

}