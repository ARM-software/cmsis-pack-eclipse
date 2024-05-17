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

package com.arm.cmsis.zone.data;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.enums.EMemoryPrivilege;
import com.arm.cmsis.pack.enums.EMemorySecurity;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;
import com.arm.cmsis.pack.permissions.MemoryPermissions;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.zone.error.CmsisZoneError;

/**
 * Class representing a memory region or a peripheral
 */
public class CpMemoryBlock extends CpResourceItem implements ICpMemoryBlock {

    protected Map<String, ICpZoneAssignment> fAssignments = null; // assignments of the block
    protected IMemoryPermissions fOriginalAttributesAndPermissions = null; // Note: IMemoryPermissions is derived from
                                                                           // attributed item
    protected PhysicalMemoryRegion fPhysicalRegion = null;

    // cached values
    protected Long fOffset = null;
    protected Long fSize = null;
    protected Long fStart = null;

    /**
     * Default constructor
     */
    public CpMemoryBlock() {
        this(null, CmsisConstants.MEMORY_TAG);
    }

    /**
     * XML constructor
     *
     * @param parent parent ICpItem
     * @param tag    element tag
     */
    public CpMemoryBlock(ICpItem parent, String tag) {
        super(parent, tag);
    }

    /**
     * Copy constructor
     *
     * @param realBlock block to copy
     */
    public CpMemoryBlock(ICpMemoryBlock realBlock) {
        this(realBlock.getParent(), realBlock.getTag());
        attributes().addAttributes(realBlock.attributes());
    }

    /**
     * Constructs block from ICpMemory device property
     *
     * @param parent parent ICpItem
     * @param memory ICpMemory to get information from
     */
    public CpMemoryBlock(ICpItem parent, ICpMemory memory) {
        super(parent, CmsisConstants.MEMORY_TAG);
        setMemory(memory);
        initItem();
    }

    @Override
    public void initItem() {
        getOriginalAttributes(); // ensures we get initial attributes
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (fAssignments != null) {
            for (ICpZoneAssignment a : fAssignments.values()) {
                a.invalidate();
            }
        }
        fAssignments = null;
        fStart = null;
        fSize = null;
        fOffset = null;
    }

    @Override
    public boolean purge() {
        if (super.purge()) // children first
            return true;

        if (isValid())
            return false;

        if (hasSubBlocks()) {
            return false;
        }
        if (!isPeripheral() && getParentBlock() != null)
            return false;

        if (isAssigned()) {
            return false;
        }
        // check if exists
        return !resourceExists();
    }

    @Override
    public boolean resourceExists() {
        if (hasError(CmsisZoneError.Z2_MASK))
            return false;
        ICpMemoryBlock parent = getParentBlock();
        if (parent != null)
            return parent.resourceExists();
        return true;
    }

    @Override
    public boolean isValid() {
        if (!super.isValid())
            return false;
        ICpMemoryBlock parent = getParentBlock();
        if (parent != null)
            return parent.isValid();
        return true;
    }

    @Override
    public String constructId() {
        return ICpMemoryBlock.constructBlockId(getTag(), getName(), getGroupName());
    }

    @Override
    public String getPeripheralName() {
        return null;
    }

    @Override
    public String getGroupName() {
        return null;
    }

    @Override
    public ICpMemoryBlock cloneBlock() {
        ICpMemoryBlock cloned = (ICpMemoryBlock) cloneItem();
        if (cloned.hasExplicitStart()) {
            cloned.removeAttribute(CmsisConstants.FIXED);
        }
        return cloned;
    }

    @Override
    public ICpMemoryBlock copyBlockTo(ICpItem parent) {
        ICpMemoryBlock block = cloneBlock();
        block.setParent(parent);
        if (parent != null) {
            parent.addChild(block);
        }
        return block;
    }

    @Override
    public void copyChildBlocksTo(ICpItem parent) {
        Collection<ICpMemoryBlock> blocks = getSubBlocks();
        if (blocks == null || blocks.isEmpty())
            return;
        String parentRegionName = getName();
        for (ICpMemoryBlock b : blocks) {
            ICpMemoryBlock newb = b.copyBlockTo(parent);
            newb.setAttribute(CmsisConstants.PARENT, parentRegionName);
        }
    }

    @Override
    public ICpMemoryBlock getSubBlock(String name) {
        Collection<ICpMemoryBlock> blocks = getSubBlocks();
        if (blocks == null || blocks.isEmpty())
            return null;

        for (ICpMemoryBlock b : blocks) {
            if (b.getName().equals(name))
                return b;
        }
        return null;
    }

    @Override
    public String getName() {
        return getAttribute(CmsisConstants.NAME);
    }

