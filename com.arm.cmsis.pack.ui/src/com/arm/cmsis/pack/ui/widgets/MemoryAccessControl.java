package com.arm.cmsis.pack.ui.widgets;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.arm.cmsis.pack.build.IMemoryAccess;
import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.data.ICpMemory;

public class MemoryAccessControl extends Composite {
	private String fAccess = CmsisConstants.EMPTY_STRING;
	private String fAccessMask = CmsisConstants.EMPTY_STRING;

	private Map<Character, Button> fButtons = new HashMap<>(); 
	private Button btnUnprivileged;
	private Button btnCallable;
	private Button btnNonsecure;
	private Button btnSecure;
	private Button btnExecute;
	private Button btnWrite;
	private Button btnRead;
	private Button btnPeripheral;
	private boolean fbUpdating = false;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public MemoryAccessControl(Composite parent, int style) {
		super(parent, style);
		
		SelectionAdapter selectionAdapter =  new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button)e.getSource();
				boolean set = btn.getSelection();
				if(!set)
					return;
				char ch = (Character)btn.getData();
				updateButtons(ch);
			}
		};
		setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Group grpAccess = new Group(this, SWT.NONE);
		grpAccess.setText("Access permissions");
		grpAccess.setLayout(new GridLayout(2, false));
		
		Label lblP = new Label(grpAccess, SWT.NONE);
		lblP.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblP.setText("p");
		
		btnPeripheral = new Button(grpAccess, SWT.CHECK);
		btnPeripheral.setEnabled(false);
		btnPeripheral.addSelectionListener(selectionAdapter);
		btnPeripheral.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnPeripheral.setText("Peripheral");
		addButton(btnPeripheral, ICpMemory.PERIPHERAL_ACCESS);
		
		Label lblR = new Label(grpAccess, SWT.NONE);
		lblR.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblR.setText("r");
		
		btnRead = new Button(grpAccess, SWT.CHECK);
		btnRead.setEnabled(false);
		btnRead.addSelectionListener(selectionAdapter);
		btnRead.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnRead.setText("Read");
		addButton(btnRead, ICpMemory.READ_ACCESS);
		
		Label lblW = new Label(grpAccess, SWT.NONE);
		lblW.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblW.setText("w");
		
		btnWrite = new Button(grpAccess, SWT.CHECK);
		btnWrite.setEnabled(false);
		btnWrite.addSelectionListener(selectionAdapter);
		btnWrite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnWrite.setText("Write");
		addButton(btnWrite, ICpMemory.WRITE_ACCESS);
		
		Label lblX = new Label(grpAccess, SWT.NONE);
		lblX.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblX.setText("x");
		
		btnExecute = new Button(grpAccess, SWT.CHECK);
		btnExecute.setEnabled(false);
		btnExecute.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnExecute.setText("Execute");
		addButton(btnExecute, ICpMemory.EXECUTE_ACCESS);
		
		Label lblS = new Label(grpAccess, SWT.NONE);
		lblS.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblS.setText("s");
		
		btnSecure = new Button(grpAccess, SWT.CHECK);
		btnSecure.setEnabled(false);
		btnSecure.addSelectionListener(selectionAdapter);
		btnSecure.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnSecure.setText("Secure");
		addButton(btnSecure, ICpMemory.SECURE_ACCESS);
		
		Label lblN = new Label(grpAccess, SWT.NONE);
		lblN.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblN.setText("n");
		
		btnNonsecure = new Button(grpAccess, SWT.CHECK);
		btnNonsecure.setEnabled(false);
		btnNonsecure.addSelectionListener(selectionAdapter);
		btnNonsecure.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnNonsecure.setText("Non-secure");
		addButton(btnNonsecure, ICpMemory.NON_SECURE_ACCESS);
		
		Label lblC = new Label(grpAccess, SWT.NONE);
		lblC.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblC.setText("c");
		
		btnCallable = new Button(grpAccess, SWT.CHECK);
		btnCallable.setEnabled(false);
		btnCallable.addSelectionListener(selectionAdapter);
		btnCallable.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnCallable.setText("Callable");
		addButton(btnCallable, ICpMemory.CALLABLE_ACCESS);
		
		Label lblU = new Label(grpAccess, SWT.NONE);
		lblU.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblU.setText("u");
		
		btnUnprivileged = new Button(grpAccess, SWT.CHECK);
		btnUnprivileged.setEnabled(false);
		btnUnprivileged.addSelectionListener(selectionAdapter);
		btnUnprivileged.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		btnUnprivileged.setText("Unprivileged");
		addButton(btnUnprivileged, 'u');
		enableButtons();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	
	private void addButton(Button button, char access) {
		button.setData(new Character(access));
		fButtons.put(access, button);
	}
	
	private Button getButton(char access) {
		return fButtons.get(access);
	}
	

	public void setAccessMask(String access) {
		fAccessMask = access;
		enableButtons();
	}
	
	public void setAccess(String access) {
		fAccess = access;
		IMemoryAccess memAccess = IMemoryAccess.fromString(access); 
		for( Entry<Character, Button> e :  fButtons.entrySet()) {
			char ch = e.getKey();
			boolean bSet = memAccess.isAccessSet(ch);
			Button btn = e.getValue();
			btn.setSelection(bSet);
		}
		if(memAccess.isNonSecureAccess()) {
			getButton(IMemoryAccess.NON_SECURE_ACCESS).setSelection(true);
		}
	}

	public void enableAllButtons(boolean bEnable) {
		for(Button btn : fButtons.values()) {
			btn.setEnabled(bEnable);
		}
	}
	
	private void enableButtons() {
		if(fAccessMask == null) {
			enableAllButtons(true);
			return;
		}
		enableAllButtons(false);
		int i;
		for(i = 0; i < fAccessMask.length(); i++) {
			char ch = fAccessMask.charAt(i);
			Button btn = getButton(ch);
			if(btn == null)
				continue;
			btn.setEnabled(true);
		}
	}

	
	void updateButtons(char ch) {
		if(fbUpdating)
			return; 
		fbUpdating = true;
		if(ch == IMemoryAccess.NON_SECURE_ACCESS) {
			getButton(IMemoryAccess.SECURE_ACCESS).setSelection(false);
			getButton(IMemoryAccess.CALLABLE_ACCESS).setSelection(false);
		} else if(ch == IMemoryAccess.SECURE_ACCESS) {
			getButton(IMemoryAccess.NON_SECURE_ACCESS).setSelection(false);
			getButton(IMemoryAccess.CALLABLE_ACCESS).setSelection(false);
		} else if(ch == IMemoryAccess.CALLABLE_ACCESS){
			getButton(IMemoryAccess.NON_SECURE_ACCESS).setSelection(false);
			getButton(IMemoryAccess.SECURE_ACCESS).setSelection(false);
		}
		fbUpdating = false;
	}
	
	public String getAccess() {
		StringBuilder builder = new StringBuilder();
		for( Entry<Character, Button> e :  fButtons.entrySet()) {
			Button btn = e.getValue();
			boolean bSet = btn.getSelection();
			char ch = e.getKey();
			if(bSet)
				builder.append(ch);
		}
		return IMemoryAccess.normalize(builder.toString());
	}
	
}
