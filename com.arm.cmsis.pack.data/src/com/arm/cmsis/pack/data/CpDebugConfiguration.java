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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EDebugProtocolType;

/**
 *  Convenience class to access debug/trace device configuration     
 */
public class CpDebugConfiguration extends CpItem implements ICpDebugConfiguration {

	protected Map<Integer, ICpDebug> debugItems = new HashMap<Integer, ICpDebug>();
	protected ICpTrace traceItem = null;
	protected ICpDebugVars debugVars = null;
	protected String sdfFileName = CmsisConstants.EMPTY_STRING;

	protected Map<Long, ICpDebugPort>  debugPorts = new HashMap<Long, ICpDebugPort>();
	protected Map<String, ICpSequence> sequences = new HashMap<String, ICpSequence>();
	protected Map<String, ICpMemory>   memoryItems = new HashMap<String, ICpMemory>();
	protected ICpMemory defaultMemory;
	protected ICpMemory startupMemory;
	
	protected Map<String, ICpAlgorithm> algorithms = new HashMap<String, ICpAlgorithm>();
	
	
	public CpDebugConfiguration(ICpItem parent) {
		super(parent, CmsisConstants.DEBUGCONFIG_TAG);
	}

	public CpDebugConfiguration(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public EDebugProtocolType getDefaultProtocolType() {
		String type = attributes().getAttribute(CmsisConstants.DEFAULT);
		return EDebugProtocolType.fromString(type);
	}

	@Override
	public boolean isSWJ() {
		return attributes().getAttributeAsBoolean(CmsisConstants.SWJ, true);
	}

	@Override
	public boolean hasDormantState() {
		return attributes().getAttributeAsBoolean(CmsisConstants.DORMANT, false);
	}

	@Override
	public long getDefaultClock() {
		return attributes().getAttributeAsLong(CmsisConstants.CLOCK, CmsisConstants.DEFAULT_DEBUG_CLOCK);
	}
	

	@Override
	public String getSdfFile() {
		return sdfFileName;
	}

	@Override
	public String getSvdFile() {
		ICpDebug dbgItem = getDebugItem();
		if(dbgItem != null)
			return dbgItem.getSvdFile();
		return CmsisConstants.EMPTY_STRING;
	}

	@Override
	public ICpDebug getDebugItem() {
		return getDebugItem(0);
	}

	@Override
	public ICpDebug getDebugItem(int punitIndex) {
		return debugItems.get(punitIndex);
	}

	@Override
	public ICpTrace getTraceItem() {
		return traceItem;
	}
	

	@Override
	public boolean isTraceSupported() {
		ICpTrace ti = getTraceItem();
		if(ti != null)
			return ti.isSupported();
		return false;
	}

	@Override
	public ICpDebugVars getDebugVars() {
		return debugVars;
	}
	
	
	@Override
	public Map<Long, ICpDebugPort> getDebugPorts() {
		return debugPorts;
	}

	@Override
	public ICpDebugPort getDebugPort(long index) {
		return debugPorts.get(index);
	}

	@Override
	public Map<String, ICpSequence> getSequences() {
		return sequences;
	}

	@Override
	public ICpSequence getSequence(String name) {
		return sequences.get(name);
	}

	@Override
	public Collection<ICpAlgorithm> getAlgorithms() {
		return algorithms.values();
	}

	@Override
	public Collection<ICpAlgorithm> getDefaultAlgorithms() {
		List<ICpAlgorithm> defaultAlgos = new LinkedList<ICpAlgorithm>();
		for(ICpAlgorithm a : algorithms.values()) {
			if(a.isDefault())
				defaultAlgos.add(a);
		}
		return defaultAlgos;
	}

	
	@Override
	public Map<String, ICpMemory> getMemoryItems() {
		return memoryItems;
	}

	
	@Override
	public ICpMemory getMemory(String id) {
		return memoryItems.get(id);
	}

	@Override
	public ICpMemory getDefaulMemory() {
		return defaultMemory;
	}

	@Override
	public ICpMemory getStartupMemory() {
		return startupMemory;
	}

	/**
	 * Initializes debug configuration with effective properties 
	 * @param effectiveProperties ICpItem containing effective device properties for given processor 
	 */
	public void init(ICpItem effectiveProperties) {
		processEffectiveProperties(effectiveProperties);
		if(debugPorts.isEmpty()) {
			ICpDebugPort debugPort = createDefaultDebugPort();
			debugPorts.put(debugPort.getDP(), debugPort);
		}
	}

	protected void processEffectiveProperties(ICpItem effectiveProperties) {
		if(effectiveProperties == null)
			return;
		Collection<? extends ICpItem> children = effectiveProperties.getChildren();
		if(children == null)
			return;

		for(ICpItem item : children) {
			String tag = item.getTag();
			if(tag.equals(CmsisConstants.DEBUGCONFIG_TAG)) {
				attributes().mergeAttributes(item.attributes());
				sdfFileName = item.getAbsolutePath(item.getAttribute(CmsisConstants.SDF));
			} else if(tag.equals(CmsisConstants.SEQUENCES_TAG)) {
				addSequences(item);
			} else if(item instanceof ICpDebugPort) {
				ICpDebugPort debugPort = (ICpDebugPort)item;
				debugPorts.put(debugPort.getDP(), debugPort);
			} else if(item instanceof ICpDebug) {
				ICpDebug dbgItem = (ICpDebug)item;
				int punitIndex = dbgItem.getPunitIndex();
				debugItems.put(punitIndex, dbgItem);
			} else if(item instanceof ICpTrace) {
				traceItem = (ICpTrace)item;
			} else if(item instanceof ICpDebugVars) {
				debugVars  = (ICpDebugVars)item;
			} else if(item instanceof ICpMemory) {
				ICpMemory mem = (ICpMemory)item;
				String id = mem.getId();
				if(memoryItems.containsKey(id))
					continue;
				memoryItems.put(id, mem);
				if(defaultMemory == null && mem.isDefault())
					defaultMemory = mem;
				if(startupMemory == null && mem.isStartup())
					startupMemory = mem;
			} else if(item instanceof ICpAlgorithm) {
				ICpAlgorithm a = (ICpAlgorithm)item;
				String fileName = a.getAlgorithmFile();
				if(!algorithms.containsKey(fileName))
					algorithms.put(fileName, a);
			}
		}
	}
	
	protected void addSequences(ICpItem sequencesItem) {
		Collection<? extends ICpItem> children = sequencesItem.getChildren();
		if(children == null)
			return;

		String processorName = getProcessorName();
		for(ICpItem item : children) {
			if(item instanceof ICpSequence) {
				ICpSequence s = (ICpSequence)item;
				String pname = s.getProcessorName();
				if(pname.isEmpty() || pname.equals(processorName)) { 
					String name = s.getName();
					if(!sequences.containsKey(name))
						sequences.put(name, s);
				}
			}
		}		
	}

	protected ICpDebugPort createDefaultDebugPort() {
		CpDebugPort debugPort = new CpDebugPort(null);
		addChild(new CpDebugProtocol(this, CmsisConstants.SWD));
		addChild(new CpDebugProtocol(this, CmsisConstants.JTAG));
		addChild(new CpDebugProtocol(this, CmsisConstants.CJTAG));
		return debugPort; 
	}

}
