package com.arm.cmsis.pack.ui;
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

/**
 * An interface to open an URL in browser or editor
 */
public interface IOpenURL {

    /**
     * Opens an URL in browser or editor
     *
     * @param url URL to open
     * @return error message or null if successful
     */
    String openUrl(String url);

}
