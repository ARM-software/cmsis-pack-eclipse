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

package com.arm.cmsis.pack.ui.widgets;


import java.util.Collection;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.arm.cmsis.pack.CpPlugIn;
import com.arm.cmsis.pack.DeviceVendor;
import com.arm.cmsis.pack.ICpPackManager;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpBoard;
import com.arm.cmsis.pack.data.ICpItem;
import com.arm.cmsis.pack.data.ICpPack.PackState;
import com.arm.cmsis.pack.generic.ITreeObject;
import com.arm.cmsis.pack.info.ICpDeviceInfo;
import com.arm.cmsis.pack.rte.IRteModelController;
import com.arm.cmsis.pack.ui.CpPlugInUI;
import com.arm.cmsis.pack.ui.CpStringsUI;
import com.arm.cmsis.pack.ui.OpenURL;
import com.arm.cmsis.pack.ui.tree.AdvisedCellLabelProvider;
import com.arm.cmsis.pack.ui.tree.TreeObjectContentProvider;

import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.jface.viewers.TreeViewer;


/**
 *  Widget to display information about selected device 
 */
public class RteDeviceInfoWidget extends Composite {
	private Text text;
	private Table table;
	private TableViewer tableViewer;
	
	private Label lblVendor;
	private Label lblDevice;
	private Label lblCpu;
	private Label lblMemory;
	private Label lblPack;
	private Label lblClock;
	private Label lblFpu;
	private Label lblEndian;
	private Link linkUrl;
	
	ICpDeviceInfo fDeviceInfo = null;  
	String url = CmsisConstants.EMPTY_STRING;
	private Button btnSelect;
	private Label lblDescription;
	private Label lblFamilyLabel;
	private Label lblSubfamilyLabel;
	private Label lblFamily;
	private Label lblSubfamily;
	private Tree tree;
	private TreeViewer treeViewer;
	private Label lblBoards;
	protected IRteColumnAdvisor fBookColumnAdvisor = null;
	protected IRteColumnAdvisor fBoardColumnAdvisor = null;


	ICpItem getCpItem(Object obj) {
		if (obj instanceof ICpItem) {
			return (ICpItem)obj;
		}
		return null;
	}

