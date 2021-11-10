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

package com.arm.cmsis.pack.error;

/**
 * Strategy pattern for ICmsisConsole
 */
public interface ICmsisConsoleStrategy {

    /**
     * Returns ICmsisConsoleStrategy object: this or a proxy one
     *
     * @return ICmsisConsoleStrategy
     */
    default ICmsisConsoleStrategy getCmsisConsoleStrategy() {
        return this;
    }

    /**
     * Returns ICmsisConsole if any
     *
     * @return ICmsisConsole or null (default)
     */
    default ICmsisConsole getCmsisConsole() {
        ICmsisConsoleStrategy strategy = getCmsisConsoleStrategy();
        if (strategy != null && strategy != this) {
            return strategy.getCmsisConsole();
        }
        return null; // default has no console
    }

    /**
     * Creates default console
     *
     * @return ICmsisConsole or null
     */
    default ICmsisConsole createDefaultCmsisConsole() {
        ICmsisConsoleStrategy strategy = getCmsisConsoleStrategy();
        if (strategy != null && strategy != this) {
            return strategy.createDefaultCmsisConsole();
        }
        return null; // default does not create anything
    }

    /**
     * Sets ICmsisConsole
     *
     * @param console ICmsisConsole to set
     */
    default void setCmsisConsole(ICmsisConsole console) {
        ICmsisConsoleStrategy strategy = getCmsisConsoleStrategy();
        if (strategy != null && strategy != this) {
            strategy.setCmsisConsole(console);
        }
    }

}