    @Override
    public long getStartAddress(boolean bPhysical) {
        // return physical start
        long effectiveStart = 0L;
        if (bPhysical && hasAttribute(CmsisConstants.PHYSICAL)) {
            effectiveStart = attributes().getAttributeAsLong(CmsisConstants.PHYSICAL, 0L);
        } else if (hasAttribute(CmsisConstants.START)) {
            effectiveStart = IAttributes.stringToLong(getStartString(), 0);
        } else {
            ICpMemoryBlock parent = getParentOfType(ICpMemoryBlock.class);
            if (parent != null) {
                effectiveStart = parent.getStartAddress(bPhysical);
            }
        }
        long offset = getOffset();
        if (offset < 0)
            return -1L;

        effectiveStart += offset;
        return effectiveStart;
    }

    @Override
    public long getAddress() {
        return getStartAddress(true); // return physical
    }

    @Override
    public long getStart() {
        if (fStart == null) {
            fStart = getStartAddress(false); // returns logical
        }
        return fStart;
    }

    @Override
    public long getSize() {
        if (fSize == null) {
            long effectiveSize = 0;
            if (hasAttribute(CmsisConstants.SIZE)) {
                effectiveSize = IAttributes.stringToLong(getSizeString(), 0);
            } else {
                ICpMemoryBlock parent = getParentBlock();
                if (parent != null) {
                    effectiveSize = parent.getSize();
                }
            }
            fSize = effectiveSize;
        }
        return fSize;
    }

    @Override
    public long getOffset() {
        if (fOffset == null) {
            fOffset = IAttributes.stringToLong(getOffsetString(), 0);
            if (fOffset < 0) {
                fOffset = 0L;
            }
        }
        return fOffset;
    }

    @Override
    public void setOffset(Long offset) {
        if (offset < 0) {
            fOffset = 0L;
        } else {
            fOffset = offset;
        }
        fStart = null; // reset cache
        attributes().setAttributeHex(CmsisConstants.OFFSET, fOffset);
    }

    @Override
    public Long getFreeSize() {
        if (isPeripheralAccess())
            return 0L; // peripherals are fully allocated
        return getFreeSize(this);
    }

    @Override
    public Long getFreeSize(IMemoryPermissions permissions) {
        if (permissions == null)
            permissions = this;
        if (permissions.isPeripheralAccess())
            return 0L; // peripherals are fully allocated
        Long regionSize = getSize();
        // ensure it is a multiple of 32 byte
        regionSize -= regionSize % 32;
        PhysicalMemoryRegion pr = getPhysicalRegion();
        if (pr != null) {
            Long slotSize = pr.getMaxSlotSize(permissions, regionSize);
            if (regionSize > slotSize)
                return regionSize = slotSize;
        }
        return regionSize;
    }

    @Override
    public boolean isVisibleToProcessor(String pname) {
        if (!hasAttribute(CmsisConstants.PNAME))
            return true;
        if (pname == null || pname.isEmpty())
            return true;

        return pname.equals(getAttribute(CmsisConstants.PNAME));
    }

    @Override
    public Map<String, ICpZoneAssignment> getAssignments() {
        if (fAssignments == null) {
            collectAssignments();
        }
        return fAssignments;
    }

    @Override
    public ICpZoneAssignment getAssignment(String zoneName) {
        return getAssignments().get(zoneName);
    }

    @Override
    public int getAssignmentCount() {
        return getAssignments().size();
    }

    @Override
    public boolean isShared() {
        if (getAttributeAsBoolean(CmsisConstants.SHARED, false)) {
            return true; // inherited from parent zone
        }
        if (getAssignmentCount() < 2) {
            return false;
        }

        String pname = CmsisConstants.EMPTY_STRING;
        for (ICpZoneAssignment zoneItem : getAssignments().values()) {
            String zPname = zoneItem.getProcessorName();
            if (pname.isEmpty()) {
                pname = zPname;
            } else if (!zPname.equals(pname)) {
                return true; // another processor
            }
        }
        return false;
    }

    @Override
    public boolean isAssigned() {
        return !getAssignments().isEmpty();
    }

    @Override
    public boolean isAssigned(String zoneName) {
        return getAssignments().containsKey(zoneName);
    }