	ICpBoard getCpBoard(Object obj) {
		if (obj instanceof ICpBoard) {
			return (ICpBoard)obj;
		}
		return null;
	}

	
	public class RteBookContentProvider extends TreeObjectContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if(fDeviceInfo != null && fDeviceInfo == inputElement) {
				Collection<ICpItem> books = fDeviceInfo.getBooks();
				if(books != null && ! books.isEmpty())
					return books.toArray();
			}
			return ITreeObject.EMPTY_OBJECT_ARRAY;
		}
	}

	public class RteBoardContentProvider extends TreeObjectContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if(inputElement instanceof Collection<?>) {
				Collection<?> col = (Collection<?>)inputElement;
				return col.toArray();
			}
			return ITreeObject.EMPTY_OBJECT_ARRAY;
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			ICpBoard b = getCpBoard(parentElement);
			if(b != null) {
				if(b.getPack().getPackState() == PackState.INSTALLED) {
					Collection<ICpItem> books = b.getBooks();
					if(books != null && !books.isEmpty()) {
						return books.toArray();
					}
				}
			}
			return ITreeObject.EMPTY_OBJECT_ARRAY;
		}

		@Override
		public boolean hasChildren(Object element) {
			ICpBoard b = getCpBoard(element);
			if(b != null) 
				return getChildren(element).length > 0;
			return false;
		}
	}
	
	public class RteBoardLabelProvider extends LabelProvider{
		@Override
		public Image getImage(Object element) {
			ICpItem item = getCpItem(element);
			if(item != null) {
				if(item.getTag().equals(CmsisConstants.BOOK_TAG)) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_BOOK);
				}
				return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD);
			}
			return null;
		}

		@Override
		public String getText(Object element) {
			ICpItem item = getCpItem(element);
			if(item != null) {
				if(item.getTag().equals(CmsisConstants.BOOK_TAG))
					return item.getAttribute(CmsisConstants.TITLE);
				return item.getName();
			}
			return CmsisConstants.EMPTY_STRING;	
		}
	}
	
	/**  
	 * Column label provider for books tree 
	 */
	class RteBookColumnAdvisor extends RteColumnAdvisor {
		/**
		 * Constructs advisor for a viewer
		 * @param columnViewer ColumnViewer on which the advisor is installed
		 */
		public RteBookColumnAdvisor(ColumnViewer columnViewer) {
			super(columnViewer);
		}

		@Override
		public CellControlType getCellControlType(Object obj, int columnIndex) {
			ICpItem item = getCpItem(obj);
			if(item == null)
				return CellControlType.NONE;
			if(columnIndex == 0){
				String url = item.getUrl();
				if(url != null && ! url.isEmpty()) 
					return CellControlType.URL;
			}
			return CellControlType.TEXT;
		}

		@Override
		public String getString(Object obj, int columnIndex) {
			ICpItem item = getCpItem(obj);		
			if(columnIndex == 0 && item != null) {
				if(item.getTag().equals(CmsisConstants.BOOK_TAG))
					return item.getAttribute(CmsisConstants.TITLE);
				return item.getName();
			}
			return CmsisConstants.EMPTY_STRING;	
		}
		
		@Override
		public Image getImage(Object obj, int columnIndex) {
			ICpItem item = getCpItem(obj);
			if(columnIndex == 0 && item != null)
				if(item.getTag().equals(CmsisConstants.BOOK_TAG)) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_BOOK);
				} else if(item.getTag().equals(CmsisConstants.BOARD_TAG)) {
					return CpPlugInUI.getImage(CpPlugInUI.ICON_BOARD);
				}
			return null;
		}


		@Override
		public String getTooltipText(Object obj, int columnIndex) {
			return getUrl(obj, columnIndex);
		}

		@Override
		public String getUrl(Object obj, int columnIndex) {
			ICpItem item = getCpItem(obj);
			if(columnIndex == 0 && item != null)
				return item.getUrl();
			return null;
		}
	} /// end of ColumnAdviser


	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public RteDeviceInfoWidget(Composite parent) {
		super(parent, SWT.BORDER);
		
		GridLayout gridLayout = new GridLayout(6, false);
		gridLayout.horizontalSpacing = 8;
		setLayout(gridLayout);
		
		Label lblDeviceLabel = new Label(this, SWT.NONE);
		lblDeviceLabel.setText(CpStringsUI.RteDeviceSelectorWidget_DeviceLabel);
		
		lblDevice = new Label(this, SWT.NONE);
		lblDevice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		FontDescriptor boldDescriptor = FontDescriptor.createFrom(lblDevice.getFont()).setStyle(SWT.BOLD).increaseHeight(1);
		Font boldFont = boldDescriptor.createFont(lblDevice.getDisplay());
		lblDevice.setFont( boldFont );
		
		btnSelect = new Button(this, SWT.NONE);
		btnSelect.setText(CpStringsUI.RteDeviceInfoWidget_btnSelect_text);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		lblFamilyLabel = new Label(this, SWT.NONE);
		lblFamilyLabel.setText(CpStringsUI.RteDeviceInfoWidget_lblFamily_text);
		
		lblFamily = new Label(this, SWT.NONE);
		lblFamily.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(this, SWT.NONE);
    	
		Label lblCpuLabel = new Label(this, SWT.NONE);
		lblCpuLabel.setText(CpStringsUI.RteDeviceSelectorWidget_CPULabel);
		lblCpuLabel.setBounds(0, 0, 36, 13);
		
		lblCpu = new Label(this, SWT.NONE);
		lblCpu.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblCpu.setBounds(0, 0, 32, 13);
		new Label(this, SWT.NONE);
		
		lblSubfamilyLabel = new Label(this, SWT.NONE);
		lblSubfamilyLabel.setText(CpStringsUI.RteDeviceInfoWidget_lblSubFamily_text);
		
		lblSubfamily = new Label(this, SWT.NONE);
		lblSubfamily.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(this, SWT.NONE);
		
		Label lblClocklabel = new Label(this, SWT.NONE);
		lblClocklabel.setText(CpStringsUI.RteDeviceSelectorWidget_lblClocklabel_text);
		
		lblClock = new Label(this, SWT.NONE);
		lblClock.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(this, SWT.NONE);
		
		Label lblVendorLabel = new Label(this, SWT.NONE);
		lblVendorLabel.setText(CpStringsUI.RteDeviceSelectorWidget_VendorLabel);
		
		lblVendor = new Label(this, SWT.NONE);
		lblVendor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(this, SWT.NONE);
		
		Label lblMemoryLabel = new Label(this, SWT.NONE);
		lblMemoryLabel.setText(CpStringsUI.RteDeviceSelectorWidget_lblMemory);
		
		lblMemory = new Label(this, SWT.NONE);
		lblMemory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(this, SWT.NONE);
		
		Label lblPackLabel = new Label(this, SWT.NONE);
		lblPackLabel.setText(CpStringsUI.RteDeviceSelectorWidget_lblPack_text);
		
		lblPack = new Label(this, SWT.NONE);
		lblPack.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(this, SWT.NONE);
		
		Label lblFpuLabel = new Label(this, SWT.NONE);
		lblFpuLabel.setText(CpStringsUI.RteDeviceSelectorWidget_FPULabel);
		lblFpuLabel.setBounds(0, 0, 38, 13);
		
		lblFpu = new Label(this, SWT.NONE);
		lblFpu.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(this, SWT.NONE);
		
		Label lblUrlLabel = new Label(this, SWT.NONE);
		lblUrlLabel.setText(CpStringsUI.RteDeviceSelectorWidget_lblUrl);
		
		linkUrl = new Link(this, SWT.NONE);
		linkUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		linkUrl.addSelectionListener(new SelectionAdapter(){
	    	 @Override
	         public void widgetSelected(SelectionEvent e) {
	    		 OpenURL.open(url, getShell());
	    	}
	    });
		
		
		Label lblEndianLabel = new Label(this, SWT.NONE);
		lblEndianLabel.setText(CpStringsUI.RteDeviceSelectorWidget_Endian);
		lblEndianLabel.setBounds(0, 0, 37, 13);
		
		lblEndian = new Label(this, SWT.NONE);
		lblEndian.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(this, SWT.NONE);
		
		Label lblBooks = new Label(this, SWT.NONE);
		lblBooks.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		lblBooks.setText(CpStringsUI.RteDeviceInfoWidget_lblBooks_text);
		new Label(this, SWT.NONE);
		
		lblDescription = new Label(this, SWT.NONE);
		lblDescription.setText(CpStringsUI.RteDeviceInfoWidget_lblDescription_text);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		tableViewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL);
		table = tableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));
	
		fBookColumnAdvisor = new RteBookColumnAdvisor(tableViewer);
		ColumnViewerToolTipSupport.enableFor(tableViewer);

		
		tableViewer.setContentProvider(new RteBookContentProvider());
	//	tableViewer.setLabelProvider(new RteBookLabelProvider());
		tableViewer.setLabelProvider(new AdvisedCellLabelProvider(fBookColumnAdvisor, 0));
		
		text = new Text(this, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridData gd_text = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 3);
		gd_text.widthHint = 240;
		gd_text.minimumWidth = 200;
		text.setLayoutData(gd_text);
		text.setEditable(false);
		
		lblBoards = new Label(this, SWT.NONE);
		lblBoards.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		lblBoards.setText(CpStringsUI.RteDeviceInfoWidget_lblBoards_text);
		new Label(this, SWT.NONE);
		
		treeViewer = new TreeViewer(this, SWT.BORDER| SWT.FULL_SELECTION);
		tree = treeViewer.getTree();
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		
		treeViewer.setContentProvider(new RteBoardContentProvider());
