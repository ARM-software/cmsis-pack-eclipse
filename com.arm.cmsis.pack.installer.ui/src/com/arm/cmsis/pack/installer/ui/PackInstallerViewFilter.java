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

package com.arm.cmsis.pack.installer.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkbenchPart;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.installer.ui.views.BoardsView;
import com.arm.cmsis.pack.installer.ui.views.DevicesView;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.item.ICmsisItem;
import com.arm.cmsis.pack.rte.boards.IRteBoardDeviceItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.examples.IRteExampleItem;
import com.arm.cmsis.pack.utils.Utils;

/**
 * The filter used to filter the packs and examples
 */
public class PackInstallerViewFilter extends ViewerFilter {

	protected IRteDeviceItem 		fDeviceItem = null ;
	protected IRteBoardDeviceItem	fBoardItem = null;

	
	protected boolean bAllDevices = false;
	protected boolean bAllBoards  = false;
	
	protected String fBoardName = null;
	
	protected Set<String> fSelectedDeviceNames = null;

	protected IWorkbenchPart selectionPart;
	protected IStructuredSelection selection;

	protected boolean installedOnly;
	protected String filterString = null;
	
	protected Set<ICpPackFamily> fFilteredDevicePackFamilies = new HashSet<ICpPackFamily>();
	

	/**
	 * Default constructor
	 */
	public PackInstallerViewFilter() {
		installedOnly = false;
	}

	public void clear() {
		selection = null;
		selectionPart = null;
		fFilteredDevicePackFamilies.clear();
		fSelectedDeviceNames = null;
		fDeviceItem = null;
		fBoardItem = null;
		filterString = null;
	}
	
	public Set<ICpPackFamily> getFilteredDevicePackFamilies() {
		return fFilteredDevicePackFamilies;
	}
	
	/**
	 * @param part
	 * @param selection
	 */
	public boolean setSelection(IWorkbenchPart part, IStructuredSelection selection) {
		this.selectionPart = part;
		this.selection = selection;
		boolean bFilterChanged  = updateFilter();
		if(bFilterChanged) {
			updateFilterdPacks();
		}
		return bFilterChanged;
	}

	protected void updateFilterdPacks() {
		fFilteredDevicePackFamilies.clear();
		ICpPackManager pm = CpPlugIn.getPackManager();
		ICpPackCollection dfps = pm.getDevicePacks();
		if(dfps == null || !dfps.hasChildren())
			return;
		for(ICpItem item : dfps.getChildren()) {
			if(item instanceof ICpPackFamily) {
				ICpPackFamily f = (ICpPackFamily) item;
				if(isDevicePackFamilyFiltered(f))
					fFilteredDevicePackFamilies.add(f);
			}
		}
	}


	protected boolean updateFilter() {
		filterString = createFilterString();
		if (selectionPart instanceof DevicesView) {
			return setDeviceSelection();
		} else if (selectionPart instanceof BoardsView) {
			return setBoardSelection();
		}
		return false;
	}

	protected boolean setDeviceSelection() {
		IRteDeviceItem item = null;
		if(selection != null && selection.size() == 1) {
			item = (IRteDeviceItem) selection.getFirstElement();
			bAllDevices = CmsisConstants.ALL_DEVICES.equals(item.getName());
		} else {
			bAllDevices = false;
		}
		return setDeviceItem(item);
	}

	protected boolean setDeviceItem(IRteDeviceItem item) {
		if(fDeviceItem == item)
			return false;
		fBoardItem = null;
		fBoardName = null;
		fDeviceItem = item;
		if(fDeviceItem != null && !bAllDevices) {
			fSelectedDeviceNames = item.getAllDeviceNames();
		} else { 
			fSelectedDeviceNames = null;
		}
		return true;
	}

	
	protected boolean setBoardSelection() {
		ICmsisItem item = null;
		if(selection != null && selection.size() == 1) {
			item = (ICmsisItem) selection.getFirstElement();
			bAllBoards = CmsisConstants.ALL_BOARDS.equals(item.getName());
		} else {
			bAllBoards = false;
		}
		if(item != null && item instanceof IRteDeviceItem)
			return setDeviceItem((IRteDeviceItem)item);
		return setBoardItem((IRteBoardDeviceItem)item);
	}
	
	
	protected boolean setBoardItem(IRteBoardDeviceItem item) {
		if(fBoardItem == item)
			return false;
		fBoardItem = item;
		fDeviceItem = null;
		if(fBoardItem != null && !bAllBoards ) {
			fBoardName = fBoardItem.getName();
			fSelectedDeviceNames = fBoardItem.getAllDeviceNames();
		} else {
			fBoardName = null;
			fSelectedDeviceNames = null;
		}
		return true;
	}
	
