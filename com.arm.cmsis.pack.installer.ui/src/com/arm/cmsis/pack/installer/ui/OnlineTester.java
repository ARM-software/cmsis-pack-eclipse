package com.arm.cmsis.pack.installer.ui;

import org.eclipse.core.expressions.PropertyTester;

public class OnlineTester extends PropertyTester {

	public OnlineTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		return CpInstallerPlugInUI.isOnline();
	}

}
