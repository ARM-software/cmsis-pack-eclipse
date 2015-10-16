/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.item;


/**
 * Declares extended visitor design pattern to traverse ICmsisElement tree. Allows to
 * skip visiting some items or cancel visit entirely
 * @see  ICmsisItem
 */
public interface ICmsisVisitor {
	enum VisitResult {
		CONTINUE, SKIP_CHILDREN, SKIP_LEVEL, CANCEL
	}

	/**
	 * @param element to visit
	 * @return VisitResult instructing ICpItem on further processing:
	 * <ul>
	 * <li>CONTINUE  	  continue processing the tree
	 * <li>SKIP_CHILDREN  skip visiting child items of this item
	 * <li>SKIP_LEVEL     skip visiting child and remaining sibling items
	 * <li>CANCEL		  cancel further visits
	 * </ul>
	 */
	VisitResult visit(ICmsisItem item);
}
