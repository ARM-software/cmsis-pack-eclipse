/*******************************************************************************
 * Copyright (c) 2017 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 *******************************************************************************/
package com.arm.cmsis.zone.ui.wizards;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.ECoreArchitecture;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;
import com.arm.cmsis.pack.ui.widgets.MemoryPermissionsControl;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpMpuRegion;
import com.arm.cmsis.zone.data.ICpPeripheral;
import com.arm.cmsis.zone.ui.Messages;
import com.arm.cmsis.zone.ui.editors.CmsisZoneController;

public class MemoryBlockWizardPage extends CmsisZoneAbstractWizardPage<ICpMemoryBlock> {
	private Text textName;
	private Label lblName;
	private Label lblInfo;
	private Text textInfo;
	private Label lblSize;
	private Text textSize;
	private Label lblOffset;
	private Text textOffset;
	private MemoryPermissionsControl accessControl;

	private ICpMemoryBlock fParentBlock = null;
	private Label lblTotalSizeValue;
	private Label lblFreeSizeValue;
	private Label lblParentName;
	private Label lblParent;
	private Label lblTotalSize;
	private Label lblFreeSize;
	private Button chkStartup;
	private Button chkAutoOffset;
	private boolean fbInitialized = false;
	private boolean fbFixed = false;
	private Label lblPhysicalAddress;
	private Text  textPhysicalAddress;
	private Label lblParentPhysicalAddress;
	private Label lblParentPhysicalAddressValue;
	private Label lblParentLogicalAddress;
	private Label lblParentLogicalAddressValue;

	private Long parentPhysicalAddress = null;
	private Label lblStarupDescription;
	private Button chkShared;
	private Label lblSharedDescription;
	private Button chkNoInit;
	private Label lblNoInitDecription;
	private Button chkDma;
	private Label lblDmaDescription;


	private boolean bNoInit = false;
	private boolean bStartup = false;
	private Label lblMpuSize;

	/**
	 * @wbp.parser.constructor
	 */
	public MemoryBlockWizardPage() {
		super(Messages.MemoryBlockWizardPage_MemoryBlockProperties);
	}

	/**
	 * Create the wizard.
	 */
	public MemoryBlockWizardPage(CmsisZoneController controller, ICpMemoryBlock parentBlock, ICpMemoryBlock fExistingItem) {
		super(Messages.MemoryBlockWizardPage_MemoryRegionProperties, controller, fExistingItem);
		fParentBlock = parentBlock;
		if(fExistingItem == null) {
			setTitle(Messages.MemoryBlockWizardPage_NewMemoryRegion);
		} else if (fExistingItem instanceof ICpPeripheral) {
			setTitle(Messages.MemoryBlockWizardPage_PeripheralProperties);
		} else {
			setTitle(Messages.MemoryBlockWizardPage_MemoryRegionProperties);
		}
	}

