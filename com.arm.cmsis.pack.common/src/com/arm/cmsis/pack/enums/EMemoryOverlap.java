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

package com.arm.cmsis.pack.enums;

/**
 * Enumeration of possible situations of two memories overlapping
 */
public enum EMemoryOverlap {

    NO_OVERLAP, FULL, // start and end addresses are equal
    INSIDE, // memory lies completely inside another memory
    OUTSIDE, // memory contains other memory (i.e. the other memory is INSIDE)
    INTERSECT; // memories intersect each other

    public boolean isOverlap() {
        return this != NO_OVERLAP;
    }

    public boolean isFull() {
        return this == FULL;
    }

    public boolean isPartial() {
        return isOverlap() && !isFull();
    }

    public boolean isIntersect() {
        return this == INTERSECT;
    }

    public boolean isInside() {
        return this == INSIDE;
    }

    public boolean isOutside() {
        return this == OUTSIDE;
    }

}
