package com.arm.cmsis.parser;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.SingleLineRule;

public class CommentRule extends SingleLineRule {

	public CommentRule(String startSequence, IToken token) {
		super(startSequence, null, token);
	}

	@Override
	protected boolean endSequenceDetected(ICharacterScanner scanner) {
		return true;
	}

}