    @Override
    public boolean isAssigned(ICpProcessorUnit processor) {
        if (processor == null)
            return false;
        if (getAssignmentCount() == 0)
            return false;

        String pname = processor.getProcessorName();
        for (ICpZoneAssignment zoneItem : getAssignments().values()) {
            String zPname = zoneItem.getProcessorName();
            if (zPname.equals(pname)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAssigned(EMemorySecurity security) {
        if (security == null)
            return false;
        if (!isAssigned())
            return false;

        for (ICpZoneAssignment zoneItem : getAssignments().values()) {
            if (zoneItem.getSecurity().matches(security)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isAssigned(EMemoryPrivilege privilege) {
        if (privilege == null)
            return false;
        if (!isAssigned())
            return false;

        for (ICpZoneAssignment zoneItem : getAssignments().values()) {
            if (zoneItem.getPrivilege().matches(privilege)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getAssignedSecurity() {
        String s = getSecurityString();
        if (s != null && !s.isEmpty()) {
            return s;
        }
        s = CmsisConstants.EMPTY_STRING;
        for (ICpZoneAssignment zoneItem : getAssignments().values()) {
            char ch = zoneItem.getSecurity().toChar();
            if (ch != 0 && s.indexOf(ch) < 0) {
                s += ch;
            }
        }
        return s;
    }

    protected void collectAssignments() {
        fAssignments = new TreeMap<>(new AlnumComparator(false));
        ICpRootZone system = getRootZone();
        if (system == null)
            return;
        ICpZoneContainer zoneContainer = system.getZoneContainer();
        if (zoneContainer == null || !zoneContainer.hasChildren())
            return;
        String blockId = getId();
        for (ICpItem item : zoneContainer.getChildren()) {
            if (!(item instanceof ICpZone))
                continue;
            ICpZone zone = (ICpZone) item;
            if (zone.isRemoved())
                continue;
            Collection<ICpZoneAssignment> zoneItems = zone.getZoneAssignments();
            if (zoneItems == null || zoneItems.isEmpty())
                continue;
            for (ICpZoneAssignment zi : zoneItems) {
                if (zi.isRemoved())
                    continue;
                if (zi.getId().equals(blockId)) {
                    fAssignments.put(zone.getName(), zi);
                }
            }
        }
    }

    @Override
    public void addAssignment(String zoneName, ICpZoneAssignment zoneItem) {
        if (zoneName == null || zoneItem == null)
            return;
        if (fAssignments == null) {
            fAssignments = new TreeMap<>(new AlnumComparator(false));
        }
        fAssignments.put(zoneName, zoneItem);
    }

    @Override
    public void removeAssignment(String zoneName) {
        if (fAssignments == null)
            return;
        fAssignments.remove(zoneName);
    }

    @Override
    public String getType() {
        return getAttribute(CmsisConstants.TYPE);
    }

    @Override
    public void removeAssignments() {
        Map<String, ICpZoneAssignment> assignments = getAssignments();
        for (ICpZoneAssignment zoneItem : assignments.values()) {
            zoneItem.setRemoved(true);
            zoneItem.getParent().invalidate();
        }
        // if this block has child blocks remove their assignments too
        Collection<ICpMemoryBlock> childBlocks = getSubBlocks();
        if (childBlocks == null || childBlocks.isEmpty())
            return;
        for (ICpMemoryBlock block : childBlocks) {
            block.removeAssignments();
        }
    }

    @Override
    public void renameAssignments() {
        Map<String, ICpZoneAssignment> assignments = getAssignments();
        for (ICpZoneAssignment zoneItem : assignments.values()) {
            zoneItem.assign(this);
            zoneItem.getParent().invalidate();
        }
    }

    @Override
    public boolean updateAttributes(IAttributes attributes) {
        getAssignments(); // ensure assignments collected
        String oldName = getName();
        boolean bChanged = super.updateAttributes(attributes);
        if (bChanged && !oldName.equals(getName())) {
            renameAssignments();
        }
        return bChanged;
    }

    @Override
    public boolean isAttributeModifiable(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        switch (key) {
        case CmsisConstants.ACCESS:
        case CmsisConstants.SECURITY:
        case CmsisConstants.PRIVILEGE:
        case CmsisConstants.UNINIT:
        case CmsisConstants.DMA:
        case CmsisConstants.STARTUP:
        case CmsisConstants.LINKER_CONTROL:
            return true;

        case CmsisConstants.NAME:
        case CmsisConstants.INFO:
        case CmsisConstants.OFFSET:
        case CmsisConstants.SIZE:
        case CmsisConstants.FIXED:
            return !isPeripheral() && getParentBlock() != null;

        case CmsisConstants.PARENT:
        case CmsisConstants.PHYSICAL:
        case CmsisConstants.START:
        case CmsisConstants.TYPE:
        case CmsisConstants.GROUP:
        case CmsisConstants.EXTERNAL:
        case CmsisConstants.PNAME:
        case CmsisConstants.SHARED:
        default:
            break;
        }
        return false;
    }

    @Override
    public boolean isDeletable() {
        if (isRemoved())
            return true;
        if (isPeripheralAccess())
            return false;
        return getParentBlock() != null;
    }

    protected void setMemory(ICpMemory memory) {
        if (memory == null)
            return;
        String name = memory.getName();
        setName(name);

        String start = memory.getStartString();
        if (!start.isEmpty()) {
            setAttribute(CmsisConstants.START, start);
        }

        String size = memory.getSizeString();
        if (!size.isEmpty()) {
            setAttribute(CmsisConstants.SIZE, size);
        }

        String access = memory.getAccessString();
        if (!access.isEmpty()) {
            setAttribute(CmsisConstants.ACCESS, access);
        }

        String info = memory.getDescription();
        if (!info.isEmpty()) {
            setAttribute(CmsisConstants.INFO, info);
        }

        String linkerControl = memory.getLinkerControl();
        if (!linkerControl.isEmpty()) {
            setAttribute(CmsisConstants.LINKER_CONTROL, linkerControl);
        }

        if (memory.isStartup()) {
            setAttribute(CmsisConstants.STARTUP, true);
        }
        if (memory.isDefault()) {
            setAttribute(CmsisConstants.DEFAULT, true);
        }

        if (memory.isNoInit()) {
            setAttribute(CmsisConstants.UNINIT, true);
        }
    }

    @Override
    protected IAttributes getAttributesForFtlModel() {
        IAttributes a = new Attributes(getEffectiveAttributes(null));
        a.removeAttribute(CmsisConstants.DEFAULT); // we do not use it here
        a.mergeAttribute(CmsisConstants.PHYSICAL, IAttributes.longToHexString8(getStartAddress(true)));
        a.mergeAttribute(CmsisConstants.SECURITY, CmsisConstants.EMPTY_STRING);
        a.mergeAttribute(CmsisConstants.PRIVILEGE, CmsisConstants.EMPTY_STRING);
        a.mergeAttribute(CmsisConstants.STARTUP, isStartup());
        a.mergeAttribute(CmsisConstants.UNINIT, isNoInit());
        a.mergeAttribute(CmsisConstants.SHARED, isShared());
        a.mergeAttribute(CmsisConstants.DMA, isDma());
        a.mergeAttribute(CmsisConstants.EXTERNAL, isExternal());
        return a;
    }

    @Override
    public ICpItem getEffectiveParent() {
        return getParentBlock(); // will affect getEffectiveAttributes()
    }

    @Override
    public Map<String, String> getEffectiveAttributes(Map<String, String> m) {
        m = super.getEffectiveAttributes(m);
        // ensure start attribute
        if (hasAttribute(CmsisConstants.OFFSET) || !hasAttribute(CmsisConstants.START)) {
            long start = getStart();
            long address = getAddress();
            if (address != start) {
                m.put(CmsisConstants.PHYSICAL, IAttributes.longToHexString8(address));
            }
            m.put(CmsisConstants.START, IAttributes.longToHexString8(start));
            m.remove(CmsisConstants.OFFSET); // not used since START is set
        }
        if (isShared()) {
            m.put(CmsisConstants.SHARED, CmsisConstants.ONE);
        }
        return m;
    }

    @Override
    public boolean isPeripheral() {
        return false;
    }

    @Override
    public boolean isPeripheralAccess() {
        return false;
    }

    @Override
    public IMemoryPermissions getParentPermissions() {
        ICpMemoryBlock parent = getParentBlock();
        if (parent != null)
            return parent;
        if (fOriginalAttributesAndPermissions == null) {
            getOriginalAttributes(); // ensure we create the stored ones
        }
        return fOriginalAttributesAndPermissions;
    }

    @Override
    public IAttributes getOriginalAttributes() {
        if (fOriginalAttributesAndPermissions == null) {
            fOriginalAttributesAndPermissions = new MemoryPermissions(this); // make a copy
            fOriginalAttributesAndPermissions.updateAttributes(this);
        }
        return fOriginalAttributesAndPermissions.attributes();
    }

    @Override
    public boolean updatePermissions(IMemoryPermissions other) {
        if (!ICpMemoryBlock.super.updatePermissions(other))
            return false;
        Collection<ICpMemoryBlock> subBlocks = getSubBlocks();
        if (subBlocks != null) {
            for (ICpMemoryBlock block : getSubBlocks()) {
                block.adjustPermissions(this);
            }
        }
        return true;
    }

    @Override
    public void arrangeBlocks() {
        PhysicalMemoryRegion pr = getPhysicalRegion();
        if (pr != null)
            pr.arrangeBlocks();
    }

    @Override
    public PhysicalMemoryRegion getPhysicalRegion() {
        ICpMemoryBlock parent = getParentBlock();
        if (parent != null) {
            return parent.getPhysicalRegion();
        }

        if (fPhysicalRegion == null) {
            ICpRootZone rootZone = getRootZone();
            if (rootZone == null)
                return null;
            fPhysicalRegion = rootZone.getResources().getPhysicalRegion(getAddress());
        }
        return fPhysicalRegion;
    }

}
