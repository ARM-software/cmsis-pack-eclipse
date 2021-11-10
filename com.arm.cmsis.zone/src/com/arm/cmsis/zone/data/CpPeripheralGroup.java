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

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EMemoryPrivilege;
import com.arm.cmsis.pack.enums.EMemorySecurity;

/**
 * Implementation for a Peripheral group
 */
public class CpPeripheralGroup extends CpPeripheralItem implements ICpPeripheralGroup {

    public CpPeripheralGroup(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public String getGroupName() {
        return getName();
    }

    @Override
    public boolean isAssigned() {
        for (ICpPeripheral p : getPeripherals()) {
            if (p.isAssigned())
                return true;
        }
        return false;
    }

    @Override
    public boolean isAssigned(String zoneName) {
        if (zoneName == null)
            return false;
        for (ICpPeripheral p : getPeripherals()) {
            if (p.isAssigned(zoneName))
                return true;
        }
        return false;
    }

    @Override
    public boolean isAssigned(ICpProcessorUnit processor) {
        if (processor == null)
            return false;
        for (ICpPeripheral p : getPeripherals()) {
            if (p.isAssigned(processor))
                return true;
        }
        return false;
    }

    @Override
    public boolean isAssigned(EMemorySecurity security) {
        if (security == null)
            return false;
        for (ICpPeripheral p : getPeripherals()) {
            if (p.isAssigned(security))
                return true;
        }
        return false;
    }

    @Override
    public boolean isAssigned(EMemoryPrivilege privilege) {
        if (privilege == null)
            return false;
        for (ICpPeripheral p : getPeripherals()) {
            if (p.isAssigned(privilege))
                return true;
        }
        return false;
    }

}