//		treeViewer.setLabelProvider(new RteBoardLabelProvider());

		fBoardColumnAdvisor = new RteBookColumnAdvisor(treeViewer);
		ColumnViewerToolTipSupport.enableFor(treeViewer);
		treeViewer.setLabelProvider(new AdvisedCellLabelProvider(fBoardColumnAdvisor, 0));
		
	}
	
	protected void updateControls() {
		String description = CmsisConstants.EMPTY_STRING;
		String vendorName = CmsisConstants.EMPTY_STRING;
		String deviceName = CmsisConstants.EMPTY_STRING;
		String cpu = CmsisConstants.EMPTY_STRING;
		String clock = CmsisConstants.EMPTY_STRING;
		String pack = CmsisConstants.EMPTY_STRING;
		String mem = CmsisConstants.EMPTY_STRING;
		String fpu = CmsisConstants.EMPTY_STRING;
		String endian = CmsisConstants.EMPTY_STRING;
		String family = CmsisConstants.EMPTY_STRING;
		String subFamily = CmsisConstants.EMPTY_STRING;
		String urlText = CmsisConstants.EMPTY_STRING;
		url = CmsisConstants.EMPTY_STRING;
		
		if(fDeviceInfo != null) {
			deviceName = fDeviceInfo.getDeviceName();
			
			cpu = "ARM " + fDeviceInfo.getAttribute(CmsisConstants.DCORE); //$NON-NLS-1$
			fpu = getFpuString(fDeviceInfo.getAttribute(CmsisConstants.DFPU));
			endian = fDeviceInfo.getAttribute(CmsisConstants.DENDIAN);
			vendorName = DeviceVendor.getOfficialVendorName(fDeviceInfo.getVendor());
			clock = fDeviceInfo.getClockSummary();
			pack = fDeviceInfo.getPackId();
			family = fDeviceInfo.getAttribute(CmsisConstants.DFAMILY);
			subFamily = fDeviceInfo.getAttribute(CmsisConstants.DSUBFAMILY);
			mem = fDeviceInfo.getMemorySummary();
			url = fDeviceInfo.getUrl();
			if(!url.isEmpty()) {
				urlText = "<a href=\"" + url + "\">" + url + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			if(fDeviceInfo.getDevice() != null) {
				description = fDeviceInfo.getDescription();
			} else {  
				description = CpStringsUI.RteDeviceInfoWidget_lblMissing_text;
				deviceName += " (" + CpStringsUI.RteDeviceInfoWidget_lblMissing_text + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		lblVendor.setText(vendorName);
		lblDevice.setText(deviceName);
		text.setText(description);
		lblCpu.setText(cpu);
		lblClock.setText(clock);
		lblPack.setText(pack);
		linkUrl.setText(urlText);
		linkUrl.setToolTipText(url);
		lblMemory.setText(mem);
		lblFamily.setText(family);
		lblSubfamily.setText(subFamily);
		
		
		lblFpu.setText(fpu);
		lblEndian.setText(endian);
		tableViewer.setInput(fDeviceInfo);
		
		Collection<ICpBoard> boards = null;
		ICpPackManager pm  = CpPlugIn.getPackManager();
		if(pm!= null) {
			boards = pm.getCompatibleBoards(fDeviceInfo.attributes());
		}
		treeViewer.setInput(boards);
		
	}

	private String getFpuString(String fpu){
		if(fpu != null) {
			switch(fpu){
			case "1":				 //$NON-NLS-1$
			case CmsisConstants.FPU:
			case CmsisConstants.SP_FPU:
				return CpStringsUI.RteDeviceSelectorWidget_SinglePrecision;
			case CmsisConstants.DP_FPU:
				return CpStringsUI.RteDeviceSelectorWidget_DoublePrecision;
			default:
				return CpStringsUI.RteDeviceSelectorWidget_none;
			}
		}
		return CmsisConstants.EMPTY_STRING;
	}

	/**
	 * Returns device info for selected device
	 * @return ICpDeviceInfo
	 */
	public ICpDeviceInfo getDeviceInfo() {
		return fDeviceInfo;
	}

	/**
	 * Sets device info for the widget
	 * @param deviceInfo ICpDeviceInfo
	 */
	public void setDeviceInfo(ICpDeviceInfo deviceInfo) {
		if(fDeviceInfo == deviceInfo)
			return;
		fDeviceInfo = deviceInfo;
		updateControls();
	}

	/**
	 * Adds selection adapter to browse for device 
	 * @param SelectionAdapter
	 */
	public void setSelectionAdapter(SelectionAdapter adapter) {
		btnSelect.addSelectionListener(adapter);
	}

	@Override
	public void dispose() {
		fDeviceInfo = null;
		super.dispose();
	}

	public void setModelController(IRteModelController modelController) {
		if(fBoardColumnAdvisor != null)
			fBoardColumnAdvisor.setModelController(modelController);
		if(fBookColumnAdvisor != null)
			fBookColumnAdvisor.setModelController(modelController);
		
	}

	
}
