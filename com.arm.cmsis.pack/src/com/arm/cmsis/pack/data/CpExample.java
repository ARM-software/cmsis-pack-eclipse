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

package com.arm.cmsis.pack.data;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.DeviceVendor;
import com.arm.cmsis.pack.common.CmsisConstants;

/**
 * Default implementation of {@link ICpExample} interface
 */
public class CpExample extends CpItem implements ICpExample {

	/**
	 * @param parent
	 */
	public CpExample(ICpItem parent) {
		super(parent);
	}

	/**
	 * @param parent
	 * @param tag
	 */
	public CpExample(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public String getId() {
		String id = getAttribute(CmsisConstants.NAME);
		ICpItem board = getFirstChild(CmsisConstants.BOARD_TAG);
		if (board != null) {
			id += " (" + board.getAttribute(CmsisConstants.NAME) + ")";  //$NON-NLS-1$//$NON-NLS-2$
		}
		return id;
	}

	@Override
	public String getFolder() {
		if (hasAttribute(CmsisConstants.FOLDER)) {
			return getAttribute(CmsisConstants.FOLDER);
		}
		return null;
	}

	@Override
	public ICpBoard getBoard() {
		ICpBoard board = (ICpBoard) getFirstChild(CmsisConstants.BOARD_TAG);
		if (board == null) {
			return null;
		}
		// Dvendor has precedence over vendor attribute. here should try both
		if (CpPlugIn.getPackManager().getBoards().get(board.getId()) != null) {
			return CpPlugIn.getPackManager().getBoards().get(board.getId());
		} else {
			String id = DeviceVendor.getOfficialVendorName(board.getAttribute(CmsisConstants.VENDOR));
			String name = board.getAttribute(CmsisConstants.NAME);
			if(name != null && !name.isEmpty()) {
				id += CmsisConstants.DOBLE_COLON;
				id += name;
			}
			return CpPlugIn.getPackManager().getBoards().get(id);
		}
	}

}
