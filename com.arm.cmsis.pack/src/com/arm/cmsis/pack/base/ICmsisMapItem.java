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

package com.arm.cmsis.pack.base;

import com.arm.cmsis.pack.generic.ITreeItem;
import com.arm.cmsis.pack.generic.ITreeMapItem;

/**
 * Basic generic interface for CMSIS items with String-to-Item map collections 
 */
public interface ICmsisMapItem<T extends ITreeItem<T> & ICmsisItem> extends ICmsisTreeItem<T>, ITreeMapItem<String, T > {
	 
}
