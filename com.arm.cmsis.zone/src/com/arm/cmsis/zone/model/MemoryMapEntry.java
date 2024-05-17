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

package com.arm.cmsis.zone.model;

import java.util.ArrayList;

import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.zone.data.ICpMemoryBlock;

/**
 *
 */
public class MemoryMapEntry extends CpItem {

    public MemoryMapEntry(ICpItem parent) {
        super(parent);
        // TODO Auto-generated constructor stub
    }

    enum Type {
        UNDEFINED, START, STOP
    }

    protected Long fAddress = null;
    protected Type fType;
    protected ArrayList<ICpMemoryBlock> fBlocks;

    public void addBlock(ICpMemoryBlock block) {

    }

}
