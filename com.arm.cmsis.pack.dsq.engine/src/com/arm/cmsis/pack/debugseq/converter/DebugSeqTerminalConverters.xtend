/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.debugseq.converter

import org.eclipse.xtext.common.services.DefaultTerminalConverters
import org.eclipse.xtext.conversion.ValueConverter
import com.google.inject.Inject
import org.eclipse.xtext.conversion.IValueConverter

class DebugSeqTerminalConverters extends DefaultTerminalConverters {
	/*------------------ HEX ------------------*/
	@Inject HEXValueConverter hexValueConverter

	@ValueConverter(rule="HEX")
	def IValueConverter<Long> HEX() {
		return hexValueConverter
	}

	@Inject HEXValueConverter terminalsHexValueConverter

	/** 
	 * @since 2.9
	 */
	@ValueConverter(rule="org.example.expressions.Expressions.HEX")
	def IValueConverter<Long> TerminalsHEX() {
		return terminalsHexValueConverter
	}
	
	/*------------------ DEC ------------------*/
	@Inject DECValueConverter decValueConverter

	@ValueConverter(rule="DEC")
	def IValueConverter<Long> DEC() {
		return decValueConverter
	}

	@Inject DECValueConverter terminalsDecValueConverter

	/** 
	 * @since 2.9
	 */
	@ValueConverter(rule="org.example.expressions.Expressions.DEC")
	def IValueConverter<Long> TerminalsDEC() {
		return terminalsDecValueConverter
	}
}