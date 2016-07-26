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

package com.arm.cmsis.parser;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

import com.arm.cmsis.config.Messages;
import com.arm.cmsis.config.model.ConfigWizardItem;
import com.arm.cmsis.config.model.IConfigWizardItem;
import com.arm.cmsis.config.model.IConfigWizardItem.EItemErrorType;
import com.arm.cmsis.config.model.IConfigWizardItem.EItemType;
import com.arm.cmsis.parser.ConfigWizardScanner.ETokenType;
import com.arm.cmsis.utils.Utils;

/**
 * Parser for the config wizard
 */
public class ConfigWizardParser {

	private static final int LAST_CONFIG_WIZARD_START_LINE = 100;

	private ConfigWizardScanner fScanner;

	private IDocument fDocument;

	private boolean fContainWizard;

	private int fParsingErrorOffset;

	private IConfigWizardItem fRoot;

	private int fStartParseOffset;

	private int fEndParseOffset;

	private IToken cToken; // current token
	private ETokenType cType; // current token type

	private TreeMap<Integer, String> fNumberContainer; // offset->number string

	private TreeMap<Integer, String> fStringContainer; // offset->string

	private TreeMap<Integer, String> fCommentContainer; // offset->string

	String fParsingErrorMessage;

	/**
	 * Parser for the config wizard
	 */
	public ConfigWizardParser(ConfigWizardScanner scanner, IDocument document) {
		fScanner = scanner;
		fDocument = document;
		fContainWizard = false;
		fNumberContainer = new TreeMap<>();
		fStringContainer = new TreeMap<>();
		fCommentContainer = new TreeMap<>();

		initialize();
	}

	public void clear() {
		fScanner.clear();
		fParsingErrorOffset = -1;
		fContainWizard = false;
		fRoot = null;
		fStartParseOffset = fEndParseOffset = fDocument.getLength();
		cToken = null;
		cType = null;
		fNumberContainer.clear();
		fStringContainer.clear();
		fCommentContainer.clear();
	}

	protected void initialize() {
		fStartParseOffset = fEndParseOffset = fDocument.getLength();
		findConfigurationWizard();
	}

	public IConfigWizardItem parse() {
		clear();
		setParseRange(0, fDocument.getLength());
		return doParse();
	}

	public boolean findConfigurationWizard() {
		int offset = fDocument.getLength();
		try {
			offset = fDocument.getLineOffset(LAST_CONFIG_WIZARD_START_LINE);
		} catch (BadLocationException e) {
		}
		clear();
		setParseRange(0, offset);
		IToken token = fScanner.nextToken();
		while (!token.isEOF() && fScanner.getTokenType(token) != ETokenType.START) {
			token = fScanner.nextToken();
		}
		if (fScanner.getTokenType(token) != ETokenType.START) {
			fContainWizard = false;
		} else {
			fContainWizard = true;
		}
		return fContainWizard;
	}

	private void setParseRange(int start, int end) {
		fScanner.setRange(fDocument, start, end);
	}

	private IConfigWizardItem doParse() {
		fRoot = new ConfigWizardItem(EItemType.ROOT, 0, null);
		fRoot.setName("Root"); //$NON-NLS-1$

		IToken token = fScanner.nextToken();
		while (!token.isEOF() && fScanner.getTokenType(token) != ETokenType.START) {
			token = fScanner.nextToken();
		}
		if (token.isEOF()) {
			return null;
		}

		// Start parsing!!!
		fContainWizard = true;
		fStartParseOffset = fScanner.getTokenOffset();

		fRoot = parseItems(fRoot, ETokenType.EOC);
		if (fRoot != null) {
			setValuesAndStrings();
		}

		return fRoot;
	}

	protected IConfigWizardItem parseItems(IConfigWizardItem parent, ETokenType endToken) {

		getNextToken();

		while (cType != ETokenType.EOC && cType != ETokenType.HEADING_END && cType != ETokenType.HEADING_ENABLE_END
				&& cType != ETokenType.CODE_END) {
			ETokenType type = cType;
			IConfigWizardItem child = parseItem(parent);
			if (child == null || cType == ETokenType.UNKNOWN || cType == ETokenType.COMMENT
					|| cType == ETokenType.BLOCK_COMMENT) {
				if (fParsingErrorOffset == -1) {
					fParsingErrorMessage = NLS.bind(Messages.ConfigWizardParser_WrongTokenFormat,
							fScanner.getTokenContent(cToken));
					syntaxError();
				}
				return null;
			}
			if (type != ETokenType.TOOLTIP && type != ETokenType.NUMBER && type != ETokenType.STRING
					&& type != ETokenType.START) {
				parent.addChild(child);
			}
		}

		if (cType == endToken) {
			parent.setEndLine(fScanner.getCurrentLineNumber());
			if (cType == ETokenType.CODE_END) {
				parent = analyseCodeContent(parent);
			}
			return parent;
		}
		if (endToken == ETokenType.EOC) {
			fEndParseOffset = fScanner.getTokenOffset();
		}
		fParsingErrorMessage = Messages.ConfigWizardParser_WrongEndingToken + getTokenTypeString(endToken);
		fParsingErrorMessage += Messages.ConfigWizardParser_RealIs + getTokenTypeString(cType);
		syntaxError();
		return null;
	}

