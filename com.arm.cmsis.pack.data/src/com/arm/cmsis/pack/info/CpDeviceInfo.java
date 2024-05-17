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

package com.arm.cmsis.pack.info;

import java.util.Collection;
import java.util.Map;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpDebugConfiguration;
import com.arm.cmsis.pack.data.ICpDebugVars;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.utils.Utils;

/**
 * Default implementation of ICpDeviceInfo interface
 */
public class CpDeviceInfo extends CpItemInfo implements ICpDeviceInfo {

    protected ICpDeviceItem fDevice = null;
    protected String fPname = CmsisConstants.EMPTY_STRING;

    /**
     * Constructs CpDeviceInfo from supplied ICpDeviceItem
     *
     * @param parent         parent ICpItem
     * @param device         ICpDeviceItem to construct from
     * @param fullDeviceName devise name in form Dname:Pname
     */
    public CpDeviceInfo(ICpItem parent, ICpDeviceItem device, String fullDeviceName) {
        super(parent, CmsisConstants.DEVICE_TAG);
        setDevice(device, fullDeviceName);
    }

    /**
     * Default constructor
     *
     * @param parent parent ICpItem
     */
    public CpDeviceInfo(ICpItem parent) {
        super(parent, CmsisConstants.DEVICE_TAG);
    }

    /**
     * Constructs CpDeviceInfo from parent and tag
     *
     * @param parent parent ICpItem
     * @param tag
     */
    public CpDeviceInfo(ICpItem parent, String tag) {
        super(parent, tag);
    }

    @Override
    public ICpDeviceItem getDevice() {
        return fDevice;
    }

    @Override
    public ICpPack getPack() {
        if (fDevice != null) {
            return fDevice.getPack();
        }
        return super.getPack();
    }

    @Override
    public void setDevice(ICpDeviceItem device) {
        fDevice = device;
    }

    @Override
    public void setDevice(ICpDeviceItem device, String fullDeviceName) {
        setDevice(device);
        if (device != null) {
            fName = fullDeviceName;
            fPname = CmsisConstants.EMPTY_STRING;
            int i = fName.indexOf(':');
            if (i >= 0) {
                fPname = fName.substring(i + 1);
            } else if (fDevice == null || fDevice.getProcessorCount() == 1) {
                fPname = CmsisConstants.EMPTY_STRING;
            }
        }
        updateInfo();
    }

    @Override
    public String getProcessorName() {
        return attributes().getAttribute(CmsisConstants.PNAME, fPname);
    }

    @Override
    public void updateInfo() {
        if (fDevice != null) {
            updatePackInfo(fDevice.getPack());
            String processorName = getProcessorName();
            if (!attributes().hasAttributes()) {
                attributes().setAttributes(fDevice.getEffectiveAttributes(null));
                String url = fDevice.getUrl();
                if (url != null && !url.isEmpty()) {
                    attributes().setAttribute(CmsisConstants.URL, url);
                }
                String info = getSummary();
                if (info != null && !info.isEmpty()) {
                    attributes().setAttribute(CmsisConstants.INFO, info);
                }
            }
            // always update processor attributes
            if (isMergeProcessorAttributes()) {
                if (!processorName.isEmpty()) {
                    attributes().setAttribute(CmsisConstants.PNAME, processorName);
                }
                ICpItem proc = fDevice.getProcessor(processorName);
                if (proc != null) {
                    attributes().mergeAttributes(proc.attributes());
                }
            }
        } else {
            updatePackInfo(null);
        }
    }

    protected boolean isMergeProcessorAttributes() {
        return true;
    }

    @Override
    public String getName() {
        if (fName == null || fName.isEmpty()) {
            fName = getFullDeviceName();
        }
        return fName;
    }

    @Override
    protected String constructName() {
        return getFullDeviceName();
    }

    @Override
    public String getVersion() {
        return CmsisConstants.EMPTY_STRING;
    }

    @Override
    public String getDescription() {
        ICpItem effectiveProps = getEffectiveProperties();
        if (effectiveProps != null) {
            return effectiveProps.getDescription();
        }
        return getAttribute(CmsisConstants.INFO);
    }

