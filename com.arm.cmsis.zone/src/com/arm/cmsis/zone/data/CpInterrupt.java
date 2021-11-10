package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Implementation of an interrupt tag
 */
public class CpInterrupt extends CpResourceItem implements ICpInterrupt {

    public CpInterrupt(ICpItem parent) {
        super(parent, CmsisConstants.INTERRUPT);
    }

    public CpInterrupt(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public String getIrqNumberString() {
        if (hasAttribute(CmsisConstants.VALUE))
            return getAttribute(CmsisConstants.VALUE);
        return getAttribute(CmsisConstants.IRQN);
    }

    @Override
    public String getValue() {
        return getIrqNumberString();
    }

    @Override
    public Long getIrqNumber() {
        return IAttributes.stringToLong(getIrqNumberString(), 0);
    }

    @Override
    protected IAttributes getAttributesForFtlModel() {
        IAttributes a = new Attributes(attributes());
        a.removeAttribute(CmsisConstants.VALUE);
        a.setAttribute(CmsisConstants.IRQN, getIrqNumberString());
        if (!a.hasAttribute(CmsisConstants.SECURITY)) {
            String s = CmsisConstants.EMPTY_STRING;
            ICpPeripheral p = getParentPeripheral();
            if (p != null) {
                s = p.getAssignedSecurity();
            }
            a.setAttribute(CmsisConstants.SECURITY, s);
        }
        return a;
    }

    @Override
    public String constructId() {
        return getIrqNumberString() + '-' + getName();
    }

}
