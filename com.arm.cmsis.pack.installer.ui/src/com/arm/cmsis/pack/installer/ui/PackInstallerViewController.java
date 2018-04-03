/*******************************************************************************
* Copyright (c) 2016 ARM Ltd. and others
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* ARM Ltd and ARM Germany GmbH - Initial API and implementation
*******************************************************************************/

package com.arm.cmsis.pack.installer.ui;

import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.ICpEnvironmentProvider;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpExample;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack;
import com.arm.cmsis.pack.events.RteEvent;
import com.arm.cmsis.pack.events.RteEventProxy;
import com.arm.cmsis.pack.installer.ui.views.PackInstallerView;
import com.arm.cmsis.pack.installer.ui.views.PackPropertyView;
import com.arm.cmsis.pack.rte.boards.IRteBoardItem;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;
import com.arm.cmsis.pack.rte.examples.IRteExampleItem;

/**
 *  Class responsible for synchronizing Pack Installer Views
 */
public class PackInstallerViewController extends RteEventProxy implements ISelectionListener {

	public final static String INSTALLER_UI_FILTER_CHANGED = "installer.ui.filter.changed"; //$NON-NLS-1$
	public final static String INSTALLER_UI_PACK_CHANGED = "installer.ui.pack.changed"; //$NON-NLS-1$
	protected PackInstallerViewFilter fFilter = null;
	protected ICpPack fSelectedPack = null;

	public PackInstallerViewController() {
	}

	public void clear() {
		fSelectedPack = null;
		removeAllListeners();
		if(fFilter != null) {
			fFilter.clear();
			fFilter = null;
		}
	}


	@Override
	public void handle(RteEvent event) {
		if(event.getTopic().equals(RteEvent.PACKS_RELOADED)) {
			fSelectedPack = null;
			if (fFilter != null) {
				fFilter.clear();
			}
		}
		super.handle(event);
	}

	public ICpPack getSelectedPack() {
		return fSelectedPack;
	}

	public PackInstallerViewFilter getFilter() {
		if(fFilter == null) {
			fFilter = createFilter();
		}
		return fFilter;
	}

	public PackInstallerViewFilter createFilter() {
		return new PackInstallerViewFilter();
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if(part instanceof PackInstallerView) {
			updateSelectedPack((IStructuredSelection)selection);
			PackInstallerView view = (PackInstallerView)part;
			if(view.isFilterSource()) {
				boolean changed = getFilter().setSelection(part, (IStructuredSelection) selection);
				if(changed) {
					emitRteEvent(INSTALLER_UI_FILTER_CHANGED, null);
				}
			}
		}
	}

	static public ICpPack getPackFromSelection(ISelection selection) {
		if(selection == null || !(selection instanceof IStructuredSelection)) {
			return null;
		}
		IStructuredSelection sel = (IStructuredSelection) selection;
		if(sel.size() == 1) {
			return getPackFromObject(sel.getFirstElement());
		}
		return null;
	}

	static public ICpPack getPackFromObject(Object o) {
		if(o == null) {
			return null;
		}
		if(o instanceof IStructuredSelection) {
			return getPackFromSelection((ISelection) o);
		}
		ICpItem item = null;
		if(o instanceof ICpItem) {
			item = (ICpItem)o;
		} else 	if(o instanceof IRteBoardItem) {
			IRteBoardItem board = (IRteBoardItem)o;
			item = board.getBoard();
		} else if(o instanceof IRteDeviceItem) {
			IRteDeviceItem device = (IRteDeviceItem)o;
			item = device.getDevice();
		} else if(o instanceof IRteExampleItem) {
			IRteExampleItem example = (IRteExampleItem)o;
			item = example.getExample();
		}
		if(item != null) {
			return item.getPack();
		}
		return null;
	}



	protected void updateSelectedPack(IStructuredSelection selection) {
		ICpPack pack = getPackFromSelection(selection);
		if(fSelectedPack == pack) {
			return;
		}
		fSelectedPack = pack;
		emitRteEvent(INSTALLER_UI_PACK_CHANGED, fSelectedPack);
	}


	public void showPackProperties(ISelection selection) {
		if(selection != null && selection instanceof IStructuredSelection) {
			updateSelectedPack((IStructuredSelection)selection);
		}
		IWorkbench wb = PlatformUI.getWorkbench();
		if(wb == null) {
			return;
		}
		IWorkbenchWindow wbw = wb.getActiveWorkbenchWindow();
		if(wbw == null || wbw.getActivePage() == null) {
			return;
		}
		try {
			wbw.getActivePage().showView(PackPropertyView.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

	}

	public void copyExample(ICpExample cpExample) {
		ICpEnvironmentProvider envProvider = CpPlugIn.getEnvironmentProvider();
		if(envProvider == null) {
			return;
		}
		IAdaptable copyResult = envProvider.copyExample(cpExample);
		if(copyResult == null) {
			return;
		}
		@SuppressWarnings("cast")
		IProject project = (IProject) copyResult.getAdapter(IProject.class);
		if (project == null) {
			return;
		}

		Optional<String> perspectiveId = envProvider.getCopyExamplePerspectiveSwitchId();
		if (!perspectiveId.isPresent())
		    return;
		        
		IWorkbench wb = PlatformUI.getWorkbench();
		if (wb != null) {
			IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
			if (window != null) {
				IPerspectiveDescriptor persDescription = wb.getPerspectiveRegistry().findPerspectiveWithId(perspectiveId.get());
				IWorkbenchPage page = window.getActivePage();
				if (page != null && persDescription != null) {
					page.setPerspective(persDescription);
					try {
						String rteConf = project.getName()	+ '.' + CmsisConstants.RTECONFIG;
						IResource r = project.findMember(rteConf);
						if(r != null && r.exists() && r.getType() == IResource.FILE) {
							IDE.openEditor(page, project.getFile(rteConf));
						}
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}


}
