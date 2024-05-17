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

import java.util.Collection;

import com.arm.cmsis.pack.generic.ITreeItem;

/**
 * Base interface for CMSIS homogeneous generic tree items
 */
public interface ICmsisTreeItem<T extends ITreeItem<T> & ICmsisItem> extends ITreeItem<T>, ICmsisItem {
    /**
     * Searches child collection for the first item corresponding to the given tag
     *
     * @param tag item tag to search for
     * @return child item if found, null otherwise
     * @see #getFirstChild(String)
     */
    default T getFirstChildByTag(final String tag) {
        if (tag == null || tag.isEmpty())
            return null;
        Collection<? extends T> children = getChildren();
        if (children == null)
            return null;
        for (T child : children) {
            if (child.getTag().equals(tag))
                return child;
        }
        return null;
    }
}
