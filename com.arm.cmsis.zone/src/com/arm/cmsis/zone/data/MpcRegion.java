/*******************************************************************************
* Copyright (c) 2019 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.zone.data;

import java.util.ArrayList;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpMemory;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;
import com.arm.cmsis.pack.permissions.MemoryPermissions;

/**
 * Collection of several adjacent MPC items
 */
public class MpcRegion extends CpMpc implements IMpcRegion{

	protected ArrayList<ICpMpc> fMpcItems = new ArrayList<>();
	protected Long fSize = null; // total size
	
	protected boolean fbSupportsSecurity = false;
	protected boolean fbSupportsPrivilege= false;
	
	protected ArrayList<IMemoryPermissions> fPermissionsArray = null;

	
	public MpcRegion(ICpItem parent, ICpMpc mpc) {
		super(parent, CmsisConstants.MPC);
		fStart = mpc.getStart();
		fSize = mpc.getSize(); 
		fBlockSize = mpc.getMpcBlockSize();
		fbSupportsSecurity = mpc.supportsSecurity();
		fbSupportsPrivilege = mpc.supportsPrivilege();
		attributes().setAttributeHex(CmsisConstants.START, fStart);
		attributes().setAttribute(CmsisConstants.INFO, mpc.getInfo());
		fMpcItems.add(mpc);
	}
	
	public boolean addMpc(ICpMpc mpc) {
		if(mpc == this)
			return false;
		long start = mpc.getStart();
		if( start < 0)
			return false;
		long blockSize = mpc.getMpcBlockSize();
		long size = mpc.getSize();
		if(size <=0 )
			return false;
		
		if(fMpcItems.isEmpty()) {
			// initialize from the first element
			fBlockSize = blockSize;
			fStart = start;
			fSize = size; 
			attributes().setAttributeHex(CmsisConstants.START, fStart);
		} else {
			if(supportsSecurity() != mpc.supportsSecurity())
				return false;
			if(supportsPrivilege() != mpc.supportsPrivilege())
				return false;
			if(blockSize != fBlockSize)
				return false;
			long thisStop = getStop();
			if(thisStop + 1 != start)
				return false;
			
			fSize += mpc.getSize();
		}
		attributes().setAttributeHex(CmsisConstants.SIZE, fSize);
		
		String info = getInfo();
		if(!info.isEmpty()) {
			info +=", "; //$NON-NLS-1$
		}
		info += mpc.getInfo();
		attributes().setAttribute(CmsisConstants.INFO, info);

		fMpcItems.add(mpc);
		return true;
		
	}

	@Override
	public void invalidate() {
		fPermissionsArray = null;
		super.invalidate();
	}
	
	@Override
	public void invalidatePermissions(Long offset, Long size) {
		if(fPermissionsArray == null)
			return;  // already invalidated
		if(size < 0L)
			return;
		if(offset < 0L)
			offset = 0L;
		if(size > getSize())
			size = getSize();
		
		int from = getMpcBlockIndex(offset);
		if( from > getMpcBlockCount())
			return;
		
		Long end = offset + size - 1;
		int to = getMpcBlockIndex(end) + 1;
		if( to > getMpcBlockCount())
			to = getMpcBlockCount();
		for(int i = from; i < to; i++) {
			IMemoryPermissions p = fPermissionsArray.get(i);
			p.attributes().clear();
		}
	}

	@Override
	public String constructId() {
		return getName();
	}

	@Override
	public long getStart() {
		return fStart;
	}

	@Override
	public long getSize() {
		return fSize;
	}

	@Override
	public Long getMpcBlockSize() {
		return fBlockSize;
	}
	
	
	
	@Override
	public ArrayList<IMemoryPermissions> getMpcPermissionsArray() {
		if(fPermissionsArray == null) {
			Long size = getSize();
			int blockCount = getMpcBlockCount(size);
			fPermissionsArray = new ArrayList<>(blockCount);
			// initialize with undefined
			for(int i = 0; i < blockCount; i++) {
				fPermissionsArray.add(i, new MemoryPermissions());
			}
		}
		return fPermissionsArray;
	}
	
