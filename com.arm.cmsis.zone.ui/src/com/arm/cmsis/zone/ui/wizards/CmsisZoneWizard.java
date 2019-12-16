package com.arm.cmsis.zone.ui.wizards;

import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;

/**
 * Wizard to create a new item or edit existing item properties 
 * 
 */

public class CmsisZoneWizard extends CmsisZoneAbstractWizard<ICpZone> {
	
	/**
	 * Constructor for CmsisProjectZoneWizard.
	 */
	public CmsisZoneWizard(CmsisZoneController controller){
		this(controller, null);
	}
	
	/**
	 * Constructor for CmsisProjectZoneWizard.
	 */
	public CmsisZoneWizard(CmsisZoneController controller, ICpZone existingZone){
		super(controller, existingZone);
		if(fExistingItem == null) {
			setWindowTitle(Messages.CmsisZoneWizard_NewCmsis + controller.getZoneLabel());
		} else {
			setWindowTitle(Messages.CmsisZoneWizard_Cmsis + controller.getZoneLabel() + Messages.CmsisZoneWizard_Properties);
		}
		
	}
	
	@Override
	protected CmsisZoneWizardPage createPage(){
		return new CmsisZoneWizardPage(fController, getExistingItem());
	}
	
}