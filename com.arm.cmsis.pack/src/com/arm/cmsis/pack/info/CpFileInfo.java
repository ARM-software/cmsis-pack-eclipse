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

package com.arm.cmsis.pack.info;

import com.arm.cmsis.pack.data.CpFile;
import com.arm.cmsis.pack.data.ICpFile;
import com.arm.cmsis.pack.data.ICpItem;

/**
 *
 */
public class CpFileInfo extends CpFile implements ICpFileInfo {

	ICpFile fFile = null;
	
	public CpFileInfo(ICpItem parent, ICpFile file) {
		super(parent, file.getTag());
		fFile = file;
		attributes().setAttributes(file.attributes());
	}

	
	/**
	 * @param parent
	 * @param tag
	 */
	public CpFileInfo(ICpItem parent, String tag) {
		super(parent, tag);
	}

	@Override
	public ICpFile getFile() {
		return fFile;
	}


	@Override
	public void setFile(ICpFile file) {
		fFile = file;
	}


	@Override
	public ICpComponentInfo getComponentInfo() {
		for( ICpItem parent = getParent(); parent != null; parent = parent.getParent()){
			if(parent instanceof ICpComponentInfo)
				return (ICpComponentInfo)parent;
		}
		return null;
	}

	
	

}
