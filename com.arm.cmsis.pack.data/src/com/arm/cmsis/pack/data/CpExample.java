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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

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
	public String constructId() {
		String id = getAttribute(CmsisConstants.NAME);
		ICpItem board = getFirstChild(CmsisConstants.BOARD_TAG);
		if (board != null) {
			id += " (" + board.getAttribute(CmsisConstants.NAME) + ")";  //$NON-NLS-1$//$NON-NLS-2$
		}
		return id;
	}

	@Override
	public String getFolder() {
		return getAttribute(CmsisConstants.FOLDER);
	}


	@Override
	public String getBoardId() {
		ICpItem board = getFirstChild(CmsisConstants.BOARD_TAG);
		if (board == null) {
			return null;
		}
		return board.getId();
	}

	@Override
	public String getLoadPath(String environmentName) {
		if(environmentName == null)
			return null;
		Collection<? extends ICpItem> environments = getGrandChildren(CmsisConstants.PROJECT_TAG);
		if (environments != null) {
			for (ICpItem environment : environments) {
				if (environment.getName().equals(environmentName)){
					return environment.getAttribute(CmsisConstants.LOAD);
				}
			}
		}
		return null;
	}

	@Override
	public String getAbsoluteLoadPath(String environmentName) {
		String loadPath = getLoadPath(environmentName);
		if(loadPath == null)
			return null;
		Path path = Paths.get(getAbsolutePath(getFolder()));
		
		Path examplePath = path.resolve(loadPath);
		return examplePath.toString().replace('\\', '/');
	}

	@Override
	public boolean containsBoard(String boardId) {
		Collection<? extends ICpItem> children = getChildren();
		if(children == null)
			return false;
		for(ICpItem item : children) {
			if(!item.getTag().equals(CmsisConstants.BOARD_TAG)) 
				continue;
			if(item.getId().equals(boardId))
				return true;
		}
		return false;
	}
	
}
