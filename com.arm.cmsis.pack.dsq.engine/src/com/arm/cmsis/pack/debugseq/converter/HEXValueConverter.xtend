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

import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.util.Strings

class HEXValueConverter extends AbstractLexerBasedConverter<Long> {
	override protected String toEscapedString(Long value) {
		return '''0x«value.toString()»'''
	}

	override protected void assertValidValue(Long value) {
		super.assertValidValue(value)
		if(value < 0)
			throw new ValueConverterException('''«getRuleName()»-value may not be negative (value: «value»).''',
			null, null);
	}

	override Long toValue(String string, INode node) {
		if(Strings.isEmpty(string)) throw new ValueConverterException("Couldn't convert empty string to an hex value.",
			node, null);
		try {
			var long longValue = Long.parseUnsignedLong(string.substring(2), 16)
			return Long.valueOf(longValue)
		} catch (NumberFormatException e) {
			throw new ValueConverterException('''Couldn't convert '«string»' to an hex value.''', node, e)
		}

	}
}
