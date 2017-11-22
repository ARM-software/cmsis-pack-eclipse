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

package com.arm.cmsis.pack.rte.dependencies;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.arm.cmsis.pack.CpStrings;
import com.arm.cmsis.pack.enums.EEvaluationResult;
import com.arm.cmsis.pack.info.ICpComponentInfo;
import com.arm.cmsis.pack.info.ICpPackInfo;
import com.arm.cmsis.pack.rte.RteConstants;
import com.arm.cmsis.pack.rte.components.IRteComponentItem;

/**
 * Default implementation of IRteDependencyResult
 */
public class RteDependencyResult extends RteDependencyItem implements IRteDependencyResult {

	Set<IRteDependency> fDependencies = new LinkedHashSet<IRteDependency>();
	
	/**
	 *	Constructs DependencyResult with  associated component item 
	 */
	public RteDependencyResult(IRteComponentItem componentItem) {
		super(componentItem);
	}
	
	@Override
	public Collection<IRteDependency> getChildren() {
		return getDependencies();
	}
	
	@Override
	public Collection<IRteDependency> getDependencies() {
		return fDependencies;
	}


	@Override
	public void addDependency(IRteDependency dependency) {
		if(dependency == null)
			return;
		if(fDependencies.contains(dependency))
			return;
		if(dependency.isResolved())
			return;
		fDependencies.add(dependency);
	}

	@Override
	public void removeDependency(IRteDependency dependency) {
		if(dependency == null)
			return;
		if(!fDependencies.contains(dependency))
			return;
		fDependencies.remove(dependency);
	}


	@Override
	public void setEvaluationResult(EEvaluationResult result) {
		super.setEvaluationResult(result);
		purgeDependencies();
		
	}

	/**
	 *  Removes dependencies that are greater than overall result since they are irrelevant
	 */
	protected void purgeDependencies() {
		int thisOrdinal = getEvaluationResult().ordinal();
		for (Iterator<IRteDependency> iterator = fDependencies.iterator(); iterator.hasNext();) {
			IRteDependency d = iterator.next();
			EEvaluationResult r = d.getEvaluationResult();
			if(r.ordinal() > thisOrdinal) {
				iterator.remove();
				cachedChildArray = null;
			}
		}
	}


	@Override
	public String getDescription() {
		EEvaluationResult res = getEvaluationResult();
		if(!fDependencies.isEmpty()) {
			switch(res) {
			case INSTALLED:
			case MISSING:
			case SELECTABLE:
			case UNAVAILABLE:
			case UNAVAILABLE_PACK:
				return CpStrings.RteDependencyResult_AdditionalComponentRequired;
			case INCOMPATIBLE:
			case FAILED:
				return CpStrings.RteDependencyResult_ComponentConficts;
			default:
				break;
			}
		}
		String s = null; 
		if( fComponentItem != null && !res.isFulfilled() ) {
			ICpComponentInfo ci = fComponentItem.getActiveCpComponentInfo();
			if(ci != null && ci.getComponent() == null) {
				s = RteConstants.getDescription(EEvaluationResult.MISSING);
				s += ". "; //$NON-NLS-1$
				s += RteConstants.getDescription(res);
				if(res == EEvaluationResult.UNAVAILABLE_PACK) {
					s += ": ";  //$NON-NLS-1$
					ICpPackInfo pi = ci.getPackInfo();
					String id = pi.isVersionFixed() ? pi.getId() : pi.getPackFamilyId();
					s += id;
				}
				return s;
			}
		} 
		s = RteConstants.getDescription(res);
		
		if(s != null)
			return s;
		return super.getDescription();
	}

}
