package com.arm.cmsis.zone.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;

import com.arm.cmsis.pack.project.utils.ProjectUtils;
import com.arm.cmsis.zone.project.CmsisZoneProjectNature;

public class ZoneNaturePropertyTester extends PropertyTester {

    public ZoneNaturePropertyTester() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        IResource res = ProjectUtils.getResource(receiver);
        return CmsisZoneProjectNature.hasCmsisZoneNature(res);
    }

}
