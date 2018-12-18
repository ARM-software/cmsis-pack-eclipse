/*******************************************************************************
* Copyright (c) 2017 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.build;

public interface IMemoryAccess {
	
	final static char PERIPHERAL_ACCESS		= 'p';
	final static char READ_ACCESS 			= 'r';
	final static char WRITE_ACCESS			= 'w';
	final static char EXECUTE_ACCESS		= 'x';
	final static char SECURE_ACCESS			= 's';
	final static char NON_SECURE_ACCESS		= 'n';
	final static char CALLABLE_ACCESS		= 'c';
	final static char UNPRIVILEGED_ACCESS	= 'u';
	
	final static String DEFAULT_ACCESS = "rwx";  //$NON-NLS-1$
	final static String DEFAULT_RO_ACCESS = "rx";  //$NON-NLS-1$
	final static String DEFAULT_PERIPHERAL_ACCESS = "prw";  //$NON-NLS-1$
	final static String ACCESS_ORDER = "prwxsncu";  //$NON-NLS-1$
	/**
	 * Returns access string
	 * @return access permissions as string 
	 */
	String getAccessString();

	
	/**
	 * Merge the access permissions of this and another specification to a string.
	 * 
	 * <p>Access permissions are merged as follows:
	 * <ul>
	 * <li>peripheral access is or'ed.
	 * <li>read access is and'ed.
	 * <li>write access is and'ed.
	 * <li>execute access is and'ed.
	 * <li>secure access is or'ed.
	 * <li>non secure access is and'ed.
	 * <li>callable access is or'ed but overruled by secure access.
	 * <li>unprivileged access is or'ed.
	 * </ul>
	 * </p>
	 * 
	 * @param access Another access specification to be merged.
	 * @return merged access specification as a String
	 */
	default String mergeToString(IMemoryAccess other) {
		StringBuilder builder = new StringBuilder();
		
		if (isPeripheralAccess() 	|| other.isPeripheralAccess())	{ builder.append(PERIPHERAL_ACCESS);}
		if (isReadAccess() 			&& other.isReadAccess()) 		{ builder.append(READ_ACCESS);}
		if (isWriteAccess() 		&& other.isWriteAccess()) 		{ builder.append(WRITE_ACCESS);}
		if (isExecuteAccess() 		&& other.isExecuteAccess())		{ builder.append(EXECUTE_ACCESS);}
		if (isSecureAccess() 		|| other.isSecureAccess()) 		{ 
			builder.append(SECURE_ACCESS); 
		} else if (isCallableAccess() 	|| other.isCallableAccess()) { 
			builder.append(CALLABLE_ACCESS);
		} else if (isNonSecureAccess()	|| other.isNonSecureAccess()) { 
			builder.append(NON_SECURE_ACCESS);
		}

		if (isUnprivilegedAccess()	|| other.isUnprivilegedAccess()){ builder.append(UNPRIVILEGED_ACCESS); }
		return builder.toString();
	}
	
	/**
	 * Merge the access permissions of this and another specification.
	 * @param access Another access specification to be merged.
	 * @return The merged access specification.
	 */
	default IMemoryAccess mergeAccess(String other) {
		return mergeAccess(fromString(other));
	}
	
	/**
	 * Merge the access permissions of this and another specification.
	 * @param access Another access specification to be merged.
	 * @return The merged access specification.
	 */
	default IMemoryAccess mergeAccess(IMemoryAccess other) {
		if(other == this)
			return this; // nothing to merge
		if(other == null || other.getAccessString() == null)
			return this; // other has no access description = > inherit this 
		return fromString(mergeToString(other));
	}

	/**
	 * Creates a simple memory access object from supplied string. 
	 * @param access permissions string
	 * @return IMemoryAccess object
	 */
	static IMemoryAccess fromString(String access) {
		return new MemoryAccess(access);
	}
	
	
	/**
	 * Parse the given access string and update cache fields.
	 * @param access The access string to parse.
	 */
	default boolean isAccessEqual(String access) {
		String normalized = normalize(access);
		return normalized.equals(getAccessString());
	}
	
	/**
	 * Parse the given access string and update cache fields.
	 * @param access The access string to parse.
	 */
	default boolean isAccessEqual(IMemoryAccess access) {
		if(access == null)
			return false;
		return getAccessString().equals(access.getAccessString());
	}
	
	/**
	 * Normalizes the given access string.
	 * 
	 * <p>Normalization rules are
	 * <ul>
	 * <li>Each access char listed only once.
	 * <li>Access chars listed in default ordering.
	 * </ul>
	 * </p>
	 * 
	 * @param access The access string to be normalized.
	 * @return The normalized access string.
	 */
	 static String normalize(String access) {
		if(access == null || access.isEmpty())
			return access;
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < ACCESS_ORDER.length(); i++) {
			char ch = ACCESS_ORDER.charAt(i);
			if(access.indexOf(ch) >= 0) {
				switch(ch) {
				case NON_SECURE_ACCESS:
					if(isAccessSet(SECURE_ACCESS, access))
						continue;
					if(isAccessSet(CALLABLE_ACCESS, access))
						continue;
				case CALLABLE_ACCESS:
					if(isAccessSet(SECURE_ACCESS, access))
						continue;
				}
				builder.append(ch);
			}
		}
		return builder.toString();
	}
	
	/**
	 * Creates an mask string giving all permissions that are changeable.
	 * 
	 * <p>
	 * Peripheral access is never changeable.<br>
	 * Read/write/execute permissions can be restricted but not granted.<br>
	 * Security permissions can only be inherited but not modified.<br>
	 * Unprivileged access can only be granted but not declined.<br>
	 * </p>
	 * 
	 * <h4>Examples:</h4>
	 * <p>
	 * "rwx" yields "rwxsncu", i.e.
	 * <ul>
	 * <li>rwx can be removed,
	 * <li>security cannot be changed, and 
	 * <li>unprivileged access can permitted.
	 * </ul>
	 * </p>
	 * <p>
	 * "rxsu" yields "rxu", i.e.
	 * <ul>
	 * <li>rx can be removed,
	 * <li>security cannot be changed, and
	 * <li>unprivileged access cannot be restricted.
	 * </ul>
	 * </p>
	 * @return String masking changeable access permissions. 
	 */
	 default String getAccessMask() {
		StringBuilder builder = new StringBuilder();
		
		if (isReadAccess())         { builder.append(READ_ACCESS); }
		if (isWriteAccess())        { builder.append(WRITE_ACCESS); }
		if (isExecuteAccess())      { builder.append(EXECUTE_ACCESS); }
		
		if (!isSecureAccessDefined()) {
			builder.append(SECURE_ACCESS);
			builder.append(CALLABLE_ACCESS);
			builder.append(NON_SECURE_ACCESS);
		}
		
		if (!isUnprivilegedAccess()) { builder.append(UNPRIVILEGED_ACCESS); }
		
		return builder.toString();
	}
	

	 /**
	  * Checks if memory has specified access explicitly set
	  * @param access : one of <code>rwxpsnc</code> characters
	  * @return true if memory provides specified access
	  */
	 static boolean isAccessSet(char access, String accessString) {
		 if(accessString == null)
			 return isAccessSet(access, DEFAULT_ACCESS);
		 return accessString.indexOf(access) >= 0;
	 }

	 /**
	 * Checks if memory has specified access explicitly set
	 * @param access : one of <code>rwxpsnc</code> characters
	 * @return true if memory provides specified access
	 */
	default boolean isAccessSet(char access) {
		return isAccessSet(access, getAccessString());
	}
	
	
	/**
	 * Checks if memory has peripheral access
	 * @return true if memory has peripheral access
	 */
	default boolean isPeripheralAccess() {
		return isAccessSet(PERIPHERAL_ACCESS);
	}
	
	/**
	 * Checks if memory has read access
	 * @return true if memory has read access
	 */
	default boolean isReadAccess() {
		return isAccessSet(READ_ACCESS);
	}
	
	/**
	 * Checks if memory has write access
	 * @return true if memory has write access
	 */
	default boolean isWriteAccess() {
		return isAccessSet(WRITE_ACCESS);
	}

	/**
	 * Checks if memory has execute access
	 * @return true if memory has execute access
	 */
	default boolean isExecuteAccess() {
		return isAccessSet(EXECUTE_ACCESS);
	}
	
	/**
	 * Checks if memory has secure access
	 * @return true if memory has secure access
	 */
	default boolean isSecureAccess() {
		return isAccessSet(SECURE_ACCESS);
	}

	/**
	 * Checks if memory access is explicitly defined via one of secure attributes
	 * @return true if memory has secure access is explicitly defined
	 */
	default boolean isSecureAccessDefined() {
		return isAccessSet(SECURE_ACCESS) || isAccessSet(CALLABLE_ACCESS) || isAccessSet(NON_SECURE_ACCESS);
	}

	
	/**
	 * Checks if memory has non-secure access
	 * @return true if memory has non-secure access
	 */
	default boolean isNonSecureAccess() {
		return isAccessSet(NON_SECURE_ACCESS) && !isAccessSet(CALLABLE_ACCESS) && !isAccessSet(SECURE_ACCESS);
	}
	
	/**
	 * Checks if memory has non-secure callable access
	 * @return true if memory has non-secure callable access
	 */
	default boolean isCallableAccess() {
		return isAccessSet(CALLABLE_ACCESS) && !isAccessSet(SECURE_ACCESS);
	}

	/**
	 * Checks if memory has unprivileged access
	 * @return true if memory has unprivileged access
	 */
	default boolean isUnprivilegedAccess() {
		return isAccessSet(UNPRIVILEGED_ACCESS);
	}
}
