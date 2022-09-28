/*******************************************************************************
 * Copyright (c) 2022 ARM Ltd and others.
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
import java.util.Collections;
import java.util.Map;

import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpConditionContext;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPackFilter;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.enums.IEvaluationResult;
import com.arm.cmsis.pack.info.ICpBoardInfo;
import com.arm.cmsis.pack.info.ICpConfigurationInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.components.IRteComponent;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;
import com.arm.cmsis.pack.rte.dependencies.IRteDependencyItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;

/**
 * Interface responsible for manipulating Run-Time-Configuration of a project
 * for selected device and toolchain. It can be considered as a controller that
 * connects CMSIS-Pack items with project model
 *
 */
public interface IRteModel extends IEvaluationResult {

    /**
     * Clears the model
     */
    void clear();

    /**
     * Updates the model with using default flags to resolve components by calling
     * <code>update(RteConstants.NONE)</code>
     */
    void update();

    /**
     * Updates the model
     *
     * @param flags a combination of COMPONENT__* flags
     * @see RteConstants
     */
    void update(int flags);

    /**
     * Returns pack filter associated with model
     *
     * @return ICpPackFilter
     */
    ICpPackFilter getPackFilter();

    /**
     * Sets pack filter for the model
     *
     * @param filter ICpPackFilter to set
     * @return true if new filter is not equal to previous one
     */
    boolean setPackFilter(ICpPackFilter filter);

    /**
     * Returns actual device item used by the model
     *
     * @return ICpDeviceItem
     */
    ICpDeviceItem getDevice();

    /**
     * Returns device info used by in this model
     *
     * @return ICpDeviceInfo
     */
    ICpDeviceInfo getDeviceInfo();

    /**
     * Sets device info to be used by the model
     *
     * @param deviceInfo device info to set
     */
    void setDeviceInfo(ICpDeviceInfo deviceInfo);

    /**
     * Returns actual board item used by the model
     *
     * @return ICpBoard or null
     */
    ICpBoard getBoard();

    /**
     * Returns board info used by in this model
     *
     * @return ICpBoardInfo or null if no board is used
     */
    ICpBoardInfo getBoardInfo();

    /**
     * Sets board info to be used by the model
     *
     * @param boardInfo ICpBoardInfo info to set
     */
    void setBoardInfo(ICpBoardInfo boardInfo);

    /**
     * Returns toolchain information as generic IcpItem with "Tcompiler" and
     * "Toutput" attributes
     *
     * @return ICpItem describing toolchain info
     */
    ICpItem getToolchainInfo();

    /**
     * Returns serializable model object
     *
     * @return ICpConfigurationInfo in its current state
     */
    ICpConfigurationInfo getConfigurationInfo();

    /**
     * Sets configuration data to the model
     *
     * @param info ICpConfigurationInfo to set
     */
    void setConfigurationInfo(ICpConfigurationInfo info);

    /**
     * Returns filtered component tree
     *
     * @return IRteComponentItem representing component tree root
     */
    IRteComponentItem getComponents();

    /**
     * Sets, resets or changes component selection. <br>
     * If selection state has changed re-evaluates dependencies
     *
     * @param component  to set, reset or change selection selection
     * @param nInstances number of instances to select, 0 to reset selection
     */
    void selectComponent(IRteComponent component, int nInstances);

    /**
     * Evaluates dependencies of selected components
     *
     * @return dependency evaluation result
     */
    EEvaluationResult evaluateComponentDependencies();

    /**
     * Tries to resolve component dependencies
     *
     * @return evaluation result after dependency resolving
     */
    EEvaluationResult resolveComponentDependencies();

    /**
     * Returns dependency evaluation result for given item (class, group or
     * component)
     *
     * @param item IRteComponentItem for which to get result
     * @return condition result or IGNORED if item has no result
     */
    EEvaluationResult getEvaluationResult(IRteComponentItem item);

    /**
     * Returns collection of selected components
     *
     * @return collection of selected components
     */
    Collection<IRteComponent> getSelectedComponents();

    /**
     * Returns collection of used components
     *
     * @return collection of used components
     */
    Collection<IRteComponent> getUsedComponents();

    /**
     * Returns packs currently used by configuration
     *
     * @return map id to ICpPackInfo
     */
    Map<String, ICpPackInfo> getUsedPackInfos();

    /**
     * Returns collection of dependency results (items and dependencies)
     *
     * @return collection of dependency results
     */
    Collection<? extends IRteDependencyItem> getDependencyItems();

    /**
     * Updates ICpConfigurationInfo according to selected components
     */
    void updateComponentInfos();

    /**
     * Returns hierarchical collection of devices available for this target
     *
     * @return root of device tree as IRteDeviceItem
     */
    IRteDeviceItem getDevices();

    /**
     * Returns hierarchical collection of boards
     *
     * @return root of board tree as IRteBoardItem
     */
    IRteBoardItem getBoards();

    /**
     * Return collection of used generated packs, an entry can be <code>null</code>
     * if a gpdsc is not loaded
     *
     * @return map absolute filename -> pack, null or empty if no generated packs
     *         used
     */
    Map<String, ICpPack> getGeneratedPacks();

    /**
     * Return collection of used generated pack names
     *
     * @return collection of absolute filenames
     */
    default Collection<String> getGeneratedPackNames() {
        Map<String, ICpPack> generatedPacks = getGeneratedPacks();
        if (generatedPacks != null) {
            return generatedPacks.keySet();
        }
        return Collections.emptySet();
    }

    /**
     * Returns {@link ICpPack} loaded from given gpdsc file
     *
     * @param gpdsc absolute gpdsc file name
     * @return loaded {@link ICpPack} or null if such file does not exists or load
     *         failed
     */
    ICpPack getGeneratedPack(String gpdsc);

    /**
     * Checks if the model requires given gpdsc file
     *
     * @param gpdsc absolute gpdsc file name
     * @return true if given gpdsc file is needed
     */
    boolean isGeneratedPackUsed(String gpdsc);

    /**
     * Returns filter context used by the model
     *
     * @return ICpConditionContext
     */
    default ICpConditionContext getFilterContext() {
        return null;
    }
}