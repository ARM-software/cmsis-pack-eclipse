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

package com.arm.cmsis.pack.ui.widgets;

import com.arm.cmsis.pack.events.IRteController;
import com.arm.cmsis.pack.ui.tree.IColumnAdvisor;

/**
 * Extends IRteColumnAdvisor with IRteModelController awareness
 */
public interface IRteColumnAdvisor<TController extends IRteController> extends IColumnAdvisor {

    /**
     * Sets an RTE model controller to be used by the advisor
     *
     * @param IRteController controller to use
     */
    public void setModelController(TController modelController);

    /**
     * Returns RTE model controller used by the widget
     *
     * @return model controller
     */
    public TController getModelController();

}
