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

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.enums.ECoreArchitecture;
import com.arm.cmsis.pack.info.CpDeviceInfo;
import com.arm.cmsis.pack.info.CpPackInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.utils.AlnumComparator;

/**
 *
 */
public class CpDeviceUnit extends CpDeviceInfo implements ICpDeviceUnit {

    protected Map<String, ICpProcessorUnit> fProcessors = new TreeMap<>(new AlnumComparator(false, false));
    protected ECoreArchitecture fCoreArchitecture = null;

    public CpDeviceUnit(ICpItem parent, String tag) {
        super(parent, tag);
    }

    public CpDeviceUnit(ICpItem parent, ICpDeviceItem device) {
        super(parent, device, device.getName());
        addProcessors(device);
    }

    @Override
    public void clear() {
        super.clear();
        fCoreArchitecture = null;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        fCoreArchitecture = null;
    }

    @Override
    protected boolean isMergeProcessorAttributes() {
        return false;
    }

    @Override
    protected String constructName() {
        if (hasAttribute(CmsisConstants.NAME))
            return getAttribute(CmsisConstants.NAME);
        return super.getDeviceName();
    }

    @Override
    public String getDescription() {
        return getAttribute(CmsisConstants.INFO);
    }

    @Override
    public synchronized String getUrl() {
        return getAttribute(CmsisConstants.URL);
    }

    @Override
    public Map<String, ICpProcessorUnit> getProcessorUnits() {
        return fProcessors;
    }

    @Override
    public int getProcessorCount() {
        return fProcessors.size();
    }

    @Override
    public ICpProcessorUnit getProcessorUnit(String pname) {
        if (pname == null || pname.isEmpty()) {
            if (fProcessors.size() == 1) {
                return fProcessors.values().iterator().next();
            }
        }
        return fProcessors.get(pname);
    }

    @Override
    public ECoreArchitecture getArchitecture() {
        if (fCoreArchitecture == null) {
            Map<String, ICpProcessorUnit> allProcessors = getProcessorUnits();
            fCoreArchitecture = ECoreArchitecture.UNKNOWN;
            for (ICpProcessorUnit processor : allProcessors.values()) {
                String core = processor.getAttribute(CmsisConstants.DCORE);
                ECoreArchitecture a = ECoreArchitecture.fromString(core);
                if (fCoreArchitecture.equals(ECoreArchitecture.UNKNOWN)) {
                    fCoreArchitecture = a;
                } else if (!fCoreArchitecture.equals(a)) {
                    fCoreArchitecture = ECoreArchitecture.MIXED;
                    break;
                }
            }
        }
        return fCoreArchitecture;
    }

    /**
     * Adds resources from device
     */
    protected void addProcessors(ICpDeviceItem device) {
        if (device == null) {
            return;
        }
        Map<String, ICpItem> processors = device.getProcessors();
        for (Entry<String, ICpItem> item : processors.entrySet()) {
            ICpItem p = item.getValue();
            ICpProcessorUnit processor = new CpProcessorUnit(this, CmsisConstants.PROCESSOR_TAG);
            processor.attributes().setAttributes(p.attributes());
            addChild(processor);
        }
    }

    @Override
    protected ICpItem createChildItem(String tag) {
        switch (tag) {
        case CmsisConstants.PROCESSOR_TAG:
            return new CpProcessorUnit(this, tag);
        case CmsisConstants.PACKAGE_TAG:
            return new CpPackInfo(this, tag);
        default:
            break;
        }
        return super.createChildItem(tag);
    }

    @Override
    public void addChild(ICpItem item) {
        super.addChild(item);
        if (item instanceof ICpProcessorUnit) {
            ICpProcessorUnit processor = (ICpProcessorUnit) item;
            fProcessors.put(processor.getName(), processor);
        }
    }

    @Override
    public boolean hasSecureCore() {
        Map<String, ICpProcessorUnit> allProcessors = getProcessorUnits();
        for (ICpProcessorUnit processor : allProcessors.values()) {
            String dtz = processor.getAttribute(CmsisConstants.DTZ);
            if (dtz.equals(CmsisConstants.TZ)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateDevice(ICpZone projectZone) {
        boolean bChanged = false;
        ICpDeviceUnit du = projectZone.getTargetDevice();
        if (du == null) {
            return true; // TODO: add error
        }
        if (updateAttributes(du.attributes())) {
            bChanged = true;
        }
        fName = du.getDeviceName();

        ICpPackInfo packInfo = du.getPackInfo();
        if (fPackInfo == null) {
            fPackInfo = new CpPackInfo(this, packInfo);
            replaceChild(fPackInfo);
        }
        if (packInfo != null) {
            fPackInfo.updateAttributes(packInfo);
        }

        ICpProcessorUnit pu = projectZone.getTargetProcessor();
        if (pu == null) {
            return true; // TODO: add error
        }
        String pname = pu.getProcessorName();
        ICpProcessorUnit processor = getProcessorUnit(pname);
        if (processor == null) {
            processor = new CpProcessorUnit(this, CmsisConstants.PROCESSOR_TAG);
            addChild(processor);
            bChanged = true;
        }
        if (processor.updateAttributes(pu.attributes())) {
            bChanged = true;
        }
        return bChanged;
    }

}
