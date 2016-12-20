/*******************************************************************************
* Copyright (c) 2015 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.data;

import java.util.Collection;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EFileCategory;


/**
 * Default implementation of ICpComponent interface
 */
public class CpComponent extends CpItem implements ICpComponent {
	
	protected int bApi = -1; // not initialized
	protected int deviceDependent = -1; // not initialized
	protected int bExclusiveApi = -1; // not initialized
	
	/**
	 * Public constructor
	 * @param parent parent ICpItem 
	 * @param tag XML tag associated with the item 
	 */
	public CpComponent(ICpItem parent, String tag) {
		super(parent, tag);
		// inherit attributes from bundle
		if(parent != null && parent.getTag().equals(CmsisConstants.BUNDLE_TAG)) {
			attributes().mergeAttributes(parent.attributes(), CmsisConstants.C_ATTRIBUTE_PREFIX);
		}
	}
	
	/**
	 * Constructor for derived CpComponentInfo class 
	 * @param parent parent ICpItem 
	 * @param component real ICpComponent object 
	 */
	protected CpComponent(ICpItem parent, ICpComponent component) {
		super(parent, component != null? component.getTag() : CmsisConstants.COMPONENT_TAG);
	}
	
	@Override
	public String constructId() {
		// construct Component ID in the form "PackId::Vendor::Cclass.Cgroup.Cvariant(condition).Version"
		String id = CmsisConstants.EMPTY_STRING;
		if(!isApi()) {
			id += getPackId();
		}
		id += CmsisConstants.DOUBLE_COLON;
		id += getName();
		if(hasCondition()){
			id += "("; //$NON-NLS-1$
			id += getConditionId();
			id += ")"; //$NON-NLS-1$
		}
		id += ":"; //$NON-NLS-1$
		id += getVersion();
		return id; 
	}


	@Override
	protected String constructName() {
		String name = CmsisConstants.EMPTY_STRING;
		if(!isApi()) {
			name = getVendor();
		}
		if(hasAttribute(CmsisConstants.CBUNDLE)) {
			name += "."; //$NON-NLS-1$
			name += getAttribute(CmsisConstants.CBUNDLE);
		}
		name += CmsisConstants.DOUBLE_COLON;
		
		name += getAttribute(CmsisConstants.CCLASS);
		name += "."; //$NON-NLS-1$
		
		name += getAttribute(CmsisConstants.CGROUP);
		if(hasAttribute(CmsisConstants.CSUB)) {
			name += "."; //$NON-NLS-1$
			name += getAttribute(CmsisConstants.CSUB);
		}
		if(hasAttribute(CmsisConstants.CVARIANT)) {
			name += "."; //$NON-NLS-1$
			name += getAttribute(CmsisConstants.CVARIANT);
		}
		return name;
	}

	@Override
	public String getVendor() {
		if(hasAttribute(CmsisConstants.CVENDOR)) {
			return getAttribute(CmsisConstants.CVENDOR);
		}
		return super.getVendor();
	}

	@Override
	public String getVersion() {
		if(isApi()) {
			return getAttribute(CmsisConstants.CAPIVERSION);
		} 
		return getAttribute(CmsisConstants.CVERSION);
	}


	@Override
	public boolean isApi() {
		if(bApi < 0) {
			bApi = getTag().equals(CmsisConstants.API_TAG) ? 1 : 0;
		}
		return bApi > 0;
	}

	
	@Override
	public boolean isExclusive() {
		if(!isApi())
			return false;
		if (bExclusiveApi < 0) {
			bExclusiveApi = attributes().getAttributeAsBoolean(CmsisConstants.EXCLUSIVE, false) ? 1 : 0;
		}
		return bExclusiveApi > 0;
	}

	@Override
	public boolean isDeviceStartupComponent() {
		if(isApi()) {
			return false;
		}
		if(getAttribute(CmsisConstants.CCLASS).equals(CmsisConstants.Device) && 
		   getAttribute(CmsisConstants.CGROUP).equals(CmsisConstants.Startup)){ 
			String sub = getAttribute(CmsisConstants.CSUB);
			return sub == null || sub.isEmpty();
		}
		return false;
	}

	@Override
	public boolean isCmsisCoreComponent() {
		if(isApi()) {
			return false;
		}
		if(getAttribute(CmsisConstants.CCLASS).equals(CmsisConstants.CMSIS) && 
		   getAttribute(CmsisConstants.CGROUP).equals(CmsisConstants.Core)){ 
			String sub = getAttribute(CmsisConstants.CSUB);
			return sub == null || sub.isEmpty();
		}
		return false;
	}

	@Override
	public boolean isCmsisRtosComponent() {
		if(isApi()) {
			return false;
		}
		if(getAttribute(CmsisConstants.CCLASS).equals(CmsisConstants.CMSIS) && 
		   getAttribute(CmsisConstants.CGROUP).equals(CmsisConstants.RTOS)){ 
			return true;
		}
		return false;
	}

	
	@Override
	public boolean isDeviceDependent() {
		if(deviceDependent < 0) {
			if(isApi()) {
				deviceDependent = 0;
			} else {
				String cClass = getAttribute(CmsisConstants.CCLASS);   
				if(cClass.equals(CmsisConstants.Device) || super.isDeviceDependent()) {
					deviceDependent = 1;	
				} else {
					deviceDependent = 0;
				}
			}
		}
		return deviceDependent > 0;
	}
	
	@Override
	public boolean isMultiInstance() {
		return attributes().hasAttribute(CmsisConstants.MAX_INSTANCES); 
	}
	
	@Override
	public int getMaxInstances() {
		return attributes().getAttributeAsInt(CmsisConstants.MAX_INSTANCES, 1); 
	}

	@Override
	public String getDoc() {
		Collection<? extends ICpItem> allFiles = getGrandChildren(CmsisConstants.FILES_TAG);
		if(allFiles != null && ! allFiles.isEmpty()) {
			for(ICpItem item : allFiles) {
				if(item instanceof ICpFile) {
					ICpFile f = (ICpFile)item;
					if(f.getCategory() == EFileCategory.DOC) {
						return getAbsolutePath(f.getName());
					}
				}
			}
		}
		return null;
	}

	@Override
	public String getRteComponentsHCode() {
		ICpItem child = getFirstChild(CmsisConstants.RTE_COMPONENTS_H);
		if(child != null) {
			return child.getText();
		}
		return null;
	}
	
	@Override
	public ICpComponent getParentComponent(){
		return this;
	}
	

	@Override
	public boolean isBootStrap() {
		return !isGenerated() && hasAttribute(CmsisConstants.GENERATOR_TAG);
	}
	
	
	@Override
	public ICpGenerator getGenerator() {
		String id = getAttribute(CmsisConstants.GENERATOR_TAG);
		if(!id.isEmpty()) {
			ICpPack pack = getPack();
			if(pack != null)
				return pack.getGenerator(id);
		}
		return null;
	}

	@Override
	public String getGeneratorId() {
		String id = getAttribute(CmsisConstants.GENERATOR_TAG);
		if(!id.isEmpty())
			return id;
		if(!isGenerated())
			return null;
		ICpPack pack = getPack();
		if(pack == null)
			return null;
		ICpGenerator gen = pack.getGenerator(null);  
		if(gen != null)
			return gen.getId();
		return null;
	}
	
}
