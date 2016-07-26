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

package com.arm.cmsis.pack.rte.boards;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.item.CmsisMapItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.devices.RteDeviceItem;
import com.arm.cmsis.pack.utils.AlnumComparator;
import com.arm.cmsis.pack.utils.VersionComparator;

/**
 * Default implementation of {@link RteBoardDeviceItem}
 */
public class RteBoardDeviceItem extends CmsisMapItem<IRteBoardDeviceItem> implements IRteBoardDeviceItem {

	private Map<String, ICpBoard> fBoards = null;	// packId -> board
	private IRteDeviceItem fMountedDevices;	// deviceName -> deviceItem
	private IRteDeviceItem fCompatibleDevices;	// deviceName -> deviceItem
	private boolean fRoot;

	public RteBoardDeviceItem() {
		fName = "All Boards"; //$NON-NLS-1$
		fRoot = true;
	}

	public RteBoardDeviceItem(String name, IRteBoardDeviceItem parent) {
		super(parent);
		fName= name;
		fRoot = false;
	}

	@Override
	protected Map<String, IRteBoardDeviceItem> createMap() {
		// create TreeMap with Alpha-Numeric case-insensitive ascending sorting
		return new TreeMap<String, IRteBoardDeviceItem>(new AlnumComparator(false, false));
	}

	/**
	 * Creates board tree from list of Packs
	 * @param packs collection of packs to use
	 * @return device tree as root IRteBoardDeviceItem
	 */
	public static IRteBoardDeviceItem createTree(Collection<ICpPack> packs){
		IRteBoardDeviceItem root = new RteBoardDeviceItem();
		if(packs == null || packs.isEmpty()) {
			return root;
		}
		for(ICpPack pack : packs) {
			root.addBoards(pack);
		}
		return root;
	}

	@Override
	public boolean isRoot() {
		return fRoot;
	}

	@Override
	public void addBoard(ICpBoard item) {
		if (item == null) {
			return;
		}

		if (fRoot) {
			addBoardItem(item, item.getId());
		} else {
			ICpPack pack = item.getPack();
			String packId = pack.getId();
			if(fBoards == null) {
				fBoards = new TreeMap<String, ICpBoard>(new VersionComparator());
			}

			ICpBoard board = fBoards.get(packId);
			if (board == null ||
					// new item's pack is installed/downloaded and the one in the tree is not
					(item.getPack().getPackState().ordinal() < board.getPack().getPackState().ordinal())) {
				fBoards.put(packId, item);
			}

			Collection<ICpItem> devices = item.getMountedDevices();
			for (ICpItem device : devices) {
				String vendorName = device.getVendor();
				String deviceName = getDeviceName(device);
				IRteDeviceItem allDevices = CpPlugIn.getPackManager().getDevices();
				if (allDevices == null) {
					continue;
				}
				IRteDeviceItem rteDeviceItem = allDevices.findItem(deviceName, vendorName, false);
				if (rteDeviceItem == null) {
					continue;
				}
				if (fMountedDevices == null) {
					fMountedDevices = new RteDeviceItem(CmsisConstants.MOUNTED_DEVICES, -1, null);	// -1 means pseudo root
				}
				if (fMountedDevices.getChild(rteDeviceItem.getName()) == null) {
					fMountedDevices.addChild(rteDeviceItem);
				}
			}

			devices = item.getCompatibleDevices();
			for (ICpItem device : devices) {
				String vendorName = device.getVendor();
				String deviceName = getDeviceName(device);
				IRteDeviceItem allDevices = CpPlugIn.getPackManager().getDevices();
				if (allDevices == null) {
					continue;
				}
				IRteDeviceItem rteDeviceItem = allDevices.findItem(deviceName, vendorName, false);
				if (rteDeviceItem == null) {
					continue;
				}
				if (fCompatibleDevices == null) {
					fCompatibleDevices = new RteDeviceItem(CmsisConstants.COMPATIBLE_DEVICES, -1, null);	// -1 means pseudo root
				}
				if (fCompatibleDevices.getChild(rteDeviceItem.getName()) == null) {
					fCompatibleDevices.addChild(rteDeviceItem);
				}
			}

			return;
		}
	}

	protected void addBoardItem(ICpBoard item, final String itemName) {
		IRteBoardDeviceItem bi = getChild(itemName);
		if(bi == null ) {
			bi = new RteBoardDeviceItem(itemName, this);
			addChild(bi);
		}
		bi.addBoard(item);
	}

	@Override
	public void addBoards(ICpPack pack) {
		if (pack == null) {
			return;
		}
		Collection<? extends ICpItem> boards = pack.getGrandChildren(CmsisConstants.BOARDS_TAG);
		if(boards == null) {
			return;
		}
		for(ICpItem item : boards) {
			if(!(item instanceof ICpBoard)) {
				continue;
			}
			ICpBoard boardItem = (ICpBoard)item;
			addBoard(boardItem);
		}
	}

	@Override
	public void removeBoard(ICpBoard item) {
		if (item == null) {
			return;
		}

		if (fRoot) {
			IRteBoardDeviceItem b = getChild(item.getId());
			if(b == null ) {
				return;
			}
			b.removeBoard(item);
		} else {
			String packId = item.getPackId();
			if (fBoards == null) {
				return;
			}

			fBoards.remove(packId);

			if (fBoards.size() == 0) {
				getParent().removeChild(this);
				setParent(null);
			}
			return;
		}
	}

	@Override
	public void removeBoards(ICpPack pack) {
		if (pack == null) {
			return;
		}
		Collection<? extends ICpItem> boards = pack.getGrandChildren(CmsisConstants.BOARDS_TAG);
		if (boards != null) {
			for(ICpItem item : boards) {
				if(!(item instanceof ICpBoard)) {
					continue;
				}
				ICpBoard currentBoard = (ICpBoard)item;
				removeBoard(currentBoard);
			}
		}
	}

	@Override
	public ICpBoard getBoard() {
		if(fBoards != null && !fBoards.isEmpty()) {
			// Return the latest INSTALLED pack's board
			for (ICpBoard board : fBoards.values()) {
				if (board.getPack().getPackState() == PackState.INSTALLED) {
					return board;
				}
			}
			// Otherwise return the latest pack's board
			return fBoards.entrySet().iterator().next().getValue();
		}
		return null;
	}

	@Override
	public Collection<ICpBoard> getBoards() {
		if (fBoards != null) {
			return fBoards.values();
		}
		return null;
	}

	@Override
	public IRteDeviceItem getMountedDevices() {
		return fMountedDevices;
	}

	@Override
	public IRteDeviceItem getCompatibleDevices() {
		return fCompatibleDevices;
	}

	private String getDeviceName(ICpItem device) {
		String deviceName = CmsisConstants.EMPTY_STRING;
		if (device.hasAttribute(CmsisConstants.DFAMILY)) {
			deviceName = device.getAttribute(CmsisConstants.DFAMILY);
		} else if (device.hasAttribute(CmsisConstants.DSUBFAMILY)) {
			deviceName = device.getAttribute(CmsisConstants.DSUBFAMILY);
		} else if (device.hasAttribute(CmsisConstants.DNAME)) {
			deviceName = device.getAttribute(CmsisConstants.DNAME);
		} else if (device.hasAttribute(CmsisConstants.DVARIANT)) {
			deviceName = device.getAttribute(CmsisConstants.DVARIANT);
		}
		return deviceName;
	}

	@Override
	public IRteBoardDeviceItem findBoard(String boardId) {
		if (fRoot) {
			return getChild(boardId);
		} else if (fName.equals(boardId)) {
			return this;
		}
		return null;
	}

	@Override
	public String getDescription() {
		ICpBoard board = getBoard();
		if(board != null) {
			String description = board.getDescription();
			if(description != null && !description.isEmpty()) {
				return description;
			}
		}
		if(getParent() != null) {
			return getParent().getDescription();
		}
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public String getUrl() {
		ICpBoard board = getBoard();
		if(board != null) {
			return board.getUrl();
		}
		return null;
	}

	@Override
	public String getDoc() {
		ICpBoard board = getBoard();
		if(board != null)
		{
			return board.getDoc(); // TODO: return a collection of documents
		}
		return null;
	}

}
