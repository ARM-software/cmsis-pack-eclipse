/*******************************************************************************
* Copyright (c) 2015 ARM Ltd.
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.arm.cmsis.pack.refclient.wizards;


import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.arm.cmsis.pack.data.ICpItem;

/**
 * A temporary page to select compiler and output, in real projects toolchain options are defined by CDT manage builder 
 */
public class ToolchainSelectorPage extends WizardPage {

	private Combo compilerCombo = null;
	private Combo outputCombo = null;
    private Composite mainComposite;

	private static final String[] toolchainNames = new String[]{ "GCC", "ARMCC", "IAR", "Tasking", "GHS", "Cosmic", "G++"};
	private static final String[] buildOuputs = new String[]{ "executable", "library"};
	
	private int initialCompiler = 1; // ARM compiler is default
	private int initialOutput = 0;
	
	public ToolchainSelectorPage() {
		super("Toolchain Options");
	    setTitle("Toolchain Options");
	    setDescription("Select compiler toolchain and build output");
	}

	

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);

	    GridLayout layout = new GridLayout();
	    mainComposite.setLayout(layout);
	    layout.numColumns = 2;

	    Label labelCompiler = new Label(mainComposite, SWT.NONE);
	    labelCompiler.setText("Compile toolchain:");
	    compilerCombo = new Combo(mainComposite, SWT.READ_ONLY);
	    compilerCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    compilerCombo.setItems(toolchainNames);
	    compilerCombo.select(initialCompiler); // select ARMCC by default
	    
	    Label labelOutput= new Label(mainComposite, SWT.NONE);
	    labelOutput.setText("Build output:");
	    outputCombo = new Combo(mainComposite, SWT.READ_ONLY);
	    outputCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    outputCombo.setItems(buildOuputs);
	    outputCombo.select(initialOutput);

	    setControl(mainComposite);
	    setPageComplete(true);

	}

	String getCompilerName() {
		int selected = 0;
		if(compilerCombo != null)
			selected = compilerCombo.getSelectionIndex();
		return toolchainNames[selected];
	};

	String getBuildOutput() {
		int selected = 0;
		if(compilerCombo != null)
			selected = outputCombo.getSelectionIndex();
		if(selected ==1)
			return "lib";
		return "exe";
	}



	public void setInitialSelection(ICpItem toolchainInfo) {
		if(toolchainInfo != null){
			String compiler = toolchainInfo.attributes().getAttribute("Tcompiler");
			for(int i = 0; i < toolchainNames.length; i++) {
				if(toolchainNames[i].equals(compiler)) {
					initialCompiler = i;
					break;
				}
			}
			String output = toolchainInfo.attributes().getAttribute("Toutput");
			if(output.equals("lib"))
				initialOutput = 1;
			else
				initialOutput = 0;
		}
	};
	
}