	protected IConfigWizardItem parseItem(IConfigWizardItem parent) {
		IConfigWizardItem item;
		switch (cType) {
			case HEADING:
				return parseHeading(parent);
			case HEADING_ENABLE:
				return parseHeadingEnable(parent);
			case CODE_ENABLE:
			case CODE_DISABLE:
				return parseCodeComment(parent);
			case OPTION:
				item = new ConfigWizardItem(EItemType.OPTION, fScanner.getCurrentLineNumber(), parent);
				return parseOption(item);
			case OPTION_CHECK:
				item = new ConfigWizardItem(EItemType.OPTION_CHECK, fScanner.getCurrentLineNumber(), parent);
				return parseOption(item);
			case OPTION_STRING:
				item = new ConfigWizardItem(EItemType.OPTION_STRING, fScanner.getCurrentLineNumber(), parent);
				return parseOptionString(item);
			case TOOLTIP:
				item = parent.getLastChild();
				if (item == null) {
					item = parent;
				}
				return parseTooltip(item);
			case NUMBER:
				parseNumber();
				return parent;
			case STRING:
				parseString();
				return parent;
			case START:
				getNextToken();
				return parent;
			default:
				fParsingErrorMessage = NLS.bind(Messages.ConfigWizardParser_UnknownTokenType,
						fScanner.getTokenContent(cToken));
				syntaxError();
				break;
		}

		return null;
	}

	protected IConfigWizardItem parseHeading(IConfigWizardItem parent) {
		Assert.isTrue(cType == ETokenType.HEADING);

		String tokenContent = fScanner.getTokenContent(cToken);
		if (!tokenContent.toLowerCase().equals("h")) { //$NON-NLS-1$
			fParsingErrorMessage = Messages.ConfigWizardParser_WrongHeadTokenFormat + tokenContent;
			fParsingErrorMessage += Messages.ConfigWizardParser_CorrectTokenFormat + "<h>"; //$NON-NLS-1$
			syntaxError();
			return null;
		}

		IConfigWizardItem heading = new ConfigWizardItem(EItemType.HEADING, fScanner.getCurrentLineNumber(), parent);
		heading.setName(fScanner.readLine());

		heading = parseItems(heading, ETokenType.HEADING_END);

		getNextToken();

		return heading;
	}

	protected IConfigWizardItem parseHeadingEnable(IConfigWizardItem parent) {
		Assert.isTrue(cType == ETokenType.HEADING_ENABLE);

		IConfigWizardItem headingEnable = new ConfigWizardItem(EItemType.HEADING_ENABLE,
				fScanner.getCurrentLineNumber(), parent);
		String tokenContent = fScanner.getTokenContent(cToken);

		headingEnable = parseModifier(headingEnable, tokenContent.substring(1));

		if (headingEnable != null) {
			headingEnable.setName(fScanner.readLine());
			headingEnable = parseItems(headingEnable, ETokenType.HEADING_ENABLE_END);
		}

		getNextToken();

		return headingEnable;
	}

	protected IConfigWizardItem parseCodeComment(IConfigWizardItem parent) {
		Assert.isTrue(cType == ETokenType.CODE_ENABLE || cType == ETokenType.CODE_DISABLE);

		EItemType itemType = EItemType.CODE_ENABLE;
		if (cType == ETokenType.CODE_DISABLE) {
			itemType = EItemType.CODE_DISABLE;
		}
		IConfigWizardItem codeComment = new ConfigWizardItem(itemType, fScanner.getCurrentLineNumber(), parent);
		String tokenContent = fScanner.getTokenContent(cToken);
		String modifier = tokenContent.substring(1);

		if (itemType == EItemType.CODE_DISABLE) {
			codeComment.setInvertValue(true);
			modifier = tokenContent.substring(2);
		}
		codeComment = parseModifier(codeComment, modifier);

		if (codeComment != null) {
			codeComment.setName(fScanner.readLine());
			codeComment = parseItems(codeComment, ETokenType.CODE_END);
		}

		getNextToken();

		return codeComment;
	}

	protected IConfigWizardItem parseModifier(IConfigWizardItem item, String modifier) {
		item.setMinBit(0);
		item.setMaxBit(Long.SIZE - 1);
		item.setSkipNumber(0);
		item.setMinValue(Long.MIN_VALUE);
		item.setMaxValue(Long.MAX_VALUE);
		item.setBase(16);

		if (modifier.length() == 0) {
			return item;
		}
		if (modifier.startsWith(".")) { //$NON-NLS-1$
			modifier = "0" + modifier; //$NON-NLS-1$
		}
		if (!modifier.matches("\\d+((\\.\\d+)?|(\\.\\d+(\\.\\.\\d+)?))")) { //$NON-NLS-1$
			fParsingErrorMessage = Messages.ConfigWizardParser_WrongModificationFormat + modifier;
			fParsingErrorMessage += Messages.ConfigWizardParser_CorrectTokenFormat
					+ "\\d+((\\.\\d+)?|(\\.\\d+(\\.\\.\\d+)?))"; //$NON-NLS-1$
			syntaxError();
			return null;
		}
		try {
			int dotPos = modifier.indexOf('.');
			if (dotPos == -1) {
				dotPos = modifier.length();
			}
			int skipNumber = Integer.parseInt(modifier.substring(0, dotPos));
			item.setSkipNumber(skipNumber);

			if (dotPos == modifier.length()) {
				return item;
			}

			int ddotPos = modifier.indexOf(".."); //$NON-NLS-1$
			if (ddotPos == -1) {
				ddotPos = modifier.length();
			}
			int minBit = Integer.parseInt(modifier.substring(dotPos + 1, ddotPos));
			item.setMinBit(minBit);
			item.setMaxBit(minBit);

			if (ddotPos == modifier.length()) {
				if (item.getItemType() == EItemType.OPTION) {
					item.setItemType(EItemType.OPTION_CHECK);
				}
				return item;
			}

			int maxBit = Integer.parseInt(modifier.substring(ddotPos + 2, modifier.length()));
			if (maxBit < minBit) {
				maxBit = minBit;
			}
			item.setMaxBit(maxBit);

			if (item.getMinBit() == item.getMaxBit() && item.getItemType() == EItemType.OPTION) {
				item.setItemType(EItemType.OPTION_CHECK);
			}

		} catch (NumberFormatException e) {
			fParsingErrorMessage = Messages.ConfigWizardParser_FailToParseModifierNumber + modifier;
			syntaxError();
			return null;
		}
		return item;
	}

	protected IConfigWizardItem parseOption(IConfigWizardItem item) {
		Assert.isTrue(cType == ETokenType.OPTION || cType == ETokenType.OPTION_CHECK);

		String tokenContent = fScanner.getTokenContent(cToken);
		item = parseModifier(item, tokenContent.substring(1));

		if (item != null) {
			String name = fScanner.readString();
			item.setName(name);

			getNextToken();
			while (cType == ETokenType.TOOLTIP) {
				parseTooltip(item);
			}

			if (cType != ETokenType.EOC) {
				tokenContent = fScanner.getTokenContent(cToken);
				if (parseRangeOrSelection(item, tokenContent) == null) {
					return null;
				}

				if (!item.isSelection()) {
					tokenContent = fScanner.getTokenContent(cToken);
					if (parseModification(item, tokenContent) == null) {
						return null;
					}
				} else {
					item.setItemType(EItemType.OPTION_SELECT);
				}
			}
		}

		return item;
	}

	// jump to next token afterwards if cToken is a range or selection token
	protected IConfigWizardItem parseRangeOrSelection(IConfigWizardItem item, String rangeOrSelection) {
		item.setBase(16);
		int radix = 16;

		String numRegex = "(0[xX][0-9a-fA-F]+|\\d+)"; //$NON-NLS-1$
		String rangeRegex = numRegex + "-" + numRegex + "(:" + numRegex + ")?"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		String selectRegex = numRegex + "="; //$NON-NLS-1$
		if (!rangeOrSelection.matches(rangeRegex) && !rangeOrSelection.matches(selectRegex)) {
			return item;
		}

		if (!rangeOrSelection.toLowerCase().contains("0x")) { //$NON-NLS-1$
			radix = 10;
			item.setBase(10);
			item.setMinValue(Long.MIN_VALUE);
		}

		try {
			if (rangeOrSelection.matches(rangeRegex)) {

				rangeOrSelection = rangeOrSelection.toLowerCase().replace("0x", ""); //$NON-NLS-1$ //$NON-NLS-2$

				int hypPos = rangeOrSelection.indexOf('-');
				long minValue = Long.parseLong(rangeOrSelection.substring(0, hypPos), radix);
				item.setMinValue(minValue);
				item.setMaxValue(minValue);

				int colPos = rangeOrSelection.indexOf(':');
				if (colPos == -1) {
					colPos = rangeOrSelection.length();
				}
				long maxValue = Long.parseLong(rangeOrSelection.substring(hypPos + 1, colPos), radix);
				item.setMaxValue(maxValue);

				if (colPos < rangeOrSelection.length()) {
					long spinStep = Long.parseLong(rangeOrSelection.substring(colPos + 1), radix);
					item.setSpinStep(spinStep);
				}
				// The next line added just to be consistent with the
				// selection part, so that
				// after this method the token is already the next one
				getNextToken();
			} else {
				item = parseSelection(item);
				return item;
			}

			return item;
		} catch (NumberFormatException e) {
			fParsingErrorMessage = Messages.ConfigWizardParser_WrongRangeSelectionFormat + rangeOrSelection;
			fParsingErrorMessage += Messages.ConfigWizardParser_CorrectTokenFormat + rangeRegex
					+ Messages.ConfigWizardParser_Range + selectRegex + Messages.ConfigWizardParser_Selection;
			syntaxError();
			return null;
		}
	}

