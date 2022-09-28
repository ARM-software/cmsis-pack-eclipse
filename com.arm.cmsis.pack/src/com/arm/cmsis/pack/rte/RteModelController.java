/*******************************************************************************
* Copyright (c) 2022 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.rte;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpPackFilter;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpConditionContext;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpGenerator;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.EVersionMatchMode;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RteEventProxy;
import com.arm.cmsis.pack.generic.Attributes;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.ICpBoardInfo;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpPackFilterInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.packs.IRtePack;
import com.arm.cmsis.pack.rte.packs.IRtePackCollection;
import com.arm.cmsis.pack.rte.packs.IRtePackFamily;
import com.arm.cmsis.pack.rte.packs.RtePackCollection;

/**
 * Default implementation of IRteModelController interface
 *
 */
public abstract class RteModelController extends RteEventProxy implements IRteModelController {

    protected IRteModel fModel = null;
    // filtered Packs
    protected ICpPackFilter fSavedPackFilter = null;
    protected ICpPackFilter fCurrentPackFilter = null;
    protected IRtePackCollection fRtePackCollection = null;

    protected IAttributes fSavedDeviceAttributes = null;
    protected IAttributes fSavedBoardAttributes = null;
    protected Set<String> fSavedComponentKeys = null;
    protected Set<String> fSavedGpdscFiles = null;

    protected boolean fbComponentSelectionModified = false;
    protected boolean fbPackFilterModified = false;
    protected boolean fbDeviceModified = false;
    protected boolean fbBoardModified = false;
    protected boolean fbShowUsedPacksOnly = true;

    /**
     * Default constructor
     */
    public RteModelController(IRteModel model) {
        fModel = model;
    }

    @Override
    public IRteModel getModel() {
        return fModel;
    }

    // @Override
    @Override
    public void clear() {
        if (fModel != null) {
            fModel.clear();
        }
        fModel = null;
        fSavedPackFilter = null;
        fCurrentPackFilter = null;
        fRtePackCollection = null;
        fSavedDeviceAttributes = null;
        fSavedBoardAttributes = null;
        fSavedComponentKeys = null;
        fSavedGpdscFiles = null;
    }

    @Override
    public boolean isComponentSelectionModified() {
        return fbComponentSelectionModified;
    }

    @Override
    public boolean isPackFilterModified() {
        return fbPackFilterModified;
    }

    @Override
    public boolean isDeviceModified() {
        return fbDeviceModified;
    }

    @Override
    public boolean isBoardModified() {
        return fbBoardModified;
    }

    protected boolean isGpdscFileListModified() {
        Map<String, ICpPack> genPacks = getGeneratedPacks();

        if (fSavedGpdscFiles == null)
            return genPacks != null && !genPacks.isEmpty();
        if (genPacks == null)
            return fSavedGpdscFiles != null && !fSavedGpdscFiles.isEmpty();

        return !fSavedGpdscFiles.equals(genPacks.keySet());

    }

    protected Set<String> collectGpdscFiles() {
        Map<String, ICpPack> genPacks = getGeneratedPacks();
        if (genPacks == null || genPacks.isEmpty()) {
            return null;
        }
        return new HashSet<String>(genPacks.keySet());
    }

    @Override
    public boolean isModified() {
        return isDeviceModified() || isBoardModified() || isPackFilterModified() || isComponentSelectionModified();
    }

    protected boolean checkIfComponentsModified() {
        Set<String> keys = collectComponentKeys();
        return !keys.equals(fSavedComponentKeys);
    }

    protected Set<String> collectComponentKeys() {
        Set<String> ids = new HashSet<String>();
        ICpConfigurationInfo info = getConfigurationInfo();
        collectComponentKeys(ids, info.getGrandChildren(CmsisConstants.COMPONENTS_TAG));
        collectComponentKeys(ids, info.getGrandChildren(CmsisConstants.APIS_TAG));
        return ids;
    }

    protected static void collectComponentKeys(Set<String> ids, Collection<? extends ICpItem> children) {
        if (children == null || children.isEmpty()) {
            return;
        }
        for (ICpItem child : children) {
            if (!(child instanceof ICpComponentInfo)) {
                continue;
            }
            ICpComponentInfo ci = (ICpComponentInfo) child;
            String key = ci.getName() + ':' + ci.getAttribute(CmsisConstants.INSTANCES);
            if (ci.isVersionFixed()) {
                key += ':' + ci.getVersion();
            }
            ids.add(key);
        }
    }

