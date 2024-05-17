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

package com.arm.cmsis.zone.svd;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.permissions.IMemoryAccess;
import com.arm.cmsis.pack.utils.AlnumComparator;

/*
 * Base SVD item
 */
public class SvdPeripheral extends SvdItem implements ISvdPeripheral {
    public static final String BASE_ADDRESS = "baseAddress"; //$NON-NLS-1$
    public static final String ADDRESS_BLOCK = "addressBlock"; //$NON-NLS-1$
    public static final String GROUP_NAME = "groupName"; //$NON-NLS-1$
    public static final String DERIVED_FROM = "derivedFrom"; //$NON-NLS-1$

    private String fAccess = null;
    private Map<String, ISvdInterrupt> fInterrupts = null;

    public SvdPeripheral(ICpItem parent) {
        super(parent);
    }

    public SvdPeripheral(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public Collection<ISvdInterrupt> getInterrups() {
        if (fInterrupts == null) {
            fInterrupts = new TreeMap<>(new AlnumComparator(false));
            Collection<ISvdInterrupt> interrupts = getChildrenOfType(ISvdInterrupt.class);
            for (ISvdInterrupt i : interrupts) {
                String name = i.getName();
                if (fInterrupts.containsKey(name))
                    continue;
                fInterrupts.put(name, i);
            }
        }
        return fInterrupts.values();
    }

    @Override
    public boolean isPeripheralAccess() {
        return true;
    }

    @Override
    public String getAccessString() {
        if (fAccess == null)
            fAccess = calculateAccess();
        return fAccess;
    }

    private String calculateAccess() {
        String access = IMemoryAccess.DEFAULT_PERIPHERAL_ACCESS;
        // TODO : calculate from the registers ?
        return access;
    }

    ISvdPeripheral getBase() {
        String derivedFrom = getAttribute(DERIVED_FROM);
        if (!derivedFrom.isEmpty()) {
            return getSvdRoot().getPeripheral(derivedFrom);
        }
        return null;
    }

    @Override
    public String getGroupName() {
        ICpItem nameChild = getFirstChild(GROUP_NAME);
        if (nameChild != null)
            return nameChild.getText();

        ISvdPeripheral base = getBase();
        if (base != null)
            return base.getGroupName();

        // extract from name
        String name = getName();
        int splitIndex;
        for (splitIndex = name.length() - 1; splitIndex > 0; splitIndex--) {
            char ch = name.charAt(splitIndex);
            if (ch == '_') {
                splitIndex--;
                break;
            }
            if (!Character.isDigit(ch)) {
                splitIndex++;
                break;
            }
        }

        if (splitIndex > 0 && splitIndex < name.length()) {
            return name.substring(0, splitIndex);
        }

        return CmsisConstants.EMPTY_STRING;
    }

    @Override
    public String getStartString() {
        ICpItem baseAddress = getFirstChild(BASE_ADDRESS);
        if (baseAddress != null)
            return baseAddress.getText();
        ISvdPeripheral base = getBase();
        if (base != null)
            return base.getStartString();
        return CmsisConstants.EMPTY_STRING;
    }

    @Override
    public String getOffsetString() {
        ICpItem addressBlock = getAddressBlock();
        if (addressBlock != null) {
            ICpItem offset = addressBlock.getFirstChild(CmsisConstants.OFFSET);
            if (offset != null)
                return offset.getText();
        }

        ISvdPeripheral base = getBase();
        if (base != null)
            return base.getOffsetString();

        return CmsisConstants.EMPTY_STRING;
    }

    @Override
    public String getSizeString() {
        ICpItem addressBlock = getAddressBlock();
        if (addressBlock != null) {
            ICpItem size = addressBlock.getFirstChild(CmsisConstants.SIZE);
            if (size != null)
                return size.getText();
        }
        ISvdPeripheral base = getBase();
        if (base != null)
            return base.getSizeString();

        return CmsisConstants.EMPTY_STRING;
    }

    protected ICpItem getAddressBlock() {
        return getFirstChild(ADDRESS_BLOCK);
    }

}
