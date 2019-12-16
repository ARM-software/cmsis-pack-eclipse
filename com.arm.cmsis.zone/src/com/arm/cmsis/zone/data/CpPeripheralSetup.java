package com.arm.cmsis.zone.data;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.EMemoryPrivilege;
import com.arm.cmsis.pack.enums.EMemorySecurity;
import com.arm.cmsis.pack.generic.IAttributes;

/**
 * Implementation of an interrupt tag
 */
public class CpPeripheralSetup extends CpResourceItem implements ICpPeripheralSetup {

	public CpPeripheralSetup(ICpItem parent) {
		super(parent, CmsisConstants.SETUP);
	}

	public CpPeripheralSetup(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public String getIndexString() {
		return getAttribute(CmsisConstants.INDEX);
	}
	
	@Override
	public Long getIndex() {
		return IAttributes.stringToLong(getIndexString(), 0);
	}
	
	@Override
	public String constructId() {
		return getName() + "[" + getIndex() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean matchesPermissions() {
		EMemorySecurity security = getSecurity();
		EMemoryPrivilege privilege = getPrivilege();
		ICpSlot slot = getParentSlot();
		if(slot != null) {
			return (!security.isSpecified() || security.isSecure() == slot.isSecure()) && 
				   (!privilege.isSpecified() || privilege.isPrivileged() == slot.isPrivileged());
		}
		
		ICpPeripheralItem pItem = getParentPeripheralItem();
		if(pItem != null) {
			return (!security.isSpecified() || security.isSecure() == pItem.isSecure()) && 
		  	       (!privilege.isSpecified() || privilege.isPrivileged() == pItem.isPrivilegedAccess());
		}

		return true; // should be unreachable 
	}
}