	// jump to next token afterwards if cToken is a selection token
	protected IConfigWizardItem parseSelection(IConfigWizardItem parent) {
		Assert.isTrue(cType == ETokenType.VALUE);
		parent.setIsSelection(true);
		while (cType == ETokenType.VALUE) {
			String value = fScanner.getTokenContent(cToken);
			int equPos = value.indexOf('=');

			int radix = 10;
			String number = value.substring(0, equPos).toLowerCase();
			if (number.startsWith("0x")) { //$NON-NLS-1$
				radix = 16;
				number = number.substring(2);
			}
			long key = Long.parseLong(number, radix);
			String name = fScanner.readString();
			parent.addItem(key, name);

			getNextToken();
		}

		if (cType == ETokenType.UNKNOWN) {
			fParsingErrorMessage = NLS.bind(Messages.ConfigWizardParser_WrongSelectionToken,
					fScanner.getTokenContent(cToken));
			fParsingErrorMessage += Messages.ConfigWizardParser_CorrectTokenFormat
					+ getTokenTypeString(ETokenType.VALUE);
			syntaxError();
			return null;
		}

		return parent;
	}

	protected IConfigWizardItem parseModification(IConfigWizardItem item, String modification) {
		String regex = "#(\\+|-|\\*|/)(0[xX]|)\\d+"; //$NON-NLS-1$
		if (modification.matches(regex)) {
			try {
				char m = modification.charAt(1);
				item.setModification(m);
				int radix = 10;
				String number = modification.substring(2).toLowerCase();
				if (number.startsWith("0x")) { //$NON-NLS-1$
					radix = 16;
					number = number.substring(2);
				}
				long modifier = Long.parseLong(number, radix);
				if ((m == '/' || m == '*') && modifier == 0) {
					modifier = 1;
				}
				item.setModifier(modifier);

				getNextToken();
			} catch (NumberFormatException e) {
				fParsingErrorMessage = Messages.ConfigWizardParser_WrongModificationFormat + modification;
				fParsingErrorMessage += Messages.ConfigWizardParser_CorrectTokenFormat + regex;
				syntaxError();
				return null;
			}
		}
		return item;
	}

	protected IConfigWizardItem parseTooltip(IConfigWizardItem item) {
		Assert.isTrue(cType == ETokenType.TOOLTIP);
		if (item == null) {
			fParsingErrorMessage = Messages.ConfigWizardParser_TooltipForUnknownConfigItem;
			syntaxError();
			return null;
		}
		String moreTooltip = fScanner.readLine();
		item.extendTooltip(moreTooltip);
		getNextToken();
		return item;
	}

	protected IConfigWizardItem parseOptionString(IConfigWizardItem item) {
		Assert.isTrue(cType == ETokenType.OPTION_STRING);
		String tokenContent = fScanner.getTokenContent(cToken);
		item = parseStringModifier(item, tokenContent.substring(1));
		if (item != null) {
			String name = fScanner.readString();
			item.setName(name);

			getNextToken();
			while (cType == ETokenType.TOOLTIP) {
				parseTooltip(item);
			}
		}

		return item;
	}

	protected IConfigWizardItem parseStringModifier(IConfigWizardItem item, String modifier) {
		if (modifier.length() == 0) {
			return item;
		}
		if (modifier.startsWith(".")) { //$NON-NLS-1$
			modifier = "0" + modifier; //$NON-NLS-1$
		}

		if (!modifier.matches("\\d+(\\.\\d+)?")) { //$NON-NLS-1$
			fParsingErrorMessage = Messages.ConfigWizardParser_WrongModifierFormat + modifier;
			fParsingErrorMessage += Messages.ConfigWizardParser_CorrectTokenFormat + "\\d+(\\.\\d+)?"; //$NON-NLS-1$
			syntaxError();
			return null;
		}

		try {
			int dotPos = modifier.indexOf('.');
			if (dotPos == -1) {
				dotPos = modifier.length();
			}
			int skipNumber = Integer.parseInt(modifier.substring(0, dotPos));
			item.setSkipNumber(skipNumber);

			if (dotPos == modifier.length()) {
				return item;
			}

			int length = Integer.parseInt(modifier.substring(dotPos + 1));
			item.setStringLength(length);
		} catch (NumberFormatException e) {
			fParsingErrorMessage = Messages.ConfigWizardParser_FailToParseModifierNumber + modifier;
			syntaxError();
			return null;
		}

		return item;
	}

