/*******************************************************************************
* Copyright (c) 2014 ARM Ltd.
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
package com.arm.cmsis.pack.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;



import com.arm.cmsis.pack.events.IRteConfigurationProxy;
import com.arm.cmsis.pack.rte.IRteConfiguration;

/**
 * This class implements
 */
public class RteManagerWidget {
	private SashForm sashForm = null;
	private RteComponentTreeWidget rteComponentTreeWidget = new RteComponentTreeWidget();
	private RteValidateWidget rteValidateWidget = new RteValidateWidget();
	
	protected IRteConfigurationProxy fConfiguration = null;		// contains RteComponents
	
	
	
	public SashForm getSashForm() {
		return sashForm;
	}
	
	/**
	 * @return RTE configuration
	 */
	public IRteConfiguration getConfiguration() {
		return fConfiguration;
	}
	
	public void setConfiguration(IRteConfigurationProxy  configuration) {
		fConfiguration = configuration;
		rteComponentTreeWidget.setConfiguration(configuration);
		rteValidateWidget.setConfiguration(configuration);
		if (sashForm != null) {
			rteComponentTreeWidget.update();
			rteValidateWidget.update();
		}
	}

    /**
     * @wbp.parser.entryPoint
     */
    public void createControl(Composite parent) {
    	
    	Composite sashComposite = new Composite(parent, SWT.NONE);
    	sashComposite.setLayout(new FillLayout());
    	sashComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
    	sashForm = new SashForm(sashComposite, SWT.VERTICAL);
    	sashForm.setSashWidth(3);

    	rteComponentTreeWidget.createControl(sashForm);
    	rteValidateWidget.createControl(sashForm);
    	sashForm.setWeights(new int[] {3,1});
	}
}