    @Override
    public void reloadPacks() {
        collectPacks();
        fRtePackCollection.setPackFilterInfo(fModel.getConfigurationInfo().getPackFilterInfo());
        update();
    }

    protected void collectPacks() {
        ICpPackCollection allPacks = null;
        ICpPackManager pm = CpPlugIn.getPackManager();
        if (pm != null) {
            allPacks = pm.getInstalledPacks();
        }
        fRtePackCollection = new RtePackCollection();
        if (allPacks != null) {
            fRtePackCollection.addCpItem(allPacks);
        }
    }

    @Override
    public void setConfigurationInfo(ICpConfigurationInfo info) {
        if (info == getDataInfo()) {
            return;
        }

        if (info == null) {
            clear();
            return;
        }

        fSavedPackFilter = new CpPackFilter(info.createPackFilter());
        fCurrentPackFilter = new CpPackFilter(fSavedPackFilter);
        fSavedDeviceAttributes = new Attributes(info.getDeviceInfo().attributes());
        if (info.getBoardInfo() != null) {
            fSavedBoardAttributes = new Attributes(info.getBoardInfo().attributes());
        } else {
            fSavedBoardAttributes = new Attributes();
        }
        collectPacks();
        fRtePackCollection.setPackFilterInfo(info.getPackFilterInfo());
        fModel.setConfigurationInfo(info); // will update used packs
        fRtePackCollection.setUsedPacks(getUsedPackInfos());
        fSavedComponentKeys = collectComponentKeys(); // initial update
        fSavedGpdscFiles = collectGpdscFiles();// initial update
        emitRteEvent(RteEvent.CONFIGURATION_MODIFIED, this); // will update widgets if they are already open
    }

    @Override
    public void updateConfigurationInfo() {
        if (getConfigurationInfo() == null) {
            return;
        }
        if (setPackFilter(fCurrentPackFilter)) {
            update();
        } else {
            updateComponentInfos();
        }
    }

    @Override
    public void updateComponentInfos() {
        fModel.updateComponentInfos();
        fRtePackCollection.setUsedPacks(getUsedPackInfos());
    }

    @Override
    public void update() {
        update(RteConstants.NONE);
    }

    @Override
    public void update(int flags) {
        updateComponentInfos();
        updatePackFilterInfo();
        fModel.update(flags);
        fRtePackCollection.setUsedPacks(getUsedPackInfos());
        emitRteEvent(RteEvent.CONFIGURATION_MODIFIED, this);
    }

    @Override
    public void commit() {

        ICpConfigurationInfo info = fModel.getConfigurationInfo();
        if (info != null) {
            setSavedFlags(info.getGrandChildren(CmsisConstants.COMPONENTS_TAG));
            setSavedFlags(info.getGrandChildren(CmsisConstants.APIS_TAG));
        }
        if (isGpdscFileListModified()) {
            update();
        } else {
            updateConfigurationInfo();
        }

        fModel.getComponents().purge();
        fRtePackCollection.purge();
        fSavedPackFilter = new CpPackFilter(getPackFilter());
        fCurrentPackFilter = new CpPackFilter(fSavedPackFilter);
        fSavedDeviceAttributes = new Attributes(getDeviceInfo().attributes());
        if (getBoardInfo() != null) {
            fSavedBoardAttributes = new Attributes(getBoardInfo().attributes());
        } else {
            fSavedBoardAttributes = new Attributes();
        }
        fSavedComponentKeys = collectComponentKeys();
        fSavedGpdscFiles = collectGpdscFiles();
        fbComponentSelectionModified = false;
        fbPackFilterModified = false;
        fbDeviceModified = false;
        fbBoardModified = false;
    }

    protected void setSavedFlags(Collection<? extends ICpItem> children) {

        if (children != null) {
            for (ICpItem item : children) {
                if (item instanceof ICpComponentInfo) {
                    ICpComponentInfo ci = (ICpComponentInfo) item;
                    ci.setSaved(true);
                }
            }
        }
    }

    @Override
    public IRtePackCollection getRtePackCollection() {
        return fRtePackCollection;
    }