	protected void parseNumber() {
		String tokenContent = fScanner.getTokenContent(cToken);
		fNumberContainer.put(fScanner.getTokenOffset(), tokenContent);
		getNextToken();
	}

	protected void parseString() {
		String tokenContent = fScanner.getTokenContent(cToken);
		fStringContainer.put(fScanner.getTokenOffset() + 1, tokenContent.substring(1, tokenContent.length() - 1));
		getNextToken();
	}

	private long parseNumber(IConfigWizardItem item, String number) {
		int radix = 10;
		number = number.toLowerCase();
		if (number.startsWith("0x")) { //$NON-NLS-1$
			radix = 16;
			number = number.substring(2);
		} else if (number.startsWith("0b")) { //$NON-NLS-1$
			radix = 2;
			number = number.substring(2);
		} else if (number.startsWith("0") && number.length() > 1 && //$NON-NLS-1$
				Character.isDigit(number.charAt(number.length() - 1))) {
			radix = 8;
			number = number.substring(1);
		}
		char lastChar = number.charAt(number.length() - 1);
		if (radix != 16 && radix != 2 && radix != 8 && !Character.isDigit(lastChar)) {
			switch (lastChar) {
				case 'b':
				case 'B':
					radix = 2;
					break;
				case 'o':
				case 'O':
				case 'q':
				case 'Q':
					radix = 8;
					break;
				case 'd':
				case 'D':
					radix = 10;
					break;
				case 'h':
				case 'H':
					radix = 16;
					break;
				default:
					item.setItemErrorType(EItemErrorType.NUMBER_PARSE_ERROR);
					return 0;
			}
			number = number.substring(0, number.length() - 1);
		}
		// For 2,8,16 based numbers, min value could not be negative
		if (radix == 2 || radix == 8 || radix == 16) {
			if (item.getMinValue() <= 0) {
				item.setMinValue(0);
			}
		}
		try {
			long i = Long.parseLong(number, radix);
			item.setItemErrorType(EItemErrorType.NO_ERROR);
			return i;
		} catch (NumberFormatException e) {
			item.setItemErrorType(EItemErrorType.NUMBER_PARSE_ERROR);
			return 0;
		}
	}

