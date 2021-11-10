/** 
 * Copyright (c) 2021 ARM Ltd. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * ARM Ltd and ARM Germany GmbH - Initial API and implementation
 */
package com.arm.cmsis.pack.debugseq.generator

import java.util.Collection
import java.util.Map
import org.eclipse.core.runtime.CoreException
import com.arm.cmsis.pack.CpAbstractExtensionFactory

class DsqScriptGeneratorFactory extends CpAbstractExtensionFactory<IDsqScriptGenerator> {
	public static final String SCRIPT_GENERATOR = "ScriptGenerator"
	private static DsqScriptGeneratorFactory theFactory = null
	private Map<String, IDsqScriptGenerator> fDsqScriptGenerators = newHashMap

	protected new(String extensionPointId) {
		super(extensionPointId)
	}
	
	override String getExtensionPointId(String elementId) {
		"com.arm.cmsis.pack.dsq.engine." + elementId;
	}

	def static DsqScriptGeneratorFactory getInstance() {
		if (theFactory === null) {
			theFactory = new DsqScriptGeneratorFactory(SCRIPT_GENERATOR)
		}
		return theFactory
	}

	def synchronized IDsqScriptGenerator getExtender(String generatorID) {
		if (fDsqScriptGenerators.containsKey(generatorID)) {
			return fDsqScriptGenerators.get(generatorID)
		}
		if (elements.containsKey(generatorID)) {
			try {
				var IDsqScriptGenerator generator = createExtender(elements.get(generatorID))
				if (generator !== null) {
					fDsqScriptGenerators.put(generatorID, generator)
					return generator
				}
			} catch (CoreException e) {
				e.printStackTrace()
			}

		}
		return null
	}
	
	def Collection<IDsqScriptGenerator> getDsqScriptGenerators() {
		return fDsqScriptGenerators.values()
	}

	override protected IDsqScriptGenerator castToExtenderClass(Object extender) {
		if (extender instanceof IDsqScriptGenerator) {
			return extender
		}
		return null
	}
}
