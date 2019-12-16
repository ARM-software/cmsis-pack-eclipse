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

package com.arm.cmsis.zone.ui.editors;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.events.IRteController;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RteEventProxy;
import com.arm.cmsis.pack.item.ICmsisVisitor.VisitResult;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.OpenURL;
import com.arm.cmsis.pack.ui.console.RteConsole;
import com.arm.cmsis.pack.utils.FullDeviceName;
import com.arm.cmsis.pack.utils.Utils;
import com.arm.cmsis.zone.data.CpMemoryRegion;
import com.arm.cmsis.zone.data.CpZoneAssignment;
import com.arm.cmsis.zone.data.ICpMemoryBlock;
import com.arm.cmsis.zone.data.ICpPeripheralGroup;
import com.arm.cmsis.zone.data.ICpResourceContainer;
import com.arm.cmsis.zone.data.ICpRootZone;
import com.arm.cmsis.zone.data.ICpZone;
import com.arm.cmsis.zone.data.ICpZoneAssignment;
import com.arm.cmsis.zone.data.ICpZoneItem;
import com.arm.cmsis.zone.data.PhysicalMemoryRegion;
import com.arm.cmsis.zone.gen.FmGenerator;
import com.arm.cmsis.zone.project.CmsisZoneValidator;
import com.arm.cmsis.zone.project.ICmsisZoneValidator;
import com.arm.cmsis.zone.ui.CpZonePluginUI;
import com.arm.cmsis.zone.ui.Messages;

/**
 * Controller to back CMSIS Zone editor  
 *    
 */
public class CmsisZoneController extends RteEventProxy implements IRteController {
	
	public static final String ZONE = "com.arm.cmsis.zone."; //$NON-NLS-1$
	
	public static final String ZONE_ADDED = ZONE + "new"; //$NON-NLS-1$
	public static final String ZONE_MODIFIED = ZONE + "modified"; //$NON-NLS-1$
	public static final String ZONE_DELETED  = ZONE + "deleted"; //$NON-NLS-1$
	public static final String ZONE_BLOCK_ASSIGNED = ZONE + "block.assigned"; //$NON-NLS-1$
	public static final String ZONE_ITEM_SHOW = ZONE + "item.show"; //$NON-NLS-1$

	
	private ICpRootZone fRootZone = null;
	private boolean fbModified = false;
	
	CmsisZoneController() {
		super();
	}
	
	@Override
	public void commit() {
		fRootZone.purge();
		fRootZone.invalidateAll(); // ensure fresh state
		fRootZone.updatePartition();
		check(true);
		fbModified = false;
	}
	
	public String getZoneLabel() {
		return Messages.CmsisZoneController_Zone;
	}

	@Override
	public boolean isModified() {
		return fbModified;
	}

	public void setModified(boolean bModified) {
		fbModified = bModified;
		emitRteEvent(RteEvent.CONFIGURATION_MODIFIED, this);
	}

	public void emitShowEvent(ICpZoneItem item) {
		emitRteEvent(ZONE_ITEM_SHOW, item);
	}
	
	@Override
	public String openUrl(String url) {
		return OpenURL.open(url);
	}

	@Override
	public ICpItem getDataInfo() {
		return getRootZone();
	}

	public ICpRootZone getRootZone() {
		return fRootZone;
	}

	
	@Override
	public void setDataInfo(ICpItem info) {
		if(info instanceof ICpRootZone)
			fRootZone = (ICpRootZone)info;
		else
			fRootZone = null;
		if(fRootZone != null) {
			fRootZone.init();
			check(true);
		}
		emitRteEvent(RteEvent.CONFIGURATION_MODIFIED, this); 
	}

	@Override
	public void updateDataInfo() {
				
	}

	public ICpResourceContainer getResources() {
		if(fRootZone != null) {
			return fRootZone.getResources();
		}
		return null;
	}

