package com.arm.cmsis.zone.ui.wizards;

import org.eclipse.ui.INewWizard;

import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.ui.wizards.OkWizard;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;

/**
 * Wizard to create a new item or edit existing item properties
 * 
 */

public abstract class CmsisZoneAbstractWizard<T extends ICpItem> extends OkWizard implements INewWizard {
    private CmsisZoneAbstractWizardPage<T> page;
    CmsisZoneController fController;
    ICpRootZone fRootZone;
    T fExistingItem;

    /**
     * Constructor for CmsisZoneAbstractWizard.
     */
    public CmsisZoneAbstractWizard(CmsisZoneController controller) {
        this(controller, null);
    }

    /**
     * Constructor for CmsisZoneAbstractWizard.
     */
    public CmsisZoneAbstractWizard(CmsisZoneController controller, T existingItem) {
        super();
        fController = controller;
        fExistingItem = existingItem;
        setNeedsProgressMonitor(false);
    }

    public T getExistingItem() {
        return fExistingItem;
    }

    @Override
    public void addPages() {
        page = createPage();
        addPage(page);
    }

    /**
     * Creates wizard's page
     * 
     * @return CmsisZoneAbstractWizardPage<T>
     */
    protected abstract CmsisZoneAbstractWizardPage<T> createPage();

    @Override
    public boolean performFinish() {
        return page.apply();
    }

    @Override
    public boolean isShowOkButton() {
        return getExistingItem() != null;
    }

}