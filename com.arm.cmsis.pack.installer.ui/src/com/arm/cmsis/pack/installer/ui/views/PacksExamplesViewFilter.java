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

package com.arm.cmsis.pack.installer.ui.views;

import java.util.Collection;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.IWorkbenchPart;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.data.ICpPackCollection;
import com.arm.cmsis.pack.data.ICpPackFamily;
import com.arm.cmsis.pack.installer.ui.Messages;
import com.arm.cmsis.pack.item.ICmsisItem;
import com.arm.cmsis.pack.rte.boards.IRteBoardDeviceItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.examples.IRteExampleItem;

/**
 * The filter used to filter the packs and examples
 */
public class PacksExamplesViewFilter extends ViewerFilter {

	private static PacksExamplesViewFilter filter;

	private IWorkbenchPart selectionPart;
	private IStructuredSelection selection;
	private boolean installedOnly;

	/**
	 * Default constructor
	 */
	protected PacksExamplesViewFilter() {
		installedOnly = false;
	}

	synchronized public static PacksExamplesViewFilter getInstance() {
		if (filter == null) {
			filter = new PacksExamplesViewFilter();
		}
		return filter;
	}

	/**
	 * @param part
	 * @param selection
	 */
	public void setSelection(IWorkbenchPart part, IStructuredSelection selection) {
		this.selectionPart = part;
		this.selection = selection;
	}

	public String getFilterString() {
		if (selection == null || selection.isEmpty()) {
			if (selectionPart instanceof DevicesView) {
				return Messages.PacksExamplesViewFilter_NoDevices;
			} else if (selectionPart instanceof BoardsView) {
				return Messages.PacksExamplesViewFilter_NoBoads;
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
			return selectExamples(viewer, parentElement, element);
		}
		return selectPacks(viewer, parentElement, element);
	}

	private boolean selectPacks(Viewer viewer, Object parentElement, Object element) {

		if (element instanceof ICpPackCollection) {
			return true;
		} else if (element instanceof ICpPackFamily) {
			ICpPackFamily f = (ICpPackFamily) element;
			if (f.getPack() == null) {
				return false;
			} else if(f.getPack().isDevicelessPack()) {
				return true;
			}
		}


		if (selectionPart instanceof DevicesView) {
			if (element instanceof ICpPackFamily) {		// Element in PacksView
				if (selection.isEmpty()) {
					return false;
				}
				IRteDeviceItem item = (IRteDeviceItem) selection.getFirstElement();
				if (CmsisConstants.ALL_DEVICES.equals(item.getName())) {
					return true;
				}
				return packContainsRteDevice(item, (ICpPackFamily) element);
			}
		} else if (selectionPart instanceof BoardsView) {
			if (element instanceof ICpPackFamily) {		// Element in PacksView
				if (selection.isEmpty()) {
					return false;
				}
				Object item = selection.getFirstElement();

				if (item instanceof IRteDeviceItem) {	// Selected Device
					return packContainsRteDevice((IRteDeviceItem) item, (ICpPackFamily) element);
				}

				if (item instanceof IRteBoardDeviceItem) {	// Selected Board
					IRteBoardDeviceItem bdItem = (IRteBoardDeviceItem) item;
					if (CmsisConstants.ALL_BOARDS.equals(bdItem.getName())) {
						return true;
					}
					return packContainsBoard(bdItem, (ICpPackFamily) element);
				}
			}
		}
		return true;
	}

	/**
	 * @param board The board
	 * @param pack	The pack
	 * @return		true if pack contains this board, otherwise false
	 */
	private boolean packContainsBoard(IRteBoardDeviceItem board, ICpPackFamily packFamily) {

		IRteDeviceItem mountedDevices = board.getMountedDevices();
		IRteDeviceItem compatibleDevices = board.getCompatibleDevices();

		return packContainsRteDevice(mountedDevices, packFamily) ||
				packContainsRteDevice(compatibleDevices, packFamily);
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

	private boolean packContainsRteDevice(IRteDeviceItem deviceItem, ICpPackFamily packFamily) {
		if (deviceItem == null) {
			return false;
		}
		Set<String> devices = deviceItem.getAllDeviceNames();
		ICpPack pack = packFamily.getPack();
		Set<String> devicesContainedInPack = pack.getAllDeviceNames();
		if (intersect(devices, devicesContainedInPack)) {
			return true;
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
				if (intersect(temp, devices)) {
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
				if (intersect(temp, devices)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean selectExamples(Viewer viewer, Object parentElement, Object element) {

		if (selection == null || selection.isEmpty()) {
			return false;
		}

		ICpExample example = ((IRteExampleItem) element).getExample();

		if (example == null ||
				(installedOnly &&
						example.getPack().getPackState() != PackState.INSTALLED &&
						example.getPack().getPackState() != PackState.GENERATED)) {
			return false;
		}

		if (selectionPart instanceof DevicesView) {
			IRteDeviceItem item = (IRteDeviceItem) selection.getFirstElement();
			if (item == null) {
				return false;
			}
			if (CmsisConstants.ALL_DEVICES.equals(item.getName())) {
				return true;
			}
			return boardContainsDevice(example.getBoard(), item);
		} else if (selectionPart instanceof BoardsView) {
			Object item = selection.getFirstElement();

			if (item instanceof IRteDeviceItem) {	// Selected Device
				return boardContainsDevice(example.getBoard(), (IRteDeviceItem) item);
			}

			if (item instanceof IRteBoardDeviceItem) {	// Selected Board
				IRteBoardDeviceItem bdItem = (IRteBoardDeviceItem) item;
				if (CmsisConstants.ALL_BOARDS.equals(bdItem.getName())) {
					return true;
				}
				return exampleContainsBoard(example, bdItem.getBoard());
			}
		}

		return true;
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

		IRteDeviceItem deviceItem = board.getMountedDevices();
		if (deviceItem != null) {
			if (intersect(deviceItem.getAllDeviceNames(), rteDeviceItem.getAllDeviceNames())) {
				return true;
			}
		}

		deviceItem = board.getCompatibleDevices();
		if (deviceItem != null) {
			if (intersect(deviceItem.getAllDeviceNames(), rteDeviceItem.getAllDeviceNames())) {
				return true;
			}
		}

		return false;
	}

	private boolean intersect(Set<String> set1, Set<String> set2) {
		if (set1.size() > set2.size()) {
			return intersect(set2, set1);
		}
		for (String d1 : set1) {
			if (set2.contains(d1)) {
				return true;
			}
		}
		return false;
	}

}