    /**
     * Returns absolute gpdsc filename associated with component
     *
     * @param component {@link IRteComponent}
     * @return associated gpdsc file or null if none
     */
    protected String getGpdsc(IRteComponent component) {
        if (component == null)
            return null;
        ICpComponent c = component.getActiveCpComponent();
        if (c == null)
            return null;

        if (c.isGenerated()) {
            ICpPack pack = c.getPack();
            if (pack == null)
                return null; // should not happen
            return pack.getFileName();
        }
        ICpGenerator gen = c.getGenerator();
        if (gen != null) {
            ICpEnvironmentProvider ep = CpPlugIn.getEnvironmentProvider();
            return ep.expandString(gen.getGpdsc(), getConfigurationInfo(), true);
        }

        return null;
    }

    @Override
    public void selectComponent(IRteComponent component, int nInstances) {
        if (component == null)
            return;
        ICpComponent old = component.getActiveCpComponent();
        fModel.selectComponent(component, nInstances);
        postChangeSelection(component, old);
    }

    @Override
    public void selectActiveVariant(IRteComponentItem item, String variant) {
        if (item == null)
            return;
        ICpComponent old = item.getActiveCpComponent();
        item.setActiveVariant(variant);
        postChangeSelection(item, old);
    }

    @Override
    public void selectActiveVendor(IRteComponentItem item, String vendor) {
        if (item == null)
            return;
        ICpComponent old = item.getActiveCpComponent();
        item.setActiveVendor(vendor);
        postChangeSelection(item, old);
    }

    @Override
    public void selectActiveVersion(IRteComponentItem item, String version) {
        if (item == null)
            return;
        ICpComponent old = item.getActiveCpComponent();
        item.setActiveVersion(version);
        postChangeSelection(item, old);
    }

    protected void postChangeSelection(IRteComponentItem item, ICpComponent oldComponent) {
        if (item instanceof IRteComponent) {
            IRteComponent component = (IRteComponent) item;
            String genId = component.getGeneratorId();
            String oldGenId = oldComponent != null ? oldComponent.getGeneratorId() : null;
            if (oldGenId != null && !oldGenId.equals(genId)) {
                adjustGeneratedSelection(component, oldGenId, false);
            }
            if (genId != null) {
                adjustGeneratedSelection(component, genId, component.isSelected());
            }
        }
        updateComponentInfos();
        evaluateComponentDependencies();
    }

    protected void adjustGeneratedSelection(IRteComponent component, String genId, boolean bSelect) {
        IRteComponentItem componentRoot = getComponents();
        Collection<IRteComponent> componentsToSelect = componentRoot.getGeneratorComponents(genId, null);
        if (componentsToSelect == null || componentsToSelect.isEmpty())
            return;
        int count = bSelect ? 1 : 0;
        for (IRteComponent c : componentsToSelect) {
            if (c == component)
                continue;
            c.setSelected(count);
        }
    }

    protected void emitComponentSelectionModified() {
        fbComponentSelectionModified = checkIfComponentsModified();
        emitRteEvent(RteEvent.COMPONENT_SELECTION_MODIFIED, this);
    }

    protected void emitPackFilterModified() {
        fCurrentPackFilter = fRtePackCollection.createPackFiler();
        fbPackFilterModified = !fSavedPackFilter.equals(fCurrentPackFilter);
        emitRteEvent(RteEvent.FILTER_MODIFIED, this);
    }

    @Override
    public EEvaluationResult resolveComponentDependencies() {
        EEvaluationResult res = fModel.resolveComponentDependencies();
        updateComponentInfos();
        emitComponentSelectionModified();
        return res;
    }

    @Override
    public ICpPackFilter getPackFilter() {
        return fModel.getPackFilter();
    }

    @Override
    public boolean setPackFilter(ICpPackFilter filter) {
        return fModel.setPackFilter(filter);
    }

    @Override
    public ICpDeviceItem getDevice() {
        return fModel.getDevice();
    }

    @Override
    public ICpDeviceInfo getDeviceInfo() {
        return fModel.getDeviceInfo();
    }

    @Override
    public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
        boolean changed = false;
        int updateFlags = RteConstants.NONE;
        if (getDeviceInfo() == null) {
            changed = true;
        } else {
            changed = !getDeviceInfo().attributes().equals(deviceInfo.attributes());
            if (changed)
                updateFlags = RteConstants.COMPONENT_IGNORE_ALL;
        }

