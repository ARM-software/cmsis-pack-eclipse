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


import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.arm.cmsis.pack.data.CpItem;
import com.arm.cmsis.pack.data.ICpDeviceItem;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.generic.IAttributes;
import com.arm.cmsis.pack.info.CpDeviceInfo;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.devices.IRteDeviceItem;


/**
 *  Widget to select a CMSIS device 
 */
public class RteDeviceSelectorWidget extends Composite {
	private Text text;
	private Text txtSearch;
	private Label lblVendor;
	private Label lblDevice;
	private Label lblCpu;
	private Combo comboFpu;
	private Combo comboEndian;

	private IRteDeviceItem fDevices = null;
	private IRteDeviceItem fSelectedItem = null;
	private ICpDeviceItem fSelectedDevice = null; 
	private ICpDeviceInfo fDeviceInfo = null;  
	

	private RteDeviceTreeContentProvider contentLabelProvider = null;
	private TreeViewer treeViewer;
	
	// list of listeners (e.g parent widgets to monitor events)
	private ListenerList listeners = new ListenerList();
	private String fSearchString = "";
	private String selectedFpu;
	private String selectedEndian;
	
	private boolean updatingControls = false;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public RteDeviceSelectorWidget(Composite parent, int style) {
		super(parent, SWT.NONE);
		
		contentLabelProvider = new RteDeviceTreeContentProvider();
		GridLayout gridLayout = new GridLayout(6, false);
		gridLayout.horizontalSpacing = 8;
		setLayout(gridLayout);
		
		Label lblDeviceLabel = new Label(this, SWT.NONE);
		lblDeviceLabel.setText("Device:");
		
		lblDevice = new Label(this, SWT.NONE);
		lblDevice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);
		
		Label lblCpuLabel = new Label(this, SWT.NONE);
		lblCpuLabel.setText("CPU:");
		lblCpuLabel.setBounds(0, 0, 36, 13);
		
		lblCpu = new Label(this, SWT.NONE);
		lblCpu.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblCpu.setBounds(0, 0, 32, 13);
		new Label(this, SWT.NONE);
		
		Label lblVendorLabel = new Label(this, SWT.NONE);
		lblVendorLabel.setText("Vendor:");
		
