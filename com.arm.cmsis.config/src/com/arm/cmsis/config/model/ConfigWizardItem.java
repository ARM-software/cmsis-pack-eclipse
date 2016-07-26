/*******************************************************************************
 * Copyright (c) 2016 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/

package com.arm.cmsis.config.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class ConfigWizardItem implements IConfigWizardItem {

	private IConfigWizardItem fParent;
	private EItemType fItemType;
	private EItemErrorType fItemErrorType;

	private int fStartLine;
	private int fEndLine;
	private String fName;
	private String fTooltip;
	private int fSkipNumber;
	private int fMinBit;
	private int fMaxBit;
	private boolean fSelection;
	private long fMinValue;
	private long fMaxValue;
	private long fValue;
	private String fString;
	private int fStringLength;
	private int fBase;
	private long fSpinStep;
	private char fModification;
	private long fModifier;
	private boolean fInconsistent;
	private boolean fInvertValue;
	private Map<Long, String> fItems;
	private LinkedList<IConfigWizardItem> fChildren;

	/**
	 *
	 */
	public ConfigWizardItem(EItemType itemType, int line, IConfigWizardItem parent) {
		fParent = parent;
		fItemType = itemType;
		fStartLine = line;
		fEndLine = line;
		fItemErrorType = EItemErrorType.NO_ERROR;
		fName = ""; //$NON-NLS-1$
		fTooltip = ""; //$NON-NLS-1$
		fString = ""; //$NON-NLS-1$
		fMinBit = -1;
		fMaxBit = -1;
		fBase = 16;
		fModifier = 0;
		fSpinStep = 0;
		fItems = new TreeMap<>();
		fChildren = new LinkedList<>();
	}

	@Override
	public IConfigWizardItem getParent() {
		return fParent;
	}

	@Override
	public EItemType getItemType() {
		return fItemType;
	}

	@Override
	public void setItemType(EItemType type) {
		fItemType = type;
	}

	@Override
	public EItemErrorType getItemErrorType() {
		return fItemErrorType;
	}

	@Override
	public void setItemErrorType(EItemErrorType errorType) {
		fItemErrorType = errorType;
	}

	@Override
	public int getStartLine() {
		return fStartLine;
	}

	@Override
	public int getEndLine() {
		return fEndLine;
	}

	@Override
	public void setEndLine(int line) {
		fEndLine = line;
	}

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public void setName(String name) {
		fName = name;
	}

	@Override
	public String getTooltip() {
		return fTooltip;
	}

	@Override
	public void extendTooltip(String moreTooltip) {
		if (!fTooltip.isEmpty()) {
			fTooltip += System.lineSeparator();
		}
		fTooltip += moreTooltip;
	}

	@Override
	public int getSkipNumber() {
		return fSkipNumber;
	}

	@Override
	public void setSkipNumber(int skipNumber) {
		fSkipNumber = skipNumber;
	}

	@Override
	public int getMinBit() {
		return fMinBit;
	}

	@Override
	public void setMinBit(int minBit) {
		fMinBit = minBit;
	}

	@Override
	public int getMaxBit() {
		return fMaxBit;
	}

	@Override
	public void setMaxBit(int maxBit) {
		fMaxBit = maxBit;
	}

	@Override
	public boolean isSelection() {
		return fSelection;
	}

	@Override
	public void setIsSelection(boolean isSelection) {
		fSelection = isSelection;
	}

	@Override
	public long getMinValue() {
		return fMinValue;
	}

	@Override
	public void setMinValue(long minValue) {
		fMinValue = minValue;
	}

	@Override
	public long getMaxValue() {
		return fMaxValue;
	}

	@Override
	public void setMaxValue(long maxValue) {
		fMaxValue = maxValue;
	}

	@Override
	public long getValue() {
		return fValue;
	}

	@Override
	public void setValue(long value) {
		fValue = value;
	}

	@Override
	public String getString() {
		return fString;
	}

	@Override
	public void setString(String string) {
		fString = string;
	}

	@Override
	public int getStringLength() {
		return fStringLength;
	}

	@Override
	public void setStringLength(int stringLength) {
		fStringLength = stringLength;
	}

	@Override
	public int getBase() {
		return fBase;
	}

	@Override
	public void setBase(int base) {
		fBase = base;
	}

	@Override
	public long getSpinStep() {
		return fSpinStep;
	}

	@Override
	public void setSpinStep(long spinStep) {
		fSpinStep = spinStep;
	}

	@Override
	public char getModification() {
		return fModification;
	}

	@Override
	public void setModification(char modification) {
		fModification = modification;
	}

	@Override
	public long getModifier() {
		return fModifier;
	}

	@Override
	public void setModifier(long modifier) {
		fModifier = modifier;
	}

	@Override
	public boolean isInconsistent() {
		return fInconsistent;
	}

	@Override
	public void setInconsistent(boolean inconsistent) {
		fInconsistent = inconsistent;
	}

	@Override
	public boolean invertValue() {
		return fInvertValue;
	}

	@Override
	public void setInvertValue(boolean invertValue) {
		fInvertValue = invertValue;
	}

	@Override
	public Map<Long, String> getItems() {
		return fItems;
	}

	@Override
	public void addItem(long key, String value) {
		fItems.put(key, value);
	}

	@Override
	public Collection<IConfigWizardItem> getChildren() {
		return fChildren;
	}

	@Override
	public IConfigWizardItem getLastChild() {
		if (fChildren == null || fChildren.size() == 0) {
			return null;
		}
		return fChildren.getLast();
	}

	@Override
	public void addChild(IConfigWizardItem child) {
		fChildren.add(child);
	}

	@Override
	public boolean hasChildren() {
		if (fChildren == null || fChildren.isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean canModify() {
		if (fItemType == EItemType.ROOT) {
			return true;
		} else if (fItemType == EItemType.HEADING_ENABLE) {
			return getValue() > 0 && getParent().canModify();
		} else {
			return getParent().canModify();
		}
	}

	@Override
	public String toString() {
		return fName;
	}

}