        if (changed) {
            fbDeviceModified = !fSavedDeviceAttributes.equals(deviceInfo.attributes());
            fModel.setDeviceInfo(deviceInfo);
            update(updateFlags);
        }
    }

    @Override
    public ICpBoardInfo getBoardInfo() {
        return fModel.getBoardInfo();
    }

    @Override
    public ICpBoard getBoard() {
        return fModel.getBoard();
    }

    @Override
    public void setBoardInfo(ICpBoardInfo boardInfo) {
        if (getBoardInfo() == boardInfo) {
            return;
        }

        boolean changed = false;
        int updateFlags = RteConstants.NONE;

        if (getBoardInfo() == null) {
            changed = true;
        } else {
            if (boardInfo == null) // "-No Board-" item is selected.
                changed = true;
            else
                changed = !getBoardInfo().attributes().equals(boardInfo.attributes());

            if (changed)
                updateFlags = RteConstants.COMPONENT_IGNORE_ALL;
        }

        if (changed) {
            if (boardInfo != null)
                fbBoardModified = !fSavedBoardAttributes.equals(boardInfo.attributes());
            else
                fbBoardModified = true;

            fModel.setBoardInfo(boardInfo);
            update(updateFlags);
        }
    }

    @Override
    public ICpItem getToolchainInfo() {
        return fModel.getToolchainInfo();
    }

    @Override
    public ICpConfigurationInfo getConfigurationInfo() {
        return fModel.getConfigurationInfo();
    }

    @Override
    public IRteComponentItem getComponents() {
        return fModel.getComponents();
    }

    @Override
    public EEvaluationResult evaluateComponentDependencies() {
        EEvaluationResult res = fModel.evaluateComponentDependencies();
        emitComponentSelectionModified();
        return res;
    }

    @Override
    public EEvaluationResult getEvaluationResult() {
        return fModel.getEvaluationResult();
    }

    @Override
    public EEvaluationResult getEvaluationResult(IRteComponentItem item) {
        return fModel.getEvaluationResult(item);
    }

    @Override
    public void setEvaluationResult(EEvaluationResult result) {
        fModel.setEvaluationResult(result);
    }

    @Override
    public Collection<IRteComponent> getSelectedComponents() {
        return fModel.getSelectedComponents();
    }

    @Override
    public Collection<IRteComponent> getUsedComponents() {
        return fModel.getUsedComponents();
    }

    @Override
    public Map<String, ICpPackInfo> getUsedPackInfos() {
        return fModel.getUsedPackInfos();
    }

    @Override
    public Collection<? extends IRteDependencyItem> getDependencyItems() {
        return fModel.getDependencyItems();
    }

    // @Override
    public void updatePackFilterInfo() {
        ICpPackFilterInfo packFilterInfo = fRtePackCollection.createPackFilterInfo();
        ICpConfigurationInfo confInfo = getConfigurationInfo();
        packFilterInfo.setParent(confInfo);
        confInfo.replaceChild(packFilterInfo);
    }

    @Override
    public void selectPack(IRtePack pack, boolean select) {
        if (pack != null) {
            pack.setSelected(select);
            IRtePackFamily family = pack.getFamily();
            if (family != null) {
                family.updateVersionMatchMode();
            }
            emitPackFilterModified();
        }
    }

    @Override
    public void setVesrionMatchMode(IRtePackFamily packFamily, EVersionMatchMode mode) {
        if (packFamily != null) {
            packFamily.setVersionMatchMode(mode);
            emitPackFilterModified();
        }
    }

    @Override
    public boolean isUseAllLatestPacks() {
        return fRtePackCollection.isUseAllLatestPacks();
    }

    @Override
    public void setUseAllLatestPacks(boolean bUseLatest) {
        fRtePackCollection.setUseAllLatestPacks(bUseLatest);
        emitPackFilterModified();
    }

    @Override
    public void setShowUsedPacksOnly(boolean bShowUsed) {
        fbShowUsedPacksOnly = bShowUsed;
        emitPackFilterModified();
    }

    @Override
    public boolean isShowUsedPacksOnly() {
        return fbShowUsedPacksOnly;
    }

    @Override
    public IRteDeviceItem getDevices() {
        return fModel.getDevices();
    }

    @Override
    public IRteBoardItem getBoards() {
        return fModel.getBoards();
    }

    @Override
    public Map<String, ICpPack> getGeneratedPacks() {
        return fModel.getGeneratedPacks();
    }

    @Override
    public ICpPack getGeneratedPack(String gpdsc) {
        return fModel.getGeneratedPack(gpdsc);
    }

    @Override
    public boolean isGeneratedPackUsed(String gpdsc) {
        return fModel.isGeneratedPackUsed(gpdsc);
    }

    @Override
    public ICpConditionContext getFilterContext() {
        return fModel.getFilterContext();
    }
}
