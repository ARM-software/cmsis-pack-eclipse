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

package com.arm.cmsis.pack.data;

/**
 *   Compares ICpMemory object by offset, size and name 
 */
public class MemoryOffsetComparator extends MemorySizeComparator {

	@Override
	public int compare(ICpMemory o1, ICpMemory o2) {
		if(o1 == o2)
			return 0;
		int result = compareByOffset(o1, o2);
		if(result != 0)
			return result;
		return super.compare(o1, o2);
	}

	public static int compareByOffset(ICpMemory o1, ICpMemory o2) {
		if(o1 == null && o2 == null )
			return 0;
		if(o1 == null)
			return -1;
		if(o2 == null)
			return 1;
			
		return Long.compareUnsigned(o1.getOffset(), o2.getOffset());
	}
}
