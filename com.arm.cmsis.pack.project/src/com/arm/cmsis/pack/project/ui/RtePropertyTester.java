package com.arm.cmsis.pack.project.ui;

import org.eclipse.core.expressions.PropertyTester;
import com.arm.cmsis.pack.project.utils.ProjectUtils;

public class RtePropertyTester extends PropertyTester {
	
	public RtePropertyTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (property.equalsIgnoreCase("rteFile")) { //$NON-NLS-1$
			return ProjectUtils.getRteFileResource(receiver) != null;
		}	
		return false;
	}
}
	