	/**
	 * Assigns a memory block to specified zone or removes the assignment  
	 * @param block memory block to assign 
	 * @param zoneName zone name to assign to
	 * @param bAssign true to assign, false to remove an the existing assignment 
	 */
	public void assignBlock(ICpMemoryBlock block, String zoneName, boolean bAssign) {
		if(block == null || zoneName == null || zoneName.isEmpty())
			return;
		assignBlocks(Collections.singletonList(block), zoneName, bAssign);
	}
	
	/**
	 * Assigns memory blocks to specified zone or removes the assignment  
	 * @param blocks collection of memory blocks to assign 
	 * @param zoneName zone name to assign to
	 * @param bAssign true to assign, false to remove an the existing assignment 
	 */
	public void assignBlocks(Collection<ICpMemoryBlock> blocks, String zoneName, boolean bAssign) {
		if(blocks == null || blocks.isEmpty() || zoneName == null || zoneName.isEmpty())
			return;
		
		ICpZone zone = fRootZone.getZone(zoneName);
		if(assignBlocks(blocks, zone, bAssign)) {
			fbModified = true;
			emitRteEvent(RteEvent.CONFIGURATION_MODIFIED, this);
		}
	}

	/**
	 * Assigns memory blocks to specified zone or removes the assignment  
	 * @param blocks collection of memory blocks to assign 
	 * @param zone zone to assign to
	 * @param bAssign true to assign, false to remove an the existing assignment
	 * @return true if assignments have changed  
	 */
	protected boolean assignBlocks(Collection<ICpMemoryBlock> blocks, ICpZone zone, boolean bAssign) {
		if(blocks == null || blocks.isEmpty() || zone == null)
			return false;

		String zoneName = zone.getName();
		for(ICpMemoryBlock block : blocks) {
			ICpZoneAssignment zoneItem = zone.getZoneAssignment(block.getId());
			if(bAssign == false) {
				block.removeAssignment(zoneName);
				if(zoneItem != null) {
					zoneItem.setRemoved(true);
				}
			} else {
				if(zoneItem == null){
					zoneItem = new CpZoneAssignment(zone);
					zone.addChild(zoneItem);
					// for new items only set block name, use default mapping (not an alias) and inherit access
				}
				zoneItem.setRemoved(false);
				zoneItem.assign(block);
				block.addAssignment(zoneName, zoneItem);				
			}
		}
		return true;
	}
	
	/**
	 * Assigns blocks to a newly created zone respecting "select" configure flags 
	 * @param zone ICpZone to assign to
	 * @return true if assigned 
	 */
	protected boolean assignBlocksToNewZone(ICpZone zone) {
		if(zone == null)
			return false;

		// get "select" flags 
		boolean bSelPeripheral = fRootZone.getZoneOption(CmsisConstants.PERIPHERAL, CmsisConstants.SELECT);
		boolean bSelROM = fRootZone.getZoneOption(CmsisConstants.ROM, CmsisConstants.SELECT);
		boolean bSelRAM = fRootZone.getZoneOption(CmsisConstants.RAM, CmsisConstants.SELECT);
		List<ICpMemoryBlock> blocks = new LinkedList<>();
		for(ICpMemoryBlock block : fRootZone.getMemoryBlocksAsMap().values()){
			if(block instanceof ICpPeripheralGroup)
				continue;
			if(!bSelPeripheral && block.isPeripheral())
				continue;
			if(block.hasSubBlocks())
				continue;
			if(!bSelROM && block.isROM())
				continue;
			if(!bSelRAM && block.isRAM())
				continue;
			blocks.add(block);
		}
		boolean bModified = assignBlocks(blocks, zone, true);
		return bModified;
	}

	
	public Collection<ICpMemoryBlock> getAssignedBlocks(ICpZone zone) {
		List<ICpMemoryBlock> assignedBlocks = new LinkedList<>();
		if(fRootZone == null) {
			return assignedBlocks;
		}
		fRootZone.accept((item) -> {
			if(item instanceof ICpMemoryBlock) {
				ICpMemoryBlock block = (ICpMemoryBlock)item;
				if(block.getAssignmentCount() > 0) {
					if( zone == null || block.isAssigned(zone.getName())){ 
						assignedBlocks.add(block);
					}
				}
			}
			return VisitResult.CONTINUE;
		});
		return assignedBlocks;
	}

