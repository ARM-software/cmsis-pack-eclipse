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


/**
 * Utility class provides method to compare two string that can contain wild cards
 * 
 * Supported wild cards expressions:
 * <dl>  
 * <dt>*<dd>any substring
 * <dt>?<dd>any single character
 * <dt>[abc]<dd>any character in a set
 * </dl>  
 * 
 * Both stings can be wild card patterns, for example "a*d" and "a*"  
 * 
 * The main purpose of this class is to support condition evaluation in CMSIS packs.
 * It is optimized for small strings, primary for device names like <b>"STM32F4[23]9??</b>".  
 * <p/>
 * The method has some limitations:
 * <ul>
 * <li>there is no escape for *, ? [ and ] characters
 * <li>"*?" is equivalent to "*" =>  <code>"a*?d"</code> and <code>"a*d"</code> are equivalent
 * <li>* match is performed until first matching on character followed after * is found, therefore 
 *   pattern <code>"a*d"</code> will match <code>"a.d"</code> or <code>"a.c.d"</code>, but not <code>"a.d.d"</code>"    
 * </ul> 
 */
public class WildCards {
	
	/**
	 * Match two strings containing wild cards (case sensitive)  
	 * @param str1 first string argument
	 * @param str2 second string argument
	 * @return <b>true</b> if strings match, <b>false</b> otherwise 
	 */
	public static boolean match(final String str1, final String str2) {
		return match(str1, str2, true);
	}
		
	/**
	 * Match two strings containing wild cards ignoring case  
	 * @param str1 first string argument
	 * @param str2 second string argument
	 * @return <b>true</b> if strings match, <b>false</b> otherwise 
	 */
	public static boolean matchNoCase(final String str1, final String str2) {
		return match(str1, str2, false);
	}
	
	/**
	 * Match two strings containing wild cards
	 * @param str1 first string argument
	 * @param str2 second string argument
	 * @param cs case sensitive flag (true: respect case, false: ignore case)
	 * @return <b>true</b> if strings match, <b>false</b> otherwise 
	 */
	public static boolean match(final String str1, final String str2, boolean cs) {
		// check for empty and null strings
		if (str1 == null || str1.isEmpty()) {
			if (str2 == null || str2.isEmpty())
				return true;
			else
				return false;
		} else if (str2 == null || str2.isEmpty()) {
			return false;
		}

		WildcardState ws1 = new WildcardState(str1, cs);
		if (ws1.isAsterisk() && ws1.isEnd())
			return true;

		WildcardState ws2 = new WildcardState(str2, cs);
		if (ws2.isAsterisk() && ws2.isEnd())
			return true;

		boolean result = wildCardMatch(ws1, ws2);

		// we need a symmetric comparison in case both strings contain '*' : 
		// a*d and a*cd should be treated as equal
		if (!result & ws1.containsAsterisk() && ws2.containsAsterisk()) {
			ws1.init();
			ws2.init();
			if (wildCardMatch(ws2, ws1))
				return true;
		}
		return result;
	}

	private static boolean wildCardMatch(WildcardState ws1, WildcardState ws2) {
		while (true) {
			if (ws1.isAsterisk()) {
				if (ws1.isEnd())
					return true; // end of str2 is irrelevant
				ws2.skip(ws1);
				if (ws2.isEnd()) {
					return ws2.isAsterisk() || ws2.isQuestion();
				}
			}

			if (ws2.isAsterisk()) {
				if (ws2.isEnd())
					return true; // end of str1 is irrelevant
				ws1.skip(ws2);
				if (ws1.isEnd()) {
					return ws1.isAsterisk() || ws1.isQuestion();
				}
			}

			if (ws1.isEnd() || ws2.isEnd())
				break;

			if (!ws1.compare(ws2)) {
				return false;
			}
			ws1.next();
			ws2.next();
		}
		return ws1.isEnd() && ws2.isEnd();
	}

	static private class WildcardState {
		private String s;
		private boolean cs = true;
		private int index = 0;
		private int rangeFrom = -1;
		private int rangeTo = -1;
		private boolean asterisk = false;
		private boolean containsAsterisk = false;

		public WildcardState(String s, boolean cs ) {
			this.s = s;
			this.cs = cs;
			init();
		}

		void init() {
			asterisk = false;
			containsAsterisk = false;
			index = 0;
			createRange();
		}

		public boolean containsAsterisk() {
			return containsAsterisk;
		}

		boolean isEnd() {
			return index >= s.length();
		}

		boolean isAsterisk() {
			return asterisk;
		}

		boolean isQuestion() {
			return rangeFrom >=0 && s.charAt(rangeFrom) == '?';
		}

		void next() {
			if (isEnd())
				return;
			index++;
			createRange();
		}

		void skip(WildcardState ws) {
			while (!isEnd() && !compare(ws)) {
				next();
			}
		}

		boolean compare(WildcardState ws) {
			if (isQuestion() || ws.isQuestion())
				return true;
			if(rangeFrom < 0 && ws.rangeFrom < 0)
				return true;

			for(int i = rangeFrom; i < rangeTo; i++) {
				char ch = s.charAt(i);
				if(!cs)
					ch = Character.toUpperCase(ch);
				for(int j = ws.rangeFrom; j < ws.rangeTo; j++) {
					char otherCh = ws.s.charAt(j);
					if(!cs)
						otherCh = Character.toUpperCase(otherCh);
					if(ch == otherCh)
						return true;
				}
			}
			return false;
		}

		void createRange() {
			rangeFrom = rangeTo = -1;
			asterisk = false;
			if (isEnd())
				return;
			char ch = s.charAt(index);
			if (ch == '*') {
				containsAsterisk = asterisk = true;
				// skip all asterisks and questions
				index++;
				while (!isEnd()) {
					ch = s.charAt(index);
					if(ch != '*' && ch != '?')
						break;
				}
			}
			if (isEnd())
				return;
			if (ch == '[') {
				index++;
				if (isEnd())
					return;
				rangeTo = rangeFrom = index;
				while (!isEnd()) {
					ch = s.charAt(index);
					if (ch == ']')
						break;
					index++;
					rangeTo = index;
				}
			} else {
				rangeTo = rangeFrom = index;
				rangeTo++;
			}
		}

	}
}
