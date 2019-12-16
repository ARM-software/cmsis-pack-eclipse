/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.project;

import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.PlatformUI;

import com.arm.cmsis.pack.enums.ESeverity;
import com.arm.cmsis.pack.error.CmsisError;
import com.arm.cmsis.pack.error.CmsisErrorCollection;
import com.arm.cmsis.pack.item.ICmsisVisitor.VisitResult;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.zone.data.ICpMemoryRegion;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.data.ICpZoneItem;
import com.arm.cmsis.zone.error.CmsisZoneError;
import com.arm.cmsis.zone.ui.CpZonePluginUI;

/**
 *  A class to validate CMSIS Zone files/models  
 */
public class CmsisZoneValidator extends CmsisErrorCollection implements ICmsisZoneValidator {
	
	@Override
	public boolean validate(ICpRootZone rootZone) {
		clearErrors();
		if(rootZone == null)
			return false;
		
		collectErrors(rootZone);
		updateMarkers(rootZone);
		
		return getSevereErrorCount() == 0;
	}

	protected void collectErrors(ICpRootZone rootZone) {

		// update startup region warnings
		checkStartupRegions(rootZone); 
		// collect already existing errors discovered during parsing and initialization
		rootZone.accept((item) -> {
			if(item instanceof ICpZoneItem) {
				ICpZoneItem zItem = (ICpZoneItem)item;
				Collection<CmsisError> errors = zItem.getErrors();
				if(errors != null && !errors.isEmpty()) {
					addErrors(zItem);
				}
			}
			return VisitResult.CONTINUE;
		});
	}
	
	protected void checkStartupRegions(ICpRootZone rootZone) {
		// clear existing errors
		rootZone.clearErrors(CmsisZoneError.Z12_MASK);
		Collection<ICpMemoryRegion> allRegions = rootZone.getResources().getStarupMemoryRegions();
		for(ICpMemoryRegion r : allRegions) {
			r.clearErrors(CmsisZoneError.Z12_MASK);
		}
		if(rootZone.isZoneModeMPU()) { 
			// check if startup defined more than once
			Collection<ICpMemoryRegion> startupRegions = rootZone.getResources().getStarupMemoryRegions();
			if(startupRegions.size() <= 0) {
				rootZone.addError(new CmsisZoneError(ESeverity.Warning, CmsisZoneError.Z120));
			} else if(startupRegions.size() > 1) {
				rootZone.addError(new CmsisZoneError(ESeverity.Warning, CmsisZoneError.Z121));
				for(ICpMemoryRegion r : startupRegions) {
					r.addError(new CmsisZoneError(ESeverity.Warning, CmsisZoneError.Z121));
				}
			}
		}
	}
	
	protected void updateMarkers(ICpRootZone rootZone) {
		if(!PlatformUI.isWorkbenchRunning())
			return; // no markers in non-UI
		IFile aFile = CpPlugInUI.getFileForLocation(rootZone.getRootFileName());
		if(aFile == null) {
			return; // outside workspace? 
		}
		CpZonePluginUI.removeCmsisZoneMarkers(aFile);
		IFile rFile = CpPlugInUI.getFileForLocation(rootZone.getResourceFileName());
		if(rFile != null) {
			CpZonePluginUI.removeCmsisZoneMarkers(rFile); 
		}
		
		CpZonePluginUI.setCmsisZoneMarkers(this);
	}


}
