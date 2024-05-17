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

package com.arm.cmsis.pack.rte.components;

import com.arm.cmsis.pack.data.ICpComponent;
import com.arm.cmsis.pack.data.ICpGenerator;
import com.arm.cmsis.pack.info.ICpComponentInfo;

/**
 * Class that represents component entity that can be selected
 */
public interface IRteComponent extends IRteComponentItem {

    /**
     * Sets/resets component selection
     *
     * @param count number of instances to select, 0 to reset the component
     *              selection
     * @return true is selection state has changed
     */
    boolean setSelected(int count);

    /**
     * Returns number of selected instances
     *
     * @return number of selected instances
     */
    int getSelectedCount();

    /**
     * Returns maximum number of instances that can be selected for the component,
     * default is 1
     *
     * @return maximum number of component instances
     */
    int getMaxInstanceCount();

    /**
     * Returns number of used (instantiated) instances
     *
     * @return number of used instances
     */
    int getUseCount();

    /**
     * Sets/updates active component info, purges all non-active ones
     *
     * @param ci {@link ICpComponentInfo}
     */
    void setActiveComponentInfo(ICpComponentInfo ci);

    /**
     * Check is this RTE component represents a generated {@link ICpComponent}
     *
     * @return bootstrap ICpComponent or null
     */
    boolean isGenerated();

    /**
     * Check is this RTE component has associated bootstrap
     *
     * @return bootstrap ICpComponent or null
     */
    boolean isBootStrap();

    /**
     * Returns id of {@link ICpGenerator} associated with the component either as
     * generated or a bootstrap
     *
     * @return generator id or null
     */
    String getGeneratorId();
}
