/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.item;

import com.arm.cmsis.pack.generic.ITreeItem;
import com.arm.cmsis.pack.generic.ITreeMapItem;

/**
 * Basic generic interface for CMSIS items with String-to-Item map collections
 */
public interface ICmsisMapItem<T extends ITreeItem<T> & ICmsisItem> extends ICmsisTreeItem<T>, ITreeMapItem<String, T> {
    // Convenience synthetic interface without own methods
}