	@Override
	public IMemoryPermissions getMpcBlockPermissions(Integer index) {
		ArrayList<IMemoryPermissions> blocks = getMpcPermissionsArray();
		if(index >= 0 && index < getMpcBlockCount()) {
			return blocks.get(index);
		}
		return null; //error
	}
	
	@Override
	public boolean setMpcBlockPermissions(Integer index, ICpMemory memory) {
		ArrayList<IMemoryPermissions> blocks = getMpcPermissionsArray();
		if(index >= 0 && index < blocks.size()) {
			if(!matchPermissions(memory, index)) 
				return false;
			IMemoryPermissions p = blocks.get(index);
			String info = p.getAttribute(CmsisConstants.INFO);
			if(info.isEmpty()) {
				p.setPermissions(memory);				
			} else {
				info += ", "; //$NON-NLS-1$
			}
			info += memory.getId();
			p.setAttribute(CmsisConstants.INFO, info);

			return true;
		}
		return false;
	}

	@Override
	public boolean setMpcPermissions(ICpMemory memory, Long mpcOffset) {
		if(memory == null) {
			return false;
		}
		
		if(mpcOffset == null || mpcOffset < 0)
			return false;

		Long offset = mpcOffset + memory.getOffset();
		int from = getMpcBlockIndex(offset);
		if(from < 0 || from > getMpcBlockCount())
			return false;
		Long size = memory.getSize();
		if( size <= 0)
			return false; // do not allocate empty blocks
		Long end = offset + size - 1;
		int to = getMpcBlockIndex(end) + 1;
		if(to > getMpcBlockCount())
			return false;
		
		for(int i = from;  i < to ; i++) {
			if(!setMpcBlockPermissions(i, memory))
				return false;
		}
		return true;
	}
	
	
	@Override
	public Long getNextAvailableOffset(IMemoryPermissions permissions, Long offset, Long size) {
		Integer index = getNextAvailableIndex(permissions, offset, size);
		if(index < 0)
			return -1L;
		return getMpcBlockOffset(index);
	}

	@Override
	public Integer getNextAvailableIndex(IMemoryPermissions permissions, Long offset, Long requiredSize) {
		int index = getMpcBlockIndex(offset);
		if(offset % getMpcBlockSize() != 0)
			index++;
		boolean fits = matchPermissions(permissions, getMpcBlockOffset(index), requiredSize);
		if(fits) {
			return index;
		}
		int count = getMpcBlockCount(requiredSize) ;
		for(int i = index; i < getMpcBlockCount() - count; i++) {
			if(isMpcBlockVacant(i, count)) { 
				return i;
			}
		}
		return -1;
	}

	
	@Override
	public Long getNextAvailableOffset(IMemoryPermissions permissions, Long offset) {
		int index = getMpcBlockIndex(offset);
		Integer maxCount = 0;
		Integer maxCountIndex = -1;
		int count = 0;
		for(int i = index; i < getMpcBlockCount(); i++) {
			if(isMpcBlockVacant(i)) { 
				count++;
				if(maxCount < count) {
					maxCount = count;
					maxCountIndex = i + 1 - count;
				}
			} else {
				count = 0;
			}
		}
		if(maxCountIndex < 0)
			return -1L;
		
		return getMpcBlockOffset(maxCountIndex);
	}

	public boolean isMpcBlockVacant(Integer index) {
		IMemoryPermissions p = getMpcBlockPermissions(index);
		if(p == null)
			return false;
		
		return !p.hasAttribute(CmsisConstants.INFO);
	}
	
