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

package com.arm.cmsis.pack.ui.editors;

import com.arm.cmsis.pack.rte.IRteModel;
import com.arm.cmsis.pack.rte.RteModelController;
import com.arm.cmsis.pack.ui.OpenURL;

/**
 * RteEditor-specific controller for RTE model
 */
public class RteModelEditorController extends RteModelController {

    /**
     * @param model
     */
    public RteModelEditorController(IRteModel model) {
        super(model);
    }

    @Override
    public String openUrl(String url) {
        return OpenURL.open(url);
    }

}
