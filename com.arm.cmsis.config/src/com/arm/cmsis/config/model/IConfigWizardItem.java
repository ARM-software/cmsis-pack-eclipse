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
import java.util.Map;

/**
 * interface for configuration wizard item
 */
public interface IConfigWizardItem {

	enum EItemType {
		HEADING,
		HEADING_ENABLE,
		CODE_ENABLE,
		CODE_DISABLE,
		OPTION,
		OPTION_CHECK,
		OPTION_SELECT,
		OPTION_STRING,
		NOTIFICATION,
		ROOT
	};

	enum EItemErrorType {
		NO_ERROR,
		NUMBER_PARSE_ERROR,	// number parse error
		LOCATE_POSITION_ERROR,	// unable to locate position
	}

	EItemType getItemType();

	void setItemType(EItemType type);

	EItemErrorType getItemErrorType();

	void setItemErrorType(EItemErrorType errorType);

	IConfigWizardItem getParent();

	/**
	 * get the item's starting line in the text
	 * @return
	 */
	int getStartLine();

	/**
	 * get the item's ending line in the text
	 * @return
	 */
	int getEndLine();

	/**
	 * @param line
	 */
	void setEndLine(int line);

	/**
	 * @return item name
	 */
	String getName();

	/**
	 * @param name
	 */
	void setName(String name);

	/**
	 * @return tooltip
	 */
	String getTooltip();

	/**
	 * @param moreTooltip
	 */
	void extendTooltip(String moreTooltip);

	/**
	 * @return skip number
	 */
	int getSkipNumber();

	/**
	 * @param skipNumber
	 */
	void setSkipNumber(int skipNumber);

	/**
	 * @return minimum bit
	 */
	int getMinBit();

	/**
	 * @param minBit
	 */
	void setMinBit(int minBit);

	/**
	 * @return maximum bit
	 */
	int getMaxBit();

	/**
	 * @param maxBit
	 */
	void setMaxBit(int maxBit);

	/**
	 * @return
	 */
	boolean isSelection();

	/**
	 * @param isSelection
	 */
	void setIsSelection(boolean isSelection);

	/**
	 * @return modified minimum value
	 */
	long getMinValue();

	/**
	 * @param minValue
	 */
	void setMinValue(long minValue);

	/**
	 * @return modified maximum value
	 */
	long getMaxValue();

	/**
	 * @param maxValue
	 */
	void setMaxValue(long maxValue);

	/**
	 * @return this item's value
	 */
	long getValue();

	/**
	 * @param value
	 */
	void setValue(long value);

	/**
	 * @return the string, only valid for string item
	 */
	String getString();

	/**
	 * @param string for the string item
	 */
	void setString(String string);

	/**
	 * @return string length
	 */
	int getStringLength();

	/**
	 * @param stringLength
	 */
	void setStringLength(int stringLength);

	/**
	 * @return base
	 */
	int getBase();

	/**
	 * @param base
	 */
	void setBase(int base);

	/**
	 * @return modified spin step
	 */
	long getSpinStep();

	/**
	 * @param spinStep
	 */
	void setSpinStep(long spinStep);

	/**
	 * @return modification operator
	 */
	char getModification();

	/**
	 * @param modification the operator
	 */
	void setModification(char modification);

	/**
	 * @return modifier of the operator
	 */
	long getModifier();

	/**
	 * @param modifier
	 */
	void setModifier(long modifier);

	/**
	 * @return select items
	 */
	Map<Long, String> getItems();

	/**
	 * @param key
	 * @param value
	 */
	void addItem(long key, String value);

	/**
	 * @return children
	 */
	Collection<IConfigWizardItem> getChildren();

	/**
	 * @return last child
	 */
	IConfigWizardItem getLastChild();

	/**
	 * @param child
	 */
	void addChild(IConfigWizardItem child);

	/**
	 * @return true if this item has children
	 */
	boolean hasChildren();

	/**
	 * @return true if the comment is inconsistent
	 */
	boolean isInconsistent();

	/**
	 * @param inconsistent
	 */
	void setInconsistent(boolean inconsistent);

	/**
	 * @return true if the boolean value is inverted
	 */
	boolean invertValue();

	/**
	 * @param invertValue
	 */
	void setInvertValue(boolean invertValue);

	/**
	 * Return true if this item can be modified
	 * @return true if this item can be modified
	 */
	boolean canModify();
}
