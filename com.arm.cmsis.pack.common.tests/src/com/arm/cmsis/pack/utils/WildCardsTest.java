/*******************************************************************************
 * Copyright (c) 2020 ARM Ltd and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.pack.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


public class WildCardsTest {

	private String[] baseStrings = new String[]{"a*d", "STM32F10[123]?[CDE]","*","a*","a*d","*d","abcd","abcd","abcd","ab?d","abc?","abc[XY]-[12]","abc[XY]-[12]","abc[XY]-[12]"};
	private String[] matchedStrings = new String[]{"a.d", "STM32F103ZE", "x","ab","axd","xd","abcd","abcd","?bcd","ab??","abcx","abcX-1","abcY-1","abcY-1"};
	private String[] noMatchedStrings = new String[]{"a.d.d", "STM32F103ZF","", "bb","axe","xf","abxx","abxyz","xycd","?bce","abc??","abcX-13","abcY-13","abcZ-1"};		
	private String[] matchedStringsNoCase = new String[]{"a.D", "STM32F103Ze", "X","Ab","Axd","Xd","Abcd","Abcd","?Bcd","Ab??","Abcx","AbcX-1","AbcY-1","AbcY-1"};
	private String[] noMatchedStringsNoCase = new String[]{"a.D.d", "STM32F103Zf","", "Bb","Axe","Xf","Abxx","Abxyz","Xycd","?Bce","Abc??","AbcX-13","AbcY-13","AbcZ-1"};
	
	@Test
	public void testMatch() {			
		boolean result = false;
		String msg = null;
		for (int i = 0; i < baseStrings.length; i++) {
			result = WildCards.match(baseStrings[i], matchedStrings[i]);
			msg = result ? "Strings match" : "Strings do not match";			
			assertTrue(msg, result);
			}
	}
	
	@Test
	public void testNoMatch() {			
		boolean result = false;
		String msg = null;
		for (int i = 0; i < baseStrings.length; i++) {
			result = WildCards.match(baseStrings[i], noMatchedStrings[i]);
			msg = result ? "Strings match" : "Strings do not match";		
			assertFalse(msg, result);			
		}
	}
		
	@Test
	public void testMatchNoCase() {
		boolean result = false;
		String msg = null;
		for (int i = 0; i < baseStrings.length; i++) {
			result = WildCards.matchNoCase(baseStrings[i], matchedStringsNoCase[i]);
			msg = result ? "Strings match (no case)" : "Strings do not match";		
			assertTrue(msg, result);			
		}
	}

	@Test
	public void testNoMatchNoCase() {
		boolean result = false;
		String msg = null;
		for (int i = 0; i < baseStrings.length; i++) {
			result = WildCards.matchNoCase(baseStrings[i], noMatchedStringsNoCase[i]);
			msg = result ? "Strings match (no case)" : "Strings do not match";		
			assertFalse(msg, result);			
		}
	}

}


