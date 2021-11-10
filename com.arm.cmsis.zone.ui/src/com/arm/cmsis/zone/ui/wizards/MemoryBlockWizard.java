package com.arm.cmsis.zone.ui.wizards;

import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpPeripheral;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;

/**
 * Wizard to create a new item or edit existing item properties
 * 
 */

public class MemoryBlockWizard extends CmsisZoneAbstractWizard<ICpMemoryBlock> {

    private ICpMemoryBlock fParentBlock = null;

    /**
     * Constructor for CmsisProjectZoneWizard.
     */
    public MemoryBlockWizard(CmsisZoneController controller, ICpMemoryBlock parentBlock) {
        this(controller, parentBlock, null);
    }

    /**
     * Constructor for CmsisProjectZoneWizard.
     */
    public MemoryBlockWizard(CmsisZoneController controller, ICpMemoryBlock parentBlock, ICpMemoryBlock existingBlock) {
        super(controller, existingBlock);
        fParentBlock = parentBlock;
        setNeedsProgressMonitor(false);
        if (fExistingItem == null) {
            setWindowTitle(Messages.MemoryBlockWizard_AddMemoryRegion);
        } else if (fExistingItem instanceof ICpPeripheral) {
            setWindowTitle(Messages.MemoryBlockWizard_PeripheralProperties);
        } else {
            setWindowTitle(Messages.MemoryBlockWizard_MemoryRegionProperties);
        }
    }

    @Override
    protected MemoryBlockWizardPage createPage() {
        return new MemoryBlockWizardPage(fController, getParentBlock(), getExistingItem());
    }

    public ICpMemoryBlock getParentBlock() {
        return fParentBlock;
    }

}