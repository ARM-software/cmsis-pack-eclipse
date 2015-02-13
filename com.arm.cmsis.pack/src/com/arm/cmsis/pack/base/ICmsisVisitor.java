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

package com.arm.cmsis.pack.base;


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