	public boolean isMpcBlockVacant(Integer index, int count) {
		int to = index + count;
		if(to > getMpcBlockCount())
			return false;
		for(int i = index; i < to ; i++) {
			if(!isMpcBlockVacant(i)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Integer getMpcBlockCount() {
		return getMpcPermissionsArray().size();
	}

	
	@Override
	public Long getMpcBlockOffset(Integer index) {
		if(index < 0  || index > getMpcBlockCount())
			return -1L;
		Long offset = getMpcBlockSize() * index.longValue();
		if(offset < getSize()) {
			return offset;
		}
		return -1L;
	}

	
	@Override
	public Integer getMpcBlockIndex(Long offset) {
		if(offset == null || offset < 0 )
			return -1;
		Long blockSize =  getMpcBlockSize();
		if(blockSize <= 0)
			return -1;
		if(offset == 0L)
			return 0;
		Long index = offset / blockSize;
		return index.intValue();
	}
	
	@Override
	public Long getNextMpcBlockOffset(Long offset) {
		int index = getMpcBlockIndex(offset);
		return getMpcBlockOffset(index + 1);
	}
	
	
	@Override
	public Integer getMpcBlockCount(Long size) {
		Long blockSize =  getMpcBlockSize();
		if(blockSize <= 0)
			return -1;
		Long count = size / blockSize;
		if(count == 0 || size % blockSize != 0)
			count++;
		
		return count.intValue();
	}

	@Override
	public boolean supportsSecurity() {
		return fbSupportsSecurity;
	}

	@Override
	public boolean supportsPrivilege() {
		return fbSupportsPrivilege;
	}
	
	@Override
	public boolean matchPermissions(IMemoryPermissions p1, IMemoryPermissions p2) {
		if(p1 == null || p2 == null)
			return true;
		if(supportsSecurity()) {
    		if(p1.isSecure() != p2.isSecure())
    			return false;
    	}
    	if(supportsPrivilege()) {
    		if(p1.isPrivilegedAccess() != p2.isPrivilegedAccess())
    			return false;
    	}
    	return true;
    }
	
	@Override
	public boolean matchPermissions(IMemoryPermissions permissions, Integer index) {
		IMemoryPermissions p = getMpcBlockPermissions(index);
		if(p == null)
			return false;
		
		if(permissions == null)
			return false;
			
		if(matchPermissions(p, permissions))
			return true;
		
		return isMpcBlockVacant(index);
	}
	
	@Override
	public boolean matchPermissions(IMemoryPermissions permissions, Long offset, Long size) {
		
		int from = getMpcBlockIndex(offset);
		if(from < 0 || from > getMpcBlockCount())
			return false;
		if( size <= 0)
			return false; // do not allocate empty blocks
		Long end = offset + size - 1;
		int to = getMpcBlockIndex(end) + 1;
		if( to > getMpcBlockCount())
			return false;
		
		for(int i = from; i < to; i++) {
			if(!matchPermissions(permissions, i))
				return false;
		}
		return true;
	}


	@Override
	public ICpItem toFtlModel(ICpItem ftlParent) {
		for(ICpMpc mpc : fMpcItems) {
			toFtlModel(mpc, ftlParent);
		}
		return null; // do not add itself
	}

	protected void toFtlModel(ICpMpc mpc, ICpItem ftlParent) {
		ICpItem mpc_setup = mpc.toFtlModel(ftlParent);
		ftlParent.addChild(mpc_setup);
		// add permissions
		long offset = mpc.getStart() - getStart();
		int from = getMpcBlockIndex(offset);
		int to = from + mpc.getMpcBlockCount();

		ArrayList<IMemoryPermissions> blocks = getMpcPermissionsArray();
		for(int i = from; i < to; i++ ) {
			IMemoryPermissions p = blocks.get(i);
			if(supportsSecurity()) {
				ICpItem S_bit = new CpItem(mpc_setup, CmsisConstants.S_bit);
				mpc_setup.addChild(S_bit);
				S_bit.setText(p.isSecure() ? CmsisConstants.ONE : CmsisConstants.ZERO);
			}
			if(supportsPrivilege()) {
				ICpItem P_bit = new CpItem(mpc_setup, CmsisConstants.P_bit);
				mpc_setup.addChild(P_bit);
				P_bit.setText(p.isPrivilegedAccess() ? CmsisConstants.ONE : CmsisConstants.ZERO);
			}

			ICpItem commentItem = new CpItem(mpc_setup, CmsisConstants.bit_comment);
			mpc_setup.addChild(commentItem);
			commentItem.setText(p.getAttribute(CmsisConstants.INFO));
		}
	}		
}

