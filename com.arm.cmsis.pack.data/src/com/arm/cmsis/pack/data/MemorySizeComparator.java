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

import java.util.Comparator;

import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 *   Compares ICpMemory object by size and their names if sizes are equal 
 */
public class MemorySizeComparator implements Comparator<ICpMemory> {

	@Override
	public int compare(ICpMemory o1, ICpMemory o2) {
		if(o1 == o2)
			return 0;
		int result = compareBySize(o2, o1); // inverse : we need descending order
		if(result != 0 || o1 == null) // if o1 is null, the o2 is also null here
			return result;
		
		return AlnumComparator.alnumCompare(o1.getId(), o2.getId());
		
	}

	public static int compareBySize(ICpMemory o1, ICpMemory o2) {
		if(o1 == null && o2 == null )
			return 0;
		if(o1 == null)
			return -1;
		if(o2 == null)
			return 1;
			
		return Long.compareUnsigned(o1.getSize(), o2.getSize());
	}
}
