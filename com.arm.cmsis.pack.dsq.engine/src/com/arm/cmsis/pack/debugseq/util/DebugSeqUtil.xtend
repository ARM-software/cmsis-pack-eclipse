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

package com.arm.cmsis.pack.debugseq.util

import org.eclipse.emf.ecore.EObject

import static extension org.eclipse.xtext.EcoreUtil2.*
import com.arm.cmsis.pack.debugseq.debugSeq.Sequences
import com.arm.cmsis.pack.debugseq.debugSeq.DebugSeqModel
import com.arm.cmsis.pack.debugseq.debugSeq.Sequence
import com.arm.cmsis.pack.debugseq.debugSeq.Control
import com.arm.cmsis.pack.debugseq.debugSeq.Block
import java.util.List
import java.util.regex.Pattern
import java.math.BigInteger
import java.util.IllegalFormatException
import com.arm.cmsis.pack.dsq.IDsqContext

class DebugSeqUtil {
	
	def static containingDebugSeqModel(EObject e) {
		e.getContainerOfType(typeof(DebugSeqModel))
	}
	
	def static containingSequences(EObject e) {
		e.getContainerOfType(typeof(Sequences))
	}
	
	def static containingSequence(EObject e) {
		e.getContainerOfType(typeof(Sequence))
	}
	
	def static containingControl(EObject e) {
		e.getContainerOfType(typeof(Control))
	}
	
	def static containingBlock(EObject e) {
		e.getContainerOfType(typeof(Block))
	}
	
	def static boolean isEmptyDefaultSequence(String seqName) {
		return seqName == "DebugDeviceUnlock" || seqName == "TraceStart"
			|| seqName == "DebugCodeMemRemap" || seqName == "TraceStop"
			|| seqName == "FlashEraseDone" || seqName == "FlashProgramDone"
			|| seqName == "RecoverySupportStart" || seqName == "RecoverySupportStop"
			|| seqName == "RecoveryAcknowledge"
	}
	
	def static boolean isDefaultSequence(String seqName) {
		return seqName == "DebugPortSetup" ||
			seqName == "DebugPortStart" ||
			seqName == "DebugPortStop" ||
			seqName == "DebugDeviceUnlock" ||
			seqName == "DebugCoreStart" ||
			seqName == "DebugCoreStop" ||
			seqName == "DebugCodeMemRemap" ||
			seqName == "ResetSystem" ||
			seqName == "ResetProcessor" ||
			seqName == "ResetHardware" ||
			seqName == "ResetHardwareAssert" ||
			seqName == "ResetHardwareDeassert" ||
			seqName == "ResetCatchSet" ||
			seqName == "ResetCatchClear" ||
			seqName == "FlashEraseDone" ||
			seqName == "FlashProgramDone" ||
			seqName == "TraceStart" ||
			seqName == "TraceStop" ||
			seqName == "RecoverySupportStart" ||
			seqName == "RecoverySupportStop" ||
			seqName == "RecoveryAcknowledge"
	}
	
	def static boolean isPredefinedVariable(String varName) {
		return varName == IDsqContext::AP ||
			varName == IDsqContext::DP ||
			varName == IDsqContext::PROTOCOL ||
			varName == IDsqContext::CONNECTION ||
			varName == IDsqContext::TRACEOUT ||
			varName == IDsqContext::ERRORCONTROL
	}
	
	def static long toLong(Object o) {
		if (o instanceof Boolean) {
			if (o) 1L
			else 0L
		}
		else if (o instanceof Integer) {
			o.intValue
		}
		else if (o instanceof Long) {
			o.longValue
		}
		else if (o instanceof String) {
			try {
				Long.valueOf(o)
			} catch (Exception e) {
				0L
			}
		}
		else 0L
	}
	
	def static int toInteger(Long l) {
		Integer::parseInt(l.toString)
	}
	
	/**
	 * Format the string with parameters
	 * @param string string to format
	 * @param values values for specifiers
	 * @return formatted string
	 */
	def static String formatWithValues(String string, List<Object> values) throws IllegalFormatException {
		if (string === null || string.empty) {
			return string
		} else if (values === null || values.empty) {
			return String::format(string)
		}
		val pattern = Pattern.compile("%0?(\\d+)?(\\.\\d+)?((ll)?[uxXob]|L?f|s)")
		val matcher = pattern.matcher(string)
		var result = new StringBuilder(string)
		val removeIndices = newLinkedList // index to remove from the string
		var idx = 0 // nth format specifier
		val parms = newLinkedList
		for (Object value : values) {
			parms.add(value)
		}
		while (matcher.find()) {
			val lastIdx = matcher.end()-1
			switch (string.charAt(lastIdx)) {
				case 'u'.charAt(0): { // change specifier to 'd' to fit Java Formatter
					result.setCharAt(lastIdx, 'd')
				}
				case 'b'.charAt(0): { // change specifier to 'd' and value to a BigInteger to show the binary value
					result.setCharAt(lastIdx, 'd')
					val v = parms.get(idx)
					parms.set(idx, new BigInteger(Long.toBinaryString(v.toLong)))
				}
				case 'f'.charAt(0): { // change value to float/double representation
					val v = parms.get(idx)
					if (string.charAt(lastIdx-1) === 'L'.charAt(0)) {
						removeIndices.add(lastIdx-1)
						parms.set(idx, Double.longBitsToDouble(v.toLong))
					} else {
						parms.set(idx, Float.intBitsToFloat(v.toLong.toInteger))
					}
				}
			}
			if (string.charAt(lastIdx-1) === 'l'.charAt(0)) {
				removeIndices.add(lastIdx-2)
				removeIndices.add(lastIdx-1)
			}
			idx++
		}
		var modifier = 0
		for (int removeIdx : removeIndices) {
			result.deleteCharAt(removeIdx-modifier)
			modifier++
		}
		String::format(result.toString, parms.toArray)
	}
}