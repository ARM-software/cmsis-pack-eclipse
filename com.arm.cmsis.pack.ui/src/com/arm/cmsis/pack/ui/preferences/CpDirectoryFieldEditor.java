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

package com.arm.cmsis.pack.ui.preferences;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.swt.widgets.Composite;

import com.arm.cmsis.pack.ui.CpStringsUI;

/**
 *  A directory field editor that allows to set non-existing directories (configurable) 
 */
public class CpDirectoryFieldEditor extends DirectoryFieldEditor {

	boolean fbDirMustExist = false; // if directory must exist to be a valid value 

	public CpDirectoryFieldEditor() {
	}

	 /**
     * Creates a directory field editor.
     *
     * @param name the name of the preference this field editor works on
     * @param labelText the label text of the field editor
     * @param parent the parent of the field editor's control
     */
	public CpDirectoryFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);

	}

	@Override
	protected void createControl(Composite parent) {
		setValidateStrategy(VALIDATE_ON_KEY_STROKE);
		super.createControl(parent);
	}

	/**
	 * Checks if directory must exist to be a valid value 
	 * @return true if 
	 */
	public boolean isDirMustExist() {
		return fbDirMustExist;
	}

	/**
	 * Sets if directory must exist to be a valid value
	 * @param mustExist flag to indicate if directory must exists
	 */
	public void setDirMustExist(boolean mustExist) {
		fbDirMustExist = mustExist;
	}

	
	@Override
	protected String changePressed() {
		String fileName = getStringValue().trim();
		setFilterPath(new File(fileName));
		return super.changePressed();
	}
	
	
	@Override
	protected boolean doCheckState() {
		String dir = getStringValue().trim();
		if (dir.isEmpty() && isEmptyStringAllowed()) {
			return true;
		}
		
		
		File file = new File(dir);
		if(!file.isAbsolute()) {
			setErrorMessage(CpStringsUI.PathMustBeAbsolute);
			return false;
		}
		
		if(file.exists()) {
			if(!file.isDirectory()) {
				setErrorMessage(CpStringsUI.PathMustBeDirectory);
				return false;
			}
		}

		if(isDirMustExist()) {
			setErrorMessage(CpStringsUI.PathMustExist);
			return false;
		}

		try {
			Paths.get(dir);
		} catch (InvalidPathException |  NullPointerException ex) {
			setErrorMessage(CpStringsUI.PathInvalidOrNonAccesible);
			return false;
		}
		return true;
	}

	@Override
	public void setFilterPath(File path) {
		super.setFilterPath(getExistingPath(path));
	}
	
	
	/**
	 * Finds closest existing directory in the hierarchy  for the suppliedd path 
	 * @param path initial path
	 * @return an existing path or null
	 */
	public static File getExistingPath(File path) {
		if(path == null)
			return null; 
		if(path.exists() && path.isDirectory())
			return path;
		for (File f = path.getParentFile(); f != null; f = f.getParentFile()) {
			if(f.exists())
				return f;
		}
		return null;
	}
	
	
}