	public void deleteZone(ICpZone zone) {
		if(zone == null)
			return;
		zone.setRemoved(true);
		zone.setParent(null);
		getRootZone().invalidateAll(); //ensures all objects using the zone clear caches
		fbModified = true;
		emitRteEvent(ZONE_DELETED, zone); // to delete widget
		emitRteEvent(ZONE_MODIFIED, zone); // to update other widgets
	}
	
	public void updateZone(ICpZone zone, String name, String fullDeviceName, String security, String privilege, String info) {
		boolean bModified = false;
		boolean bNewZone = false;
		
		if(zone == null) {
			ICpRootZone system = getRootZone();
			zone = system.addZone(name);
			bModified = true;
			bNewZone = true;
		}

		String processorName = FullDeviceName.extractProcessoreName(fullDeviceName);		
		if(zone.updateAttribute(CmsisConstants.NAME, name)){
			bModified = true;
		}

		if(zone.updateAttribute(CmsisConstants.PNAME, processorName)){
			bModified = true;
		}
		if(zone.updateAttribute(CmsisConstants.INFO, info)){
			bModified = true;
		}
		if(zone.updateAttribute(CmsisConstants.SECURITY, security)){
			bModified = true;
		}

		if(zone.updateAttribute(CmsisConstants.PRIVILEGE, privilege)){
			bModified = true;
		}

		
		if(bModified){
			if(bNewZone) {
				//Assign memories and peripherals to the new zone
				assignBlocksToNewZone(zone);
			}
			zone.invalidate();
			getRootZone().invalidateAll();
			fbModified = true;
			if(bNewZone) {
				emitRteEvent(ZONE_ADDED, zone); //adds page
			}
			emitRteEvent(ZONE_MODIFIED, zone);	// refreshes other widgets		
		}
	}

	protected boolean deleteMemoryBlock(ICpMemoryBlock block) {
		if(block == null )
			return false;
		
		PhysicalMemoryRegion pr = block.getPhysicalRegion();
		block.removeAssignments();
		block.setParent(null);
		if(pr != null) {
			if(pr.arrangeBlocks()) {
				fbModified = true; // this is just to set a breakpoint here
			}
		}
		
		return true;
	}

	/**
	 * Deletes memory blocks
	 * @param blocks collection of blocks to remove 
	 */
	public void deleteMemoryBlocks(Collection<ICpMemoryBlock> blocks) {
		if(blocks == null || blocks.isEmpty())
			return;
		boolean bModified = false;
		for(ICpMemoryBlock block : blocks){
			if(deleteMemoryBlock(block))
				bModified = true;
		}
		if(bModified) {
			fbModified = true;
			getRootZone().invalidateAll(); //ensures all objects using the block clear caches
			check(false);
			emitRteEvent(RteEvent.CONFIGURATION_MODIFIED, this); 
		}
	}

	/**
	 * Moves the block from one region to another
	 * @param parent new parent
	 * @param block ICpMemoryBlock to move 
	 * @return true if moved
	 */
	public boolean moveBlock(ICpMemoryBlock parent, ICpMemoryBlock block) {
		if(block == null || parent == null)
			return false;
		ICpMemoryBlock oldParent = block.getParentBlock();
		if(oldParent == parent)
			return false;
		block.setParent(parent); // will remove from old parent and invalidate it
		parent.addChild(block);
		return true;
	}
	