	public String getFilterString() {
		return filterString;
	}
	
	protected String createFilterString() {
		if (selection == null || selection.isEmpty()) {
			if (selectionPart instanceof DevicesView) {
				return Messages.PacksExamplesViewFilter_NoDevices;
			} else if (selectionPart instanceof BoardsView) {
				return Messages.PacksExamplesViewFilter_NoBoards;
			} else {
				return null;
			}
		}
		ICmsisItem item = (ICmsisItem) selection.getFirstElement();
		if (item != null) {
			if (selectionPart instanceof BoardsView &&
					(CmsisConstants.MOUNTED_DEVICES.equals(item.getName()) ||
							CmsisConstants.COMPATIBLE_DEVICES.equals(item.getName()))) {
				return item.getChildren().iterator().next().getName()
						+ " (" + item.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return item.getName();
		}
		return null;
	}

	/**
	 * Set to filter out uninstalled examples
	 * @param installedOnly
	 */
	public void setInstalledOnly(boolean installedOnly) {
		this.installedOnly = installedOnly;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IRteExampleItem) {
			return selectExamples((IRteExampleItem) element);
		} else 	if (element instanceof ICpPackFamily)
			return selectPacks( (ICpPackFamily) element);
		return true;
		
	}

	protected boolean isDevicePackFamilyFiltered(ICpPackFamily f) {
		ICpPack pack = f.getPack();
		if (pack == null) {
			return false;
		} 
		
		if(pack.isDevicelessPack()) {
			return false;
		}
		
		if(fDeviceItem != null) {
			if(bAllDevices)
				return true;
			if(fSelectedDeviceNames == null)
				return false;
			return packContainsRteDevice(fDeviceItem, pack);
		} else if (fBoardItem != null) {
			if(bAllBoards)
				return true;
			if(fSelectedDeviceNames == null)
				return false;
			if(fBoardName == null)
				return false;
			return packContainsBoard(fBoardItem, pack); 
		}
		return false;
	}

	
	protected boolean selectPacks(ICpPackFamily f) {
		ICpPack pack = f.getPack();
		if (pack == null) {
			return false;
		} 
		
		if(pack.isDevicelessPack()) {
			return true;
		}
		
		return fFilteredDevicePackFamilies.contains(f);
	}
	

	/**
	 * @param board The board
	 * @param pack	The pack
	 * @return		true if pack contains this board, otherwise false
	 */
	private boolean packContainsBoard(IRteBoardDeviceItem board, ICpPack pack) {

		// check if the pack contains a board
		Set<String> boardNames = pack.getBoardNames();
		if(boardNames != null && boardNames.contains(fBoardName))
			return true;

		Set<String> devicesContainedInPack = pack.getAllDeviceNames();
		if (Utils.checkIfIntersect(fSelectedDeviceNames, devicesContainedInPack)) {
			return true;
		}
		
		IRteDeviceItem mountedDevices = board.getMountedDevices();
		IRteDeviceItem compatibleDevices = board.getCompatibleDevices();

		return packContainsRteDevice(mountedDevices, pack) ||
				packContainsRteDevice(compatibleDevices, pack);
	}

	/**
	 * Convert the ICpItem to IRteDeviceItem
	 * @param item
	 * @return the corresponding IRteDeviceItem, null if no matching IRteDeviceItem is found
	 */
	private IRteDeviceItem convertCpItemToRteDeviceItem(ICpItem item) {

		String vendorName = item.getVendor();
		String deviceName = CmsisConstants.EMPTY_STRING;
		if (item.hasAttribute(CmsisConstants.DFAMILY)) {
			deviceName = item.getAttribute(CmsisConstants.DFAMILY);
		} else if (item.hasAttribute(CmsisConstants.DSUBFAMILY)) {
			deviceName = item.getAttribute(CmsisConstants.DSUBFAMILY);
		} else if (item.hasAttribute(CmsisConstants.DNAME)) {
			deviceName = item.getAttribute(CmsisConstants.DNAME);
		} else if (item.hasAttribute(CmsisConstants.DVARIANT)) {
			deviceName = item.getAttribute(CmsisConstants.DVARIANT);
		}

		if (vendorName.isEmpty()) {
			return null;
		}

		IRteDeviceItem allRteDevices = CpPlugIn.getPackManager().getDevices();
		if (deviceName.isEmpty()) {
			return allRteDevices.getVendorItem(vendorName);
		}

		return allRteDevices.findItem(deviceName, vendorName, false);
	}

	private boolean packContainsRteDevice(IRteDeviceItem deviceItem, ICpPack pack) {
		if (deviceItem == null) {
			return false;
		}

		if(deviceItem == fDeviceItem) {
			Set<String> devicesContainedInPack = pack.getAllDeviceNames();
			if (Utils.checkIfIntersect(fSelectedDeviceNames, devicesContainedInPack)) {
				return true;
			}
		}

		
		// Check if the mounted devices or compatible devices on this pack's board
		// intersect with deviceItem's devices
		Collection<? extends ICpItem> boards = pack.getGrandChildren(CmsisConstants.BOARDS_TAG);
		if (boards == null || boards.isEmpty()) {
			return false;
		}
		for (ICpItem item : boards) {
			if(!(item instanceof ICpBoard)) {
				continue;
			}
			ICpBoard b = (ICpBoard)item;
			Collection<ICpItem> mountedDevices = b.getMountedDevices();
			for (ICpItem mountedDevice : mountedDevices) {
				IRteDeviceItem mountedDeviceItemInPack = convertCpItemToRteDeviceItem(mountedDevice);
				if (mountedDeviceItemInPack == null) {
					continue;
				}
				Set<String> temp = mountedDeviceItemInPack.getAllDeviceNames();
				if (Utils.checkIfIntersect(temp, fSelectedDeviceNames)) {
					return true;
				}
			}
			Collection<ICpItem> compatibleDevices = b.getCompatibleDevices();
			for (ICpItem compatibleDevice : compatibleDevices) {
				// Check intersection
				IRteDeviceItem compatibleDeviceItemInPack = convertCpItemToRteDeviceItem(compatibleDevice);
				if (compatibleDeviceItemInPack == null) {
					continue;
				}
				// Check intersection
				Set<String> temp = compatibleDeviceItemInPack.getAllDeviceNames();
				if (Utils.checkIfIntersect(temp, fSelectedDeviceNames)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean selectExamples(IRteExampleItem exampleItem ) {

		if (exampleItem == null) {
			return false;
		}

		ICpExample example = exampleItem.getExample();
		if (example == null)
			return false;

		if(fDeviceItem == null && fBoardItem == null)
			return false;
		
		if(installedOnly &&
				example.getPack().getPackState() != PackState.INSTALLED &&
				example.getPack().getPackState() != PackState.GENERATED) {
					return false;
		}
		
		if (fDeviceItem != null) {
			if (bAllDevices) {
				return true;
			}
			return boardContainsDevice(example.getBoard(), fDeviceItem);
		} else if (fBoardItem != null) {
			if (bAllBoards) {
				return true;
			}
			return exampleContainsBoard(example, fBoardItem.getBoard());
		}
		return false;
	}

	private boolean exampleContainsBoard(ICpExample example, ICpBoard board) {
		if (example.getBoard() != null && board != null) {
			return example.getBoard().getId().equals(board.getId());
		}
		return false;
	}

	private boolean boardContainsDevice(ICpBoard b, IRteDeviceItem rteDeviceItem) {
		if (b == null) {
			return false;
		}
		IRteBoardDeviceItem board = CpPlugIn.getPackManager().getRteBoardDevices().findBoard(b.getId());
		if (board != null) {
			if (Utils.checkIfIntersect(board.getAllDeviceNames(), fSelectedDeviceNames)) {
				return true;
			}
		}
		return false;
	}
}