		lblVendor = new Label(this, SWT.NONE);
		lblVendor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);
		
		Label lblFpuLabel = new Label(this, SWT.NONE);
		lblFpuLabel.setText("FPU:");
		lblFpuLabel.setBounds(0, 0, 38, 13);
		
		comboFpu = new Combo(this, SWT.READ_ONLY);
		comboFpu.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(updatingControls)
					return;
				int index = comboFpu.getSelectionIndex();
				selectedFpu = fpuIndexToString(index); 
			}
		});
		comboFpu.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);
		
		Label lblSearch = new Label(this, SWT.NONE);
		lblSearch.setText("Search:");
		
		txtSearch = new Text(this, SWT.BORDER);
		txtSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				setSearchText(txtSearch.getText());
			}
		});
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtSearch.setToolTipText("Enter search mask to filter device tree");
		new Label(this, SWT.NONE);
		
		
		Label lblEndian = new Label(this, SWT.NONE);
		lblEndian.setText("Endian:");
		lblEndian.setBounds(0, 0, 37, 13);
		
		comboEndian = new Combo(this, SWT.READ_ONLY);
		comboEndian.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(updatingControls)
					return;
				selectedEndian = adjustEndianString(comboEndian.getText());
			}
		});
		comboEndian.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);
		
		treeViewer = new TreeViewer(this, SWT.BORDER);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleTreeSelectionChanged(event);
			}
		});
		Tree tree = treeViewer.getTree();
		GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_tree.minimumWidth = 240;
		tree.setLayoutData(gd_tree);
		
		treeViewer.setContentProvider(contentLabelProvider);
		treeViewer.setLabelProvider(contentLabelProvider);
		treeViewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, 	Object element) {
				if(fSearchString.isEmpty() || fSearchString.equals("*")) {
					return true;
				}
				if(element instanceof IRteDeviceItem) {
					String s = fSearchString;
					if(!s.endsWith("*"))
						s += "*";
					IRteDeviceItem deviceItem = (IRteDeviceItem)element;
					return deviceItem.getFirstItem(s) != null;
				}
				return false;
			}
			
		});
		
		text = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gd_text.widthHint = 240;
		gd_text.minimumWidth = 200;
		text.setLayoutData(gd_text);
		text.setEditable(false);
		
		setTabList(new Control[]{txtSearch, tree, comboFpu, comboEndian});
		
		addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				fSelectedItem = null;
				contentLabelProvider = null;
				fDevices = null;
				listeners.clear();
				listeners = null;
			}
		});
		
	}

	private boolean setSearchText(String s) {
		// Search must be a substring of the existing value
		if(fSearchString  !=  s){
			fSearchString = s;
			IRteDeviceItem deviceItem = null;
			if(!fSearchString.isEmpty() && !fSearchString.equals("*")) {
				String pattern = fSearchString;
				if(!pattern.endsWith("*"))
					pattern += "*";
				if(fSelectedItem != null && fSelectedItem.getFirstItem(pattern) == fSelectedItem){
					deviceItem = fSelectedItem; 
				} else {
				 deviceItem = fDevices.getFirstItem(pattern);
				}
			}
			refreshTree();
			if(deviceItem != null) {
				selectItem(deviceItem);
				treeViewer.setExpandedState(deviceItem, true);
			}
		}
		return false;
	}
	
	private void refreshTree()
	{
		treeViewer.refresh();
	}
	
	
	/**
	 * @param fDevices
	 */
	public void setDevices(IRteDeviceItem devices) {
		this.fDevices = devices;
		treeViewer.setInput(devices);
	}

	/**
	 * @return the fSelectedItem
	 */
	public IRteDeviceItem getSelectedDeviceItem() {
		if(fSelectedDevice != null)
			return fSelectedItem;
		return null;
	}

	
	protected void handleTreeSelectionChanged(SelectionChangedEvent event) {
		IRteDeviceItem selectedItem = getSelectedItem();
		if(selectedItem == fSelectedItem)
			return;

		fSelectedItem = selectedItem; 
		updateDeviceInfo();
		updateControls();
	}
	
	/**
	 * 
	 */
	protected void updateControls() {
		updatingControls = true;
		String description = "";
		String vendorName = IAttributes.EMPTY_STRING;
		String deviceName = IAttributes.EMPTY_STRING;
		String cpu = IAttributes.EMPTY_STRING;
			
		if(fSelectedItem != null) {
			IRteDeviceItem vendorItem = fSelectedItem.getVendorItem();
			if(vendorItem != null)
				vendorName = vendorItem.getName();
			if(fSelectedItem.isDevice())
				deviceName = fSelectedItem.getName();
			
			description = fSelectedItem.getDescription();
		}

		String message = null;
		
		if(fDeviceInfo != null) {
			cpu = fDeviceInfo.attributes().getAttribute("Dcore", cpu);  
		} else {
			message = "No device selected";
		}
		
		lblVendor.setText(vendorName);
		lblDevice.setText(deviceName);
		text.setText(description);
		lblCpu.setText(cpu);
		
		updateFpu();
		updateEndian();

		updateStatus(message);
		updatingControls = false;
	}


	/**
	 * 
	 */
	private void updateDeviceInfo() {
		if(fSelectedItem != null && fSelectedItem.isDevice()) {
			fSelectedDevice = fSelectedItem.getDevice();
			fDeviceInfo = null;
			ICpItem props = fSelectedItem.getEffectiveProperties();
			if(props != null) {
				fDeviceInfo = new CpDeviceInfo(null, fSelectedDevice);
			}
		} else {
			fSelectedDevice = null;
			fDeviceInfo = null;
		}
	}

	/**
	 *  Returns IRteDeviceItem device item currently selected in the tree
	 */
	private IRteDeviceItem getSelectedItem() {
		IStructuredSelection sel= (IStructuredSelection)treeViewer.getSelection();
		if(sel.size() == 1) {
			Object o = sel.getFirstElement();
			if(o instanceof IRteDeviceItem ){
				return (IRteDeviceItem)o;
			}
		}
		return null;
	}

	/**
	 * 
	 */
	private void updateEndian() {
		String endian = null;
		if(fDeviceInfo != null)
			endian = fDeviceInfo.attributes().getAttribute("Dendian", IAttributes.EMPTY_STRING);
		
		String[] endianStrings = getEndianStrings(endian);
		comboEndian.setItems(endianStrings);
		
		endian = adjustEndianString(selectedEndian);
		int index = comboEndian.indexOf(endian);
		if(index < 0 || index >= endianStrings.length)
			index = 0;
		
		comboEndian.select(index);
		comboEndian.setEnabled(endianStrings.length > 1);
	}

	private String[] getEndianStrings(String endian){
		if(endian != null) {
			switch(endian){
			case "Big-endian":
				return new String[]{"Big-endian"};
			case "Configurable":
			case "*":
				return new String[]{"Little-endian", "Big-endian"};
			case "Little-endian":
			default:
				return new String[]{"Little-endian"};
			}
		}
		return new String[]{""};
	}
	private String adjustEndianString(String endian){
		if(endian != null)
			if(endian.equals("Big-endian") || endian.equals("Little-endian"))
				return endian;
		return "Little-endian";
	}
	
	
	/**
	 * 
	 */
	private void updateFpu() {
		String fpu = null;
		if(fDeviceInfo != null)
			fpu = fDeviceInfo.attributes().getAttribute("Dfpu", IAttributes.EMPTY_STRING);
		
		String[] fpuStrings = getFpuStrings(fpu);
		int index = fpuStringToIndex(selectedFpu);
		if(index < 0 || index >= fpuStrings.length)
			index = fpuStringToIndex(fpu);
		if(index < 0)
			index = 0;
		
		comboFpu.setItems(fpuStrings);
		comboFpu.select(index);
		comboFpu.setEnabled(fpuStrings.length > 1);
	}
	
	private String[] getFpuStrings(String fpu){
		if(fpu != null) {
			switch(fpu){
			case "1":				
			case "FPU":
			case "SP_FPU":
				return new String[]{"none", "single precision"};
			case "DP_FPU":
				return new String[]{"none", "single precision", "double precision"};
			default:
				return new String[]{"none"};
			}
		}
		return new String[]{""};
	}

	private String fpuIndexToString(int index){
		switch(index){
		case 1:
			return "SP_FPU";
		case 2:
			return "DP_FPU";
		default:
			break;
		}
		return "NO_FPU";		
	}

	private int fpuStringToIndex(String fpu){
		if(fpu != null) {
			switch(fpu){
			case "1":				
			case "FPU":
			case "SP_FPU":
				return 1;
			case "DP_FPU":
				return 2;
			default:
				return 0;
			}
		}
		return -1;
	}
	

	private void updateStatus(String message) {
		// notify listeners
		for (Object obj : listeners.getListeners()) {
			IStatusListener listener = (IStatusListener) obj;
			try {
				listener.updateStatus(message);
			} catch (Exception ex) {
				removeListener(listener);
			}
		} 
	}
	
	public void addListener(IStatusListener listener){
		listeners.add(listener);
	}
	public void removeListener(IStatusListener listener){
		listeners.remove(listener);
	}
	
	
	 public interface IStatusListener {
		 void updateStatus(String message);
	 }


	/**
	 * @return
	 */
	public ICpDeviceInfo getDeviceInfo() {
		if(fDeviceInfo != null){
			int index = comboFpu.getSelectionIndex();
			String fpu = fpuIndexToString(index);
			fDeviceInfo.attributes().setAttribute("Dfpu", fpu);
			
			String endian = comboEndian.getText();
			if(endian == null || endian.isEmpty())
				endian = "Little-endian";
			
			fDeviceInfo.attributes().setAttribute("Dendian", endian);
		}
		return fDeviceInfo;
	}

	public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
		fDeviceInfo = deviceInfo;
		if(fDeviceInfo != null) {
			selectedFpu = fDeviceInfo.attributes().getAttribute("Dfpu");
			selectedEndian = fDeviceInfo.attributes().getAttribute("Dendian");
			// set initial selection
			IRteDeviceItem item = fDevices.findItem(fDeviceInfo.attributes());
			if(item != null ){
				selectItem(item);
			} else {
				String message = "Device '" + CpItem.getDeviceName(deviceInfo.attributes()) + "' not found"; 
				updateStatus(message);
			}
			updateControls();
		}
	}
	
	/**
	 * Selects given device item in the tree
	 * @param item device item to select
	 */
	public void selectItem(IRteDeviceItem item){
		if(item != null ){
			Object[] path = item.getHierachyPath();
			TreePath tp = new TreePath(path);
			TreeSelection ts = new TreeSelection(tp);
			
			treeViewer.setSelection(ts, true);
		}
	}

}
