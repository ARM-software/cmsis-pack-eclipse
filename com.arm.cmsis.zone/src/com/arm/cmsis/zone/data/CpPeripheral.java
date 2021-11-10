/*******************************************************************************
* Copyright (c) 2021 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.data;

import java.util.Collection;
import java.util.LinkedList;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Implementation for a Peripheral tag
 */
public class CpPeripheral extends CpPeripheralItem implements ICpPeripheral {

    public CpPeripheral(ICpItem parent, ICpMemory memory) {
        super(parent, CmsisConstants.PERIPHERAL);
        setMemory(memory);
    }

    public CpPeripheral(ICpItem parent, String tag) {
        super(parent, tag);
    }

    public CpPeripheral(ICpPeripheral realPeripheral) {
        super(realPeripheral);
        ICpResourceGroup group = realPeripheral.getParentGroup();
        if (group != null && group.getTag().equals(CmsisConstants.GROUP)) {
            setAttribute(CmsisConstants.GROUP, group.getName());
        }
    }

    @Override
    public Collection<ICpPeripheralSetup> getPeripheralSetups() {
        return getAllChildrenOfType(null, ICpPeripheralSetup.class);
    }

    @Override
    public Collection<ICpMemoryBlock> getSubBlocks() {
        return null;
    }

    @Override
    public boolean hasSubBlocks() {
        return false;
    }

    @Override
    public String getPeripheralName() {
        return getName();
    }

    @Override
    public String getGroupName() {
        if (hasAttribute(CmsisConstants.GROUP))
            return getAttribute(CmsisConstants.GROUP);
        ICpPeripheralGroup group = getParentPeripheralGroup();
        if (group != null) {
            setAttribute(CmsisConstants.GROUP, group.getName());
            return group.getName();
        }
        return null;
    }

    @Override
    public long getStartAddress(boolean bPhysical) {
        String s = CmsisConstants.EMPTY_STRING;
        if (bPhysical) {
            if (hasAttribute(CmsisConstants.PHYSICAL)) {
                s = getAttribute(CmsisConstants.PHYSICAL);
            } else {
                s = getAttribute(CmsisConstants.START);
            }
        } else if (isSecureAccess() && hasAttribute(CmsisConstants.START_S)) {
            s = getAttribute(CmsisConstants.START_S);
        } else {
            s = getAttribute(CmsisConstants.START);
        }
        return IAttributes.stringToLong(s, 0L);
    }

    @Override
    public ICpItem toFtlModel(ICpItem ftlParent) {
        ICpItem ftlItem = super.toFtlModel(ftlParent);
        // Collect interrupts
        Collection<ICpInterrupt> itrps = getInterrupts();
        if (itrps != null && !itrps.isEmpty()) {
            for (ICpInterrupt itrp : itrps) {
                // Add interrupt to peripheral
                ICpItem i = itrp.toFtlModel(ftlItem);
                ftlItem.addChild(i);
            }
        }
        return ftlItem;
    }

    @Override
    protected IAttributes getAttributesForFtlModel() {
        getGroupName(); // ensure group attribute
        IAttributes a = super.getAttributesForFtlModel();
        a.setAttributeHex(CmsisConstants.START, getStart());
        a.removeAttribute(CmsisConstants.START_S);
        a.removeAttribute(CmsisConstants.STARTUP);
        return a;
    }

    @Override
    public Collection<ICpSlot> getModifiedSlots() {
        Collection<ICpSlot> modifiedSlots = new LinkedList<>();
        Collection<ICpSlot> slots = getSlots();
        if (slots != null && !slots.isEmpty()) {
            for (ICpSlot s : slots) {
                if (s.isModified()) {
                    modifiedSlots.add(s);
                }
            }
        }
        return modifiedSlots;
    }

    @Override
    public boolean isModified() {
        return areAttributesModified() || !getModifiedSlots().isEmpty();
    }

    @Override
    public ICpMemoryBlock copyBlockTo(ICpItem parent) {
        ICpMemoryBlock copy = super.copyBlockTo(parent);
        Collection<ICpSlot> modifiedSlots = getModifiedSlots();
        for (ICpSlot s : modifiedSlots) {
            s.copyTo(copy, false);
        }
        return copy;
    }

}