	public void updateMemoryBlock(ICpMemoryBlock parent, ICpMemoryBlock block, Map<String, String> newAttributes, IMemoryPermissions permissions) {
		boolean bModified = false;
		
		if(block == null) {
			block = new CpMemoryRegion(parent, CmsisConstants.MEMORY_TAG);
			parent.addChild(block);
			bModified = true;
		} else if(block.getParentBlock() != parent) {
			bModified = moveBlock(parent, block); // moves the block if the parent is different 
		}
		
		String previousName = block.getName();
		
		if(block.updateAttributes(newAttributes)) {
			bModified = true;
		}

		if(!block.arePermissionsEqual(permissions)) {
			block.updatePermissions(permissions);
			bModified = true;
		}
		
			
		if(bModified){
			String newName = block.getName();
			if(!previousName.equals(newName)) {
				Map<String, ICpZoneAssignment> assignments = block.getAssignments();
				// rename assignments
				if(assignments != null) {
					for(ICpZoneAssignment a : assignments.values()) {
						a.assign(block);
					}
				}
				getRootZone().invalidateAll();
			} else if(parent != null){
				parent.invalidate();
				block.invalidate();
			} else {
				getRootZone().invalidateAll();
			}
			PhysicalMemoryRegion pr = block.getPhysicalRegion();
			if(pr != null) {
				if(pr.arrangeBlocks()) {
					fbModified = true; // this is just to set a breakpoint here
				}
			}
			check(false); // do not update console on every change
			fbModified = true;
			emitRteEvent(RteEvent.CONFIGURATION_MODIFIED, this); 
		}
	}

	public void arrangeBlocks() {
		ICpRootZone root = getRootZone();
		ICpResourceContainer rc = root.getResources();
		if(rc == null) {
			root.init(); // will also trigger arrange
			return;
		}
		boolean bModified = rc.arrangeBlocks();
		
		if(bModified){
			fbModified = true;
			emitRteEvent(RteEvent.CONFIGURATION_MODIFIED, this); 
		}
	}

	public void check(boolean outputToConsole ) {
		ICpRootZone rootZone = getRootZone();
		if(rootZone == null)
			return;
		String absFileName = rootZone.getRootFileName();
		IFile zoneFile = CpPlugInUI.getFileForLocation(absFileName);
		if(zoneFile == null)
			return; 
		ICmsisZoneValidator validator = new CmsisZoneValidator();
		RteConsole rteConsole = null;
		if(outputToConsole) {
			IProject project = zoneFile.getProject();
			rteConsole = RteConsole.openConsole(project);
			String timestamp =  Utils.getCurrentTimeStamp();
			String msg = timestamp + Messages.CmsisZoneController_Validating + absFileName + Messages.CmsisZoneController_2;
			rteConsole.outputInfo(msg);
			validator.setCmsisConsole(rteConsole);
		}

		boolean success = validator.validate(rootZone);
		if(rteConsole == null)
			return;
		if(success)
			rteConsole.outputInfo(Messages.CmsisZoneController_ValidationCompleted);
		else 
			rteConsole.outputError(Messages.CmsisZoneController_ValidationFailed);
		
	}
	
	public void generate(IFile zoneFile, IProgressMonitor monitor){
		IProject project = zoneFile.getProject();
		FmGenerator generator = new FmGenerator();
		RteConsole rteConsole = RteConsole.openConsole(project);
		generator.setCmsisConsole(rteConsole);
		String timestamp =  Utils.getCurrentTimeStamp();
		String msg = timestamp + " **** Generating files for " + project.getName() + " ..."; //$NON-NLS-1$ //$NON-NLS-2$
		rteConsole.outputInfo(msg);
		File file = zoneFile.getLocation().toFile();
		boolean success = true;
		try {
			CpZonePluginUI.removeCmsisZoneMarkers(zoneFile.getParent());
			success = generator.processZoneFile(file.getAbsolutePath(), null, null);
			CpZonePluginUI.setCmsisZoneMarkers(generator);
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		if(success)
			rteConsole.outputInfo(Messages.CmsisZoneController_GenerationCompleted);
		else 
			rteConsole.outputError(Messages.CmsisZoneController_GenerationFailed);
		rteConsole.output(CmsisConstants.EMPTY_STRING);
	}

	public void setZoneMode(String mode) {
		ICpRootZone rootZone = getRootZone();
		if(rootZone == null)
			return;
		if(rootZone.setZoneMode(mode)) {
			setModified(true);
		}
	}

	public void setZoneOption(String type, String key, boolean value) {
		ICpRootZone rootZone = getRootZone();
		if(rootZone == null)
			return;
		
		if(rootZone.setZoneOption(type, key, value)) {
			setModified(true);
		}
	}
	
}