    @Override
    public synchronized String getUrl() {
        if (fDevice != null) {
            return fDevice.getUrl();
        }
        return getAttribute(CmsisConstants.URL);
    }

    @Override
    public EEvaluationResult getEvaluationResult() {
        return fResolveResult;
    }

    @Override
    public void setEvaluationResult(EEvaluationResult result) {
        fResolveResult = result;
    }

    @Override
    public ICpItem getEffectiveProperties() {
        if (fDevice != null) {
            return fDevice.getEffectiveProperties(getProcessorName());
        }
        return null;
    }

    @Override
    public ICpDebugConfiguration getDebugConfiguration() {
        if (fDevice != null) {
            return fDevice.getDebugConfiguration(getProcessorName());
        }
        return null;
    }

    @Override
    public String getSummary() {
        String summary = CmsisConstants.EMPTY_STRING;
        if (getProcessorName() != null) {
            summary += CmsisConstants.ARM + ' ' + getAttribute(CmsisConstants.DCORE);
            String clock = getClockSummary();
            if (!clock.isEmpty()) {
                summary += ' ' + clock;
            }
        } else if (fDevice != null) {
            Map<String, ICpItem> processors = fDevice.getProcessors();
            for (ICpItem p : processors.values()) {
                if (!summary.isEmpty()) {
                    summary += ", "; //$NON-NLS-1$
                }
                summary += CmsisConstants.ARM + ' ' + p.getAttribute(CmsisConstants.DCORE);
                String clock = Utils.getScaledClockFrequency(p.getAttribute(CmsisConstants.DCLOCK));
                if (!clock.isEmpty()) {
                    summary += ' ' + clock;
                }
            }
        }
        String memory = getMemorySummary();
        if (!memory.isEmpty()) {
            summary += ", " + memory; //$NON-NLS-1$
        }
        return summary;
    }

    @Override
    public String getClockSummary() {
        return Utils.getScaledClockFrequency(getAttribute(CmsisConstants.DCLOCK));
    }

    @Override
    public String getMemorySummary() {
        ICpItem effectiveProps = getEffectiveProperties();
        if (effectiveProps == null) {
            return CmsisConstants.EMPTY_STRING;
        }

        Collection<ICpItem> mems = effectiveProps.getChildren(CmsisConstants.MEMORY_TAG);
        if (mems == null || mems.isEmpty()) {
            return CmsisConstants.EMPTY_STRING;
        }

        long ramSize = 0;
        long romSize = 0;
        for (ICpItem item : mems) {
            if (!(item instanceof ICpMemory)) {
                continue;
            }
            ICpMemory m = (ICpMemory) item;
            long size = m.attributes().getAttributeAsLong(CmsisConstants.SIZE, 0);
            if (size == 0) {
                continue;
            }
            if (m.isRAM()) {
                ramSize += size;
            } else if (m.isROM()) {
                romSize += size;
            }
        }

        String summary = CmsisConstants.EMPTY_STRING;
        if (ramSize > 0) {
            summary += Utils.getMemorySizeString(ramSize) + ' ' + CmsisConstants.RAM;
        }
        if (romSize > 0) {
            if (!summary.isEmpty()) {
                summary += ", "; //$NON-NLS-1$
            }
            summary += Utils.getMemorySizeString(romSize) + ' ' + CmsisConstants.ROM;
        }
        return summary;
    }

    @Override
    public Collection<ICpItem> getBooks() {
        ICpItem effectiveProps = getEffectiveProperties();
        if (effectiveProps == null) {
            return null;
        }
        return effectiveProps.getBooks();
    }

    @Override
    public String getDgbConfFileName() {
        ICpDebugConfiguration dconf = getDebugConfiguration();
        if (dconf == null)
            return CmsisConstants.EMPTY_STRING;
        ICpDebugVars dv = dconf.getDebugVars();
        if (dv == null)
            return CmsisConstants.EMPTY_STRING;
        String dgbConfgFileName = dv.getName();
        if (dgbConfgFileName == null || dgbConfgFileName.isEmpty())
            return CmsisConstants.EMPTY_STRING;

        String relPath = CmsisConstants.RTE + '/' + Utils.extractFileName(dgbConfgFileName);
        return getAbsolutePath(relPath);
    }

}
