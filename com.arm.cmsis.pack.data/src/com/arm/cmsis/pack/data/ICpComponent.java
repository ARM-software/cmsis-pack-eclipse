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

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Interface describing a CMSIS component
 */
public interface ICpComponent extends ICpItem {

    /**
     * Returns if this item is an API
     *
     * @return true if the component is API
     */
    boolean isApi();

    /**
     * Checks if this component is a device startup one (Cclass="Device"
     * Cgroup="Startup" Csub="" )
     *
     * @return true if this component is a startup one
     */
    boolean isDeviceStartupComponent();

    /**
     * Checks if this component is a Core one (Cclass="CMSIS" Cgroup="CoreS"
     * Csub="")<br>
     *
     * @return true if this component is a startup one
     */
    boolean isCmsisCoreComponent();

    /**
     * Checks if this component is an RTOS one (Cclass="CMSIS" Cgroup="RTOS")<br>
     *
     * @return true if this component is a startup one
     */
    boolean isCmsisRtosComponent();

    /**
     * Checks if component should be treated as multi-instance one, even if max
     * instance count is 1
     *
     * @return true if component is a multi-instance one
     */
    boolean isMultiInstance();

    /**
     * Checks if component is a bootstrap for generator (launches specified
     * generator)
     *
     * @return true if component is bootstrap one
     */
    boolean isBootStrap();

    /**
     * Returns generator associated with the component (generated or bootstrap)
     *
     * @return {@link ICpGenerator} if component is a bootstrap or generated, null
     *         otherwise
     */
    ICpGenerator getGenerator();

    /**
     * Returns generator id associated with the component (generated or bootstrap)
     *
     * @return generator id if component is a bootstrap or generated, null otherwise
     */
    String getGeneratorId();

    /**
     * Returns number of maximum instances for the component
     *
     * @return number of maximum instances for the component
     */
    int getMaxInstances();

    /**
     * Returns piece of code corresponding this component to copy to RteComponents.h
     * file
     *
     * @return code to copy to RteComponents.h file as string
     */
    String getRteComponentsHCode();

    /**
     * Constructs component name out of ICpItem tag and attributes
     *
     * @param componentItem ICpItem representing a component or its info
     * @return component name
     */
    static String constructComponentName(ICpItem componentItem) {
        String name = CmsisConstants.EMPTY_STRING;
        if (componentItem == null)
            return name;
        if (!componentItem.getTag().equals(CmsisConstants.API_TAG)) {
            name = componentItem.getVendor();
        }
        if (componentItem.hasAttribute(CmsisConstants.CBUNDLE)) {
            name += "."; //$NON-NLS-1$
            name += componentItem.getAttribute(CmsisConstants.CBUNDLE);
        }
        name += CmsisConstants.DOUBLE_COLON;

        name += componentItem.getAttribute(CmsisConstants.CCLASS);
        name += "."; //$NON-NLS-1$

        name += componentItem.getAttribute(CmsisConstants.CGROUP);
        if (componentItem.hasAttribute(CmsisConstants.CSUB)) {
            name += "."; //$NON-NLS-1$
            name += componentItem.getAttribute(CmsisConstants.CSUB);
        }
        if (componentItem.hasAttribute(CmsisConstants.CVARIANT)) {
            name += "."; //$NON-NLS-1$
            name += componentItem.getAttribute(CmsisConstants.CVARIANT);
        }
        return name;
    }

}