	public ICpMemoryBlock getParentBlock() {
		return fParentBlock;
	}
	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(4, false));

		ModifyListener modifyNameListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				check();
			}
		};


		ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				check();
				updateOffset();
			}
		};

		Label label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));


		accessControl = new MemoryPermissionsControl(container, SWT.NONE);
		accessControl.addListener(SWT.Modify, new Listener(){
			@Override
			public void handleEvent(Event event) {
				permissionsModified();
			}

		});

		accessControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 15));

		lblName = new Label(container, SWT.NONE);
		lblName.setText(Messages.MemoryBlockWizardPage_Name);

		textName = new Text(container, SWT.BORDER);
		textName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		textName.addModifyListener(modifyNameListener);

		lblSize = new Label(container, SWT.NONE);
		lblSize.setText(Messages.MemoryBlockWizardPage_Size);

		textSize = new Text(container, SWT.BORDER);
		textSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textSize.addModifyListener(modifyListener);

		lblMpuSize = new Label(container, SWT.NONE);
		lblMpuSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		lblOffset = new Label(container, SWT.NONE);
		lblOffset.setText(Messages.MemoryBlockWizardPage_Offset);

		textOffset = new Text(container, SWT.BORDER);
		textOffset.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textOffset.addModifyListener(modifyListener);

		chkAutoOffset = new Button(container, SWT.CHECK);
		chkAutoOffset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				autoOffsetChanged();
			}
		});
		chkAutoOffset.setText(Messages.MemoryBlockWizardPage_AutoArrange);

		lblPhysicalAddress = new Label(container, SWT.NONE);
		lblPhysicalAddress.setText(Messages.MemoryBlockWizardPage_PhysicalAddress);
		textPhysicalAddress = new Text(container, SWT.BORDER);
		textPhysicalAddress.setEditable(false);
		textPhysicalAddress.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));

		chkShared = new Button(container, SWT.CHECK);
		chkShared.setEnabled(false);
		chkShared.setText(Messages.MemoryBlockWizardPage_Shared);

		lblSharedDescription = new Label(container, SWT.NONE);
		lblSharedDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblSharedDescription.setText(Messages.MemoryBlockWizardPage_SeveralZonesAccess);
		new Label(container, SWT.NONE);


		chkStartup = new Button(container, SWT.CHECK);
		chkStartup.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		chkStartup.setText(Messages.MemoryBlockWizardPage_Startup);

		lblStarupDescription = new Label(container, SWT.NONE);
		lblStarupDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		lblStarupDescription.setText(Messages.MemoryBlockWizardPage_VectorTableLocation);

		chkNoInit = new Button(container, SWT.CHECK);
		chkNoInit.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		chkNoInit.setText(Messages.MemoryBlockWizardPage_NoInit);

		lblNoInitDecription = new Label(container, SWT.NONE);
		lblNoInitDecription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		lblNoInitDecription.setText(Messages.MemoryBlockWizardPage_NoInitialization);

		chkDma = new Button(container, SWT.CHECK);
		chkDma.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		chkDma.setText(Messages.MemoryBlockWizardPage_DMA);

		lblDmaDescription = new Label(container, SWT.NONE);
		lblDmaDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));
		lblDmaDescription.setText(Messages.MemoryBlockWizardPage_DMAMemoryAccess);

		Label separator1 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gd_separator1 = new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1);
		gd_separator1.widthHint = 281;
		separator1.setLayoutData(gd_separator1);

		lblParent = new Label(container, SWT.NONE);
		lblParent.setText(Messages.MemoryBlockWizardPage_Parent);

		lblParentName = new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);

		lblParentLogicalAddress = new Label(container, SWT.NONE);
		lblParentLogicalAddress.setText(Messages.MemoryBlockWizardPage_LogicalAddress);
		lblParentLogicalAddressValue = new Label(container, SWT.NONE);
		lblParentLogicalAddressValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(container, SWT.NONE);

		lblParentPhysicalAddress = new Label(container, SWT.NONE);
		lblParentPhysicalAddress.setText(Messages.MemoryBlockWizardPage_PhysicalAddress);
		lblParentPhysicalAddressValue = new Label(container, SWT.NONE);
		lblParentPhysicalAddressValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(container, SWT.NONE);

		lblTotalSize = new Label(container, SWT.NONE);
		lblTotalSize.setText(Messages.MemoryBlockWizardPage_TotalSize);

		lblTotalSizeValue = new Label(container, SWT.NONE);
		lblTotalSizeValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(container, SWT.NONE);

		lblFreeSize = new Label(container, SWT.NONE);
		lblFreeSize.setToolTipText(Messages.MemoryBlockWizardPage_AvailableContiniousBlockSize);
		lblFreeSize.setText(Messages.MemoryBlockWizardPage_FreeSize);

		lblFreeSizeValue = new Label(container, SWT.NONE);
		lblFreeSizeValue.setToolTipText(Messages.MemoryBlockWizardPage_AvailableContiniousBlockSize);
		lblFreeSizeValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(container, SWT.NONE);

		Label separator2 = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator2.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, 4, 1));

		lblInfo = new Label(container, SWT.NONE);
		lblInfo.setText(Messages.MemoryBlockWizardPage_Info);

		textInfo = new Text(container, SWT.BORDER);
		textInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		container.setTabList(new Control[]{textName, textSize, textOffset, chkAutoOffset, textPhysicalAddress, chkStartup, chkNoInit, chkDma, accessControl, textInfo});
		container.getShell().setSize(615, 600);

		setInitialValues();
	}


	protected void autoOffsetChanged() {
		fbFixed = !chkAutoOffset.getSelection();
		updateOffset();
	}

	public boolean isFixed() {
		return fbFixed;
	}

	protected void updateOffset() {
		if(!fbInitialized)
			return;
		if(getParentBlock() == null)
			return;

		chkAutoOffset.setSelection(!fbFixed);
		textOffset.setEditable(fbFixed);

		String strPhysical = CmsisConstants.EMPTY_STRING;
		if(parentPhysicalAddress != null) {
			Long offset = getOffset();
			if(offset != null) {
				Long physicalAddress = parentPhysicalAddress + offset;
				strPhysical = IAttributes.longToHexString8(physicalAddress);
			}
		}
		textPhysicalAddress.setText(strPhysical);
	}

	protected void permissionsModified() {
		// adjust controls
		IMemoryPermissions permissions = accessControl.getPermissions();
		boolean bPeripheral = permissions.isPeripheralAccess();
		boolean bWrite = permissions.isWriteAccess();
		boolean bExecute = permissions.isExecuteAccess();
		chkStartup.setEnabled(!bPeripheral && bExecute);
		chkStartup.setSelection(bStartup && !bPeripheral && bExecute);

		chkNoInit.setEnabled(!bPeripheral && bWrite);
		chkNoInit.setSelection(bNoInit && !bPeripheral && bWrite);
	}

	public boolean isARMv8(){
		return  getController().getRootZone().getArchitecture().isARMv8();
	}

	private String getInitialName() {
		ICpMemoryBlock parent = getParentBlock();
		if(parent == null) {
			return CmsisConstants.EMPTY_STRING;
		}

		String baseName = parent.getName() + '_';
		for(int i = 1; i < 4096; i++ ) { // actually should be enough
			String name = baseName + String.valueOf(i);
			String id = ICpMemoryBlock.constructBlockId(CmsisConstants.MEMORY_TAG, name, null);
			if(getRootZone().getResources().getMemoryBlock(id) == null)
				return name;
		}
		return baseName; // should never happen
	}

	private void setInitialValues() {
		ICpMemoryBlock parentBlock = getParentBlock();
		Long size = 0L;
		String name = CmsisConstants.EMPTY_STRING;
		String offsetString = CmsisConstants.EMPTY_STRING;
		setPermissions();
		if(parentBlock != null && !parentBlock.isPeripheral()){
			lblParentName.setText(parentBlock.getName());

			Long logicalAddress = parentBlock.getStart();
			String strLogical = IAttributes.longToHexString8(logicalAddress);
			lblParentLogicalAddressValue.setText(strLogical.trim());

			parentPhysicalAddress = parentBlock.getAddress();
			String strPhysical = IAttributes.longToHexString8(parentPhysicalAddress);
			lblParentPhysicalAddressValue.setText(strPhysical.trim());

			String parentSizeString = parentBlock.getSizeString();
			Long parentSize = Utils.stringToLong(parentSizeString);
			if(parentSize != null)
				parentSizeString = Utils.getFormattedMemorySizeString(parentSize);
			lblTotalSizeValue.setText(parentSizeString.trim());

			size = parentBlock.getFreeSize();
			String sizeString = Utils.getFormattedMemorySizeString(size);
			lblFreeSizeValue.setText(sizeString.trim());
			name = getInitialName();
		} else {
			textName.setEditable(false);
			textSize.setEditable(false);
			textOffset.setEditable(false);
			lblOffset.setText(Messages.MemoryBlockWizardPage_Start);
			chkAutoOffset.setEnabled(false);
			lblParent.setVisible(false);
			lblParentLogicalAddress.setVisible(false);
			lblParentPhysicalAddress.setVisible(false);
			lblTotalSize.setVisible(false);
			lblFreeSize.setVisible(false);
			textInfo.setEditable(false);
		}
		ICpMemoryBlock block = getExistingItem();
		if(block != null) {
			name = block.getName();
			size = block.getSize();
			if(parentBlock == null) {
				long start = block.getStart();
				offsetString =  IAttributes.longToHexString8(start);

				Long physicalAddress = block.getAddress();
				String strPhysical = IAttributes.longToHexString8(physicalAddress);
				textPhysicalAddress.setText(strPhysical.trim());

			} else {
				offsetString =  IAttributes.longToHexString8(block.getOffset());
			}

			fbFixed = block.isFixed();

			textInfo.setText(block.getInfo());

			chkShared.setSelection(block.isShared());
			chkDma.setSelection(block.isDma());
			// 	startup and init flags are set in permisiionsModified()
			bStartup = block.isStartup();
			bNoInit = block.isNoInit();

		}
		textName.setText(name);

		if(size != 0L) {
			String sizeString = Utils.getFormattedMemorySizeString(size);
			textSize.setText(sizeString.trim());
		}

		textOffset.setText(offsetString.trim());
		fbInitialized = true;
		updateOffset();
		permissionsModified();
		check();
	}

	void setPermissions() {
		IMemoryPermissions permissions= null;
		IMemoryPermissions parentPermissions = null;
		ICpMemoryBlock parentBlock = getParentBlock();
		if(parentBlock != null) {
			if(!parentBlock.isPeripheralAccess()) {
				parentPermissions = parentBlock;
			}
		}
		ICpMemoryBlock block = getExistingItem();
		if(block != null) {
			permissions = block;
			parentPermissions = block.getParentPermissions();
		}

		boolean bShowSecurePermissions = getController().getRootZone().hasSecureCore();
		accessControl.setInitialValues(permissions, parentPermissions, bShowSecurePermissions);
	}

	private void updateStatus(String message) {
		updateStatus(message, ERROR);
	}

	private void updateStatus(String message, int type) {

		setMessage(message, type);
		if(message == null) {
			setErrorMessage(message);
			setMessage(message, WARNING);
		}
		setPageComplete(message == null || type != ERROR);
	}

	private Long getSize(){
		String sizeString = textSize.getText();
		if(sizeString.isEmpty()) {
			updateStatus(Messages.MemoryBlockWizardPage_SizeSpecification);
			return null;
		}
		Long size = Utils.stringToLong(sizeString);
		return size;
	}


	private Long getOffset() {
		Long offset = 0L;
		String offsetString = textOffset.getText();
		if(!offsetString.isEmpty()){
			offset = Utils.stringToLong(offsetString);
		}
		return offset;
	}

	protected boolean checkSize(Long size) {

		ICpMemoryBlock parent = getParentBlock();
		if(parent == null) {
			return true;
		}

		if(size == null) {
			updateStatus(Messages.MemoryBlockWizardPage_SizeValidation);
			return false;
		}

		if(size < 0x20L) {
			updateStatus(Messages.MemoryBlockWizardPage_MinimumBlockSize);
			return false;
		}

		if(size % 0x20L != 0L) {
			updateStatus(Messages.MemoryBlockWizardPage_SizeAlignment);
			return false;
		}


//		if(!isARMv8()){
//			if(Long.bitCount(size) != 1) {
//				updateStatus("Size value must be power of 2 to use MPU", WARNING);
//				return false;
//			}
//		}
		return true;
	}


	protected boolean checkOffset(Long offset) {
		if(!isFixed())
			return true;
		if(offset == null){
			updateStatus(Messages.MemoryBlockWizardPage_InvalidOffsetValue);
			return false;
		}

		if(offset % 0x20L != 0L) {
			updateStatus(Messages.MemoryBlockWizardPage_OffsetAlignment);
			return false;
		}
		if(fMpuMode && fArchitecture== ECoreArchitecture.ARMv7){

			Long size = getSize();
			if(size == null || size == 0)
				return true; // cannot evaluate
			size = ICpMpuRegion.getMpu7RegionSize(size);
		//   treat size alignment as warning
			if(offset % size != 0) {
				String msg = Messages.MemoryBlockWizardPage_BlockSizeAlignment + IAttributes.longToHexString8(size);
				updateStatus(msg, WARNING);
			}
		}

		return true;
	}


	protected void check() {
		if( !fbInitialized ) {
			return;
		}
		if(getRootZone() == null)
			return;
		updateStatus(null);
		ICpMemoryBlock parentBlock = getParentBlock();
		if(parentBlock == null || parentBlock.isPeripheralAccess()) {
			return;
		}

		String name = textName.getText();
		if(name.isEmpty()) {
			updateStatus(Messages.MemoryBlockWizardPage_RegionSpecification);
			return;
		}
		String id = ICpMemoryBlock.constructBlockId(CmsisConstants.MEMORY_TAG, name, null);
		ICpMemoryBlock block = getRootZone().getResources().getMemoryBlock(id);
		if(block != null && block != getExistingItem()) {
			updateStatus(Messages.MemoryBlockWizardPage_MemoryRegionValidation);
			return;
		}

		Long size = getSize();
		if(!checkSize(size)) {
			return;
		}

		if(fMpuMode && fArchitecture== ECoreArchitecture.ARMv7) {
			long alignedSize = ICpMpuRegion.alignToMpu7(size);
			String alignedSizeStr = Utils.getFormattedMemorySizeString(alignedSize);
			lblMpuSize.setText(alignedSizeStr);
		}

		Long parentSize = parentBlock.getSize();
		if(Long.compareUnsigned(size, parentSize) > 0) {
			updateStatus(Messages.MemoryBlockWizardPage_ParentSize);
			return;
		}

		if(isFixed()) {
			Long offset = getOffset();
			if(!checkOffset(offset)){
				return;
			}

			if(Long.compareUnsigned(offset, parentSize) > 0) {
				updateStatus(Messages.MemoryBlockWizardPage_OffsetExceedsParentBoundary);
				return;
			}
			Long sizeWithOffset = size + offset;
			if(Long.compareUnsigned(sizeWithOffset, parentSize) > 0) {
				updateStatus(Messages.MemoryBlockWizardPage_BlockBoundaryExceedsParentBoundary);
			}
		}

	}

	@Override
	public boolean apply() {
		Map<String, String> newAttributes = new HashMap<>();
		String dma = chkDma.getSelection() ? CmsisConstants.ONE : null;
		String startup = null;
		if(chkStartup.isEnabled() && chkStartup.getSelection()) {
			startup = CmsisConstants.ONE;
		}
		String noInit = null;
		if(chkNoInit.isEnabled() && chkNoInit.getSelection()) {
			noInit = CmsisConstants.ONE;
		}
		IMemoryPermissions permissions = accessControl.getPermissions();
		newAttributes.put(CmsisConstants.STARTUP, startup);
		newAttributes.put(CmsisConstants.UNINIT, noInit);
		newAttributes.put(CmsisConstants.DMA, dma);
		ICpMemoryBlock parentBlock = getParentBlock();
		if(getParentBlock() != null && !parentBlock.isPeripheral()) { 	// top-level block size and offset cannot be changed
			Long size = getSize();
			if(size == null || size.longValue() < 0x20 ) {
				return false;
			}
			String sizeString = IAttributes.longToHexString8(size);

			Long offset = isFixed() ? getOffset() : 0L;
			String offsetString = IAttributes.longToHexString8(offset);

			String fixed = isFixed() ? CmsisConstants.ONE : null;

			String name = textName.getText();
			String info = textInfo.getText();

			newAttributes.put(CmsisConstants.NAME, name);
			newAttributes.put(CmsisConstants.SIZE, sizeString);
			newAttributes.put(CmsisConstants.OFFSET, offsetString);
			newAttributes.put(CmsisConstants.INFO, info);
			newAttributes.put(CmsisConstants.FIXED, fixed);
		}
		getController().updateMemoryBlock(getParentBlock(), getExistingItem(), newAttributes, permissions);
		return true;
	}
}