	protected void syntaxError() {
		fParsingErrorOffset = fScanner.getTokenOffset();
		int line = fScanner.getCurrentLineNumber();
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(),
						Messages.ConfigWizardParser_ErrorInConfigWizard,
						Messages.ConfigWizardParser_SyntaxErrorAtLine + (line + 1) + ": " + fParsingErrorMessage); //$NON-NLS-1$
			}
		});
	}

	private IConfigWizardItem analyseCodeContent(IConfigWizardItem item) {
		try {
			int skip = item.getSkipNumber() + 1;
			int startLine = item.getStartLine() + skip;
			int startOffset = fDocument.getLineOffset(startLine);
			int endLine = item.getEndLine();
			int endOffset = fDocument.getLineOffset(endLine);
			if (startLine >= endLine) {
				fParsingErrorMessage = Messages.ConfigWizardParser_SkipTooManyLines;
				syntaxError();
				return null;
			}
			String codeContent = fDocument.get(startOffset, endOffset - startOffset);
			String[] lines = codeContent.split("\\r?\\n"); //$NON-NLS-1$
			Set<Boolean> lineCommented = new HashSet<>();
			for (String line : lines) {
				if (line.trim().startsWith("//") || line.trim().startsWith("/*")) { //$NON-NLS-1$ //$NON-NLS-2$
					lineCommented.add(true);
					if (line.trim().startsWith("//")) { //$NON-NLS-1$
						int moreOffset = line.indexOf("//"); //$NON-NLS-1$
						int slashLength = 2;
						while (line.length() > moreOffset + slashLength
								&& line.charAt(moreOffset + slashLength) == '/') {
							slashLength++;
						}
						int slashBegin = startOffset + moreOffset;
						fCommentContainer.put(slashBegin, fDocument.get(slashBegin, slashLength));
					}
				} else {
					int spaceCount = 0;
					for (char c : line.toCharArray()) {
						if (c == ' ') {
							spaceCount++;
						} else {
							break;
						}
					}
					fCommentContainer.put(startOffset + spaceCount, ""); //$NON-NLS-1$
					lineCommented.add(false);
				}
				startLine++;
				startOffset = fDocument.getLineOffset(startLine);
			}
			if (lineCommented.size() > 1) {
				item.setInconsistent(true);
			}
			if (lineCommented.contains(false)) {
				item.setValue(1);
			} else {
				item.setValue(0);
			}
			if (item.invertValue()) {
				item.setValue(1 - item.getValue());
			}
		} catch (BadLocationException e) {
		}
		return item;
	}

	private String getTokenTypeString(ETokenType type) {
		switch (type) {
			case HEADING:
				return "<h>"; //$NON-NLS-1$
			case HEADING_ENABLE:
				return "<e>"; //$NON-NLS-1$
			case HEADING_ENABLE_END:
				return "</e>"; //$NON-NLS-1$
			case HEADING_END:
				return "</h>"; //$NON-NLS-1$
			case OPTION:
				return "<o>"; //$NON-NLS-1$
			case OPTION_CHECK:
				return "<q>"; //$NON-NLS-1$
			case OPTION_STRING:
				return "<s>"; //$NON-NLS-1$
			case TOOLTIP:
				return "<i>"; //$NON-NLS-1$
			case VALUE:
				return "<\\d+=>"; //$NON-NLS-1$
			case EOC:
				return Messages.ConfigWizardParser_EndOfConfiguration;
			case BLOCK_COMMENT:
				return Messages.ConfigWizardParser_BlockComment;
			case COMMENT:
				return Messages.ConfigWizardParser_Comment;
			case DEFAULT:
				return Messages.ConfigWizardParser_Unknown;
			case NUMBER:
				return Messages.ConfigWizardParser_Number;
			case STRING:
				return Messages.ConfigWizardParser_String;
			default:
				return Messages.ConfigWizardParser_Unknown;
		}
	}

	private void setValuesAndStrings() {
		try {
			if (fRoot.hasChildren()) {
				for (IConfigWizardItem child : fRoot.getChildren()) {
					setValuesAndStrings(child);
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	private void setValuesAndStrings(IConfigWizardItem item) throws BadLocationException {
		int line = item.getStartLine();
		int offset = fDocument.getLineOffset(line);
		int skip = item.getSkipNumber();
		int minBit = item.getMinBit();
		int maxBit = item.getMaxBit();
		long mask = buildMask(minBit, maxBit);

		EItemType type = item.getItemType();
		switch (type) {
			case HEADING_ENABLE:
			case OPTION:
			case OPTION_CHECK:
			case OPTION_SELECT:
				Collection<String> numbers = fNumberContainer.tailMap(offset).values();
				Iterator<String> iter = numbers.iterator();
				String valueText = ""; //$NON-NLS-1$
				long value = 0;
				while (iter.hasNext() && skip >= 0) {
					valueText = iter.next();
					skip--;
				}
				if (skip >= 0) {
					item.setItemErrorType(EItemErrorType.LOCATE_POSITION_ERROR);
					return;
				}
			value = parseNumber(item, valueText);
			if (item.getItemErrorType() != EItemErrorType.NO_ERROR) {
				return;
			}
			int base = getBaseFromValue(valueText);
			item.setBase(base);
			long realValue = value;
			if (value >= 0) {
				realValue = (value >> minBit) & mask;
			}
			item.setValue(realValue);
				break;
			case OPTION_STRING:
				Collection<String> strings = fStringContainer.tailMap(offset).values();
				Iterator<String> siter = strings.iterator();
				String str = ""; //$NON-NLS-1$
				while (siter.hasNext() && skip >= 0) {
					str = siter.next();
					skip--;
				}
				if (skip >= 0) {
					item.setItemErrorType(EItemErrorType.LOCATE_POSITION_ERROR);
					return;
				}
			item.setItemErrorType(EItemErrorType.NO_ERROR);
			item.setString(str);
				break;
			default:
				break;
		}

		if (item.hasChildren()) {
			for (IConfigWizardItem child : item.getChildren()) {
				setValuesAndStrings(child);
			}
		}
	}

	private long buildMask(int minBit, int maxBit) {
		long mask = 1;
		for (int i = 0; i < maxBit - minBit; i++) {
			mask <<= 1;
			mask |= 1;
		}
		return mask;
	}

	private int getBaseFromValue(String valueText) {
		valueText = valueText.toLowerCase();
		if (valueText.startsWith("0x")) { //$NON-NLS-1$
			return 16;
		} else if (valueText.startsWith("0b")) { //$NON-NLS-1$
			return 2;
		} else if (valueText.startsWith("0") && valueText.length() > 1 && //$NON-NLS-1$
				Character.isDigit(valueText.charAt(valueText.length() - 1))) {
			return 8;
		}
		switch (valueText.charAt(valueText.length() - 1)) {
			case 'b':
				return 2;
			case 'o':
			case 'q':
				return 8;
			case 'd':
				return 10;
			case 'h':
				return 16;
			default:
				return 10;
		}
	}

	private void getNextToken() {
		cToken = fScanner.nextToken();
		cType = fScanner.getTokenType(cToken);
	}

	public IConfigWizardItem getConfigWizardRoot() {
		return fRoot;
	}

	public boolean containWizard() {
		return fContainWizard;
	}

	public int getStartParseOffset() {
		return fStartParseOffset;
	}

	public int getEndParseOffset() {
		return fEndParseOffset;
	}

	public int getParsingErrorOffset() {
		return fParsingErrorOffset;
	}

	public void updateModel(IConfigWizardItem item, Object newVal) {
		if (newVal instanceof Boolean) {
			updateBooleanValue(item, (Boolean) newVal);
		} else if (newVal instanceof Integer || newVal instanceof Long) {
			updateNumberValue(item, (Long) newVal);
		} else if (newVal instanceof String) {
			updateStringValue(item, (String) newVal);
		}
	}

	private void updateBooleanValue(IConfigWizardItem item, Boolean newVal) {
		long value = newVal.booleanValue() ? 1 : 0;
		if (item.getValue() == value) {
			return;
		}
		item.setValue(value);
		if (item.invertValue()) {
			newVal = !newVal.booleanValue();
		}
		updateDocument(item, newVal);
	}

	private void updateNumberValue(IConfigWizardItem item, Long newVal) {
		long value = newVal.longValue();
		if (item.getItemType() == EItemType.OPTION_SELECT) { // For Menu Item
			int i = 0;
			for (Long k : item.getItems().keySet()) {
				if (i == value) {
					if (item.getValue() == k) {
						return;
					}
					item.setValue(k);
					updateDocument(item, k);
					return;
				}
				i++;
			}
		} else { // For Spinner Item
			long min = item.getMinValue();
			long modifier = item.getModifier();
			char op = item.getModification();
			value = Utils.modifyValue(value, op, modifier,
					item.getMaxValue(), item.getMinValue());
			long spinStep = Utils.modifyValue(item.getSpinStep(), op, modifier,
					Long.MAX_VALUE, 1);
			if ((value - min) % spinStep != 0) {
				value = min + (value - min) / spinStep * spinStep;
			}
			if (item.getValue() == value) {
				return;
			}
			item.setValue(value);
			updateDocument(item, value);
			return;
		}
	}

	private void updateStringValue(IConfigWizardItem item, String newVal) {
		EItemType type = item.getItemType();
		String value = newVal;
		switch (type) {
			case OPTION_STRING:
				value = value.replace("\"", "\\\""); //$NON-NLS-1$ //$NON-NLS-2$
				if (item.getString().equals(value)) {
					return;
				}
				item.setString(value);
				updateDocument(item, value);
				return;
			case OPTION_SELECT:
				if (item.getItems().get(item.getValue()) != null
						&& item.getItems().get(item.getValue()).equals(value)) {
					return;
				}
				for (Entry<Long, String> entry : item.getItems().entrySet()) {
					if (entry.getValue().equals(value)) {
						item.setValue(entry.getKey());
						updateDocument(item, entry.getKey());
						return;
					}
				}
				break;
			case OPTION:
				int radix = item.getBase();
				if (value.toLowerCase().startsWith("0x")) { //$NON-NLS-1$
					value = value.substring(2);
					radix = 16;
				}
				value = value.replaceAll("[\\s<>]",""); //$NON-NLS-1$ //$NON-NLS-2$
				try {
					long realValue = Utils.modifyValue(Long.parseLong(value, radix),
							item.getModification(), item.getModifier(),
							item.getMaxValue(), item.getMinValue());
					item.setItemErrorType(EItemErrorType.NO_ERROR);
					if (item.getValue() == realValue) {
						return;
					}
					item.setValue(realValue);
					updateDocument(item, realValue);
					return;
				} catch (NumberFormatException e) {
					item.setItemErrorType(EItemErrorType.NUMBER_PARSE_ERROR);
				}
				break;
			default:
				break;
		}
	}

	private void updateDocument(IConfigWizardItem item, Object newVal) {
		try {
			int startLine = item.getStartLine();
			int endLine = item.getEndLine();
			int startOffset = fDocument.getLineOffset(startLine);
			int endOffset = fDocument.getLineOffset(endLine);
			int skip = item.getSkipNumber();

			// Comment Check Box
			if (item.getItemType() == EItemType.CODE_ENABLE || item.getItemType() == EItemType.CODE_DISABLE) {
				// newVal == true -> uncomment, newVal == false -> comment
				Assert.isTrue(newVal instanceof Boolean);

				boolean useCode = ((Boolean) newVal).booleanValue();

				startOffset = fDocument.getLineOffset(startLine + skip + 1);
				Iterator<Entry<Integer, String>> iter = fCommentContainer.subMap(startOffset, endOffset).entrySet()
						.iterator();
				TreeMap<Integer, String> newMap = new TreeMap<>();
				int shift = 0;
				while (iter.hasNext()) {
					Entry<Integer, String> e = iter.next();
					int offset = e.getKey() + shift;
					String oldComment = e.getValue();
					iter.remove();
					if (useCode) { // should uncomment
						fDocument.replace(offset, oldComment.length(), ""); //$NON-NLS-1$
						shift -= oldComment.length();
						newMap.put(offset, ""); //$NON-NLS-1$
					} else {
						fDocument.replace(offset, oldComment.length(), "//"); //$NON-NLS-1$
						shift -= oldComment.length() - 2;
						newMap.put(offset, "//"); //$NON-NLS-1$
					}
				}
				fCommentContainer.putAll(newMap);
				if (shift != 0) {
					updateIndex(endOffset, shift);
				}
			}
			// Heading_enable, Option, Option_enable, Option_select
			else if (newVal instanceof Integer || newVal instanceof Long || newVal instanceof Boolean) {
				// find the offset of the value corresponding to the item
				Set<Entry<Integer, String>> map = fNumberContainer.tailMap(startOffset).entrySet();
				int i = 0;
				int valueOffset = -1;
				String oldValue = ""; //$NON-NLS-1$
				int oldValueLength = 0;
				for (Entry<Integer, String> e : map) {
					if (i == skip) {
						valueOffset = e.getKey();
						oldValue = e.getValue();
						oldValueLength = oldValue.length();
						break;
					}
					i++;
				}
				Assert.isTrue(valueOffset != -1 && !oldValue.isEmpty());

				int minBit = item.getMinBit();
				int maxBit = item.getMaxBit();
				long mask = ~(buildMask(minBit, maxBit) << minBit);
				long value;
				if (newVal instanceof Boolean) {
					value = (Boolean) newVal ? 1 : 0;
				} else {
					value = (Long.valueOf(newVal.toString()));
				}
				value <<= minBit;
				long newValue = parseNumber(item, oldValue);
				if (item.getItemErrorType() != EItemErrorType.NO_ERROR) {
					return;
				}
				newValue &= mask;
				newValue |= value;

				String newText = String.valueOf(newValue);
				if (item.getBase() == 16) {
					newText = Long.toHexString(newValue).toUpperCase();
					StringBuilder sb = new StringBuilder(newText);
					sb.insert(0, "0x"); //$NON-NLS-1$
					while (sb.length() < oldValueLength) {
						sb.insert(2, '0');
					}
					newText = sb.toString();
				} else if (item.getBase() == 8) {
					newText = Long.toOctalString(newValue);
					newText = "0" + newText; //$NON-NLS-1$
				} else if (item.getBase() == 2) {
					newText = Long.toBinaryString(newValue);
					StringBuilder sb = new StringBuilder(newText);
					sb.insert(0, "0b"); //$NON-NLS-1$
					while (sb.length() < oldValueLength) {
						sb.insert(2, '0');
					}
					newText = sb.toString();
				}
				updateIndex(valueOffset, newText.length() - oldValueLength);
				fNumberContainer.remove(valueOffset);
				fNumberContainer.put(valueOffset, newText);
				fDocument.replace(valueOffset, oldValueLength, newText);
			}
			// Option_string
			else if (newVal instanceof String) {
				// find the offset of the value corresponding to the item
				Set<Entry<Integer, String>> map = fStringContainer.tailMap(startOffset).entrySet();
				int i = 0;
				int valueOffset = -1;
				String oldValue = ""; //$NON-NLS-1$
				int oldValueLength = 0;
				for (Entry<Integer, String> e : map) {
					if (i == skip) {
						valueOffset = e.getKey();
						oldValue = e.getValue();
						oldValueLength = oldValue.length();
						break;
					}
					i++;
				}
				Assert.isTrue(valueOffset != -1 && !oldValue.isEmpty());

				String newText = (String) newVal;

				updateIndex(valueOffset, newText.length() - oldValueLength);
				fStringContainer.remove(valueOffset);
				fStringContainer.put(valueOffset, newText);
				// consider the double quote here
				fDocument.replace(valueOffset - 1, oldValueLength + 2, "\"" + newText + "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * update the index that is > startOffset by shift characters
	 *
	 * @param startOffset
	 *            starting offset
	 * @param shift
	 *            number of characters to shift
	 */
	private void updateIndex(int startOffset, int shift) {
		if (shift == 0) {
			return;
		}
		updateContainer(fNumberContainer, startOffset, shift);
		updateContainer(fStringContainer, startOffset, shift);
		updateContainer(fCommentContainer, startOffset, shift);
	}

	private void updateContainer(TreeMap<Integer, String> container, int startOffset, int shift) {
		SortedMap<Integer, String> behind = container.tailMap(startOffset, false);
		TreeMap<Integer, String> newMap = new TreeMap<>();
		Iterator<Entry<Integer, String>> iter = behind.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, String> entry = iter.next();
			int key = entry.getKey();
			String value = entry.getValue();
			newMap.put(key + shift, value);
			iter.remove();
		}
		container.putAll(newMap);
	}

}
