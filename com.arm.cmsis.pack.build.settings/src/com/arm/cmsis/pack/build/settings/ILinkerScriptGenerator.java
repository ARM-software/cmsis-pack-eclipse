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

package com.arm.cmsis.pack.build.settings;

import org.eclipse.core.runtime.CoreException;

import com.arm.cmsis.pack.build.IMemorySettings;

/**
 * Interface responsible for generating linker script/scatter file
 */
public interface ILinkerScriptGenerator {

    /**
     * Generates linker script (scatter file) content
     *
     * @param memorySettings memory settings as input for generator
     * @return string representing content of linker script/ scatter file or null if
     *         an error is occurred
     */
    String generate(IMemorySettings memorySettings) throws CoreException;

    /**
     * Returns file extension that should be used by linker script file<br>
     * Usually they are <code>"ld"</code> for linker script and <code>"sct"</code>
     * for scatter file
     *
     * @return file extension
     */
    String getFileExtension();

}
