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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.arm.cmsis.pack.common.CmsisConstants;
import com.arm.cmsis.pack.enums.EMemoryPrivilege;
import com.arm.cmsis.pack.enums.EMemorySecurity;
import com.arm.cmsis.pack.permissions.IMemoryAccess;
import com.arm.cmsis.pack.permissions.IMemoryPermissions;
import com.arm.cmsis.pack.permissions.IMemoryPriviledge;
import com.arm.cmsis.pack.permissions.IMemorySecurity;
import com.arm.cmsis.pack.permissions.MemoryPermissions;
import com.arm.cmsis.pack.ui.CpStringsUI;

public class MemoryPermissionsControl extends Composite {
    protected IMemoryPermissions fPermissions = new MemoryPermissions();
    protected IMemoryPermissions fParentPermissions = null;

    private ButtonGroup fAccessButtons = new ButtonGroup();
    private ButtonGroup fPrivilegeButtons = new ButtonGroup();
    private ButtonGroup fSecurityButtons = new ButtonGroup();
    private Button btnUnprivileged;
    private Button btnPrivileged;
    private Button btnPrivelegeUndefined;
    private Button btnCallable;
    private Button btnNonsecure;
    private Button btnSecure;
    private Button btnExecute;
    private Button btnWrite;
    private Button btnRead;
    private Button btnPeripheral;
    private Label lblS;
    private Label lblN;
    private Label lblC;
    private Group grpSecure;
    private Label lblSecureUndefined;
    private Button btnSecureUndefined;
    private Group grpPermissions;
    private Group grpPrivilege;

    boolean fbUpdating = false;
    boolean fbShowSecurity = true;

    SelectionAdapter selectionAdapter;

    class ButtonGroup {
        private Map<Character, Button> fButtons = new HashMap<>();

        public ButtonGroup() {

        }

        public Button getButton(char access) {
            return fButtons.get(access);
        }

        public void addButton(Button button, char access) {
            button.setData(Character.valueOf(access));
            fButtons.put(access, button);
            button.addSelectionListener(selectionAdapter);
        }

        public void setValues(String accessString) {
            boolean bValueSet = false;
            if (accessString == null)
                accessString = CmsisConstants.EMPTY_STRING;
            for (Entry<Character, Button> e : fButtons.entrySet()) {
                char ch = e.getKey();
                boolean bSet = accessString.indexOf(ch) >= 0;
                if (bSet)
                    bValueSet = true;

                Button btn = e.getValue();
                btn.setSelection(bSet);
            }
            if (!bValueSet) {
                // radio button case: set to undefined
                Button btn = getButton(' ');
                if (btn != null) {
                    btn.setSelection(true);
                }
            }
        }

        public void enableButtons(String mask) {
            for (Entry<Character, Button> e : fButtons.entrySet()) {
                char ch = e.getKey();
                boolean bEnable = mask == null || mask.indexOf(ch) >= 0;
                Button btn = e.getValue();
                btn.setEnabled(bEnable);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (Entry<Character, Button> e : fButtons.entrySet()) {
                Button btn = e.getValue();
                boolean bSet = btn.getSelection();
                char ch = e.getKey();
                if (bSet)
                    builder.append(ch);
            }
            return builder.toString();
        }
    }

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public MemoryPermissionsControl(Composite parent, int style) {
        super(parent, style);

        selectionAdapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                permissionsModified();
            }
        };
        setLayout(new FillLayout(SWT.HORIZONTAL));

        grpPermissions = new Group(this, SWT.NONE);
        grpPermissions.setText(CpStringsUI.MemoryPermissionsControl_Permissions);
        grpPermissions.setLayout(new GridLayout(1, false));

        Group grpAccess = new Group(grpPermissions, SWT.NONE);
        grpAccess.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpAccess.setText(CpStringsUI.MemoryPermissionsControl_Access);
        GridLayout gl_grpAccess = new GridLayout(2, false);
        gl_grpAccess.verticalSpacing = 4;
        gl_grpAccess.horizontalSpacing = 4;
        gl_grpAccess.marginHeight = 4;
        gl_grpAccess.marginWidth = 4;
        grpAccess.setLayout(gl_grpAccess);

        Label lblP = new Label(grpAccess, SWT.NONE);
        lblP.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblP.setText(EMemoryPrivilege.p);

        btnPeripheral = new Button(grpAccess, SWT.CHECK);
        btnPeripheral.setEnabled(false);
        btnPeripheral.addSelectionListener(selectionAdapter);
        btnPeripheral.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnPeripheral.setText(CpStringsUI.MemoryPermissionsControl_Peripheral);
        fAccessButtons.addButton(btnPeripheral, IMemoryAccess.PERIPHERAL_ACCESS);

        Label lblR = new Label(grpAccess, SWT.NONE);
        lblR.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblR.setText(IMemoryAccess.R);

        btnRead = new Button(grpAccess, SWT.CHECK);
        btnRead.setEnabled(false);
        btnRead.addSelectionListener(selectionAdapter);
        btnRead.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnRead.setText(CpStringsUI.MemoryPermissionsControl_Read);
        fAccessButtons.addButton(btnRead, IMemoryAccess.READ_ACCESS);

        Label lblW = new Label(grpAccess, SWT.NONE);
        lblW.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblW.setText(IMemoryAccess.W);

        btnWrite = new Button(grpAccess, SWT.CHECK);
        btnWrite.setEnabled(false);
        btnWrite.addSelectionListener(selectionAdapter);
        btnWrite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnWrite.setText(CpStringsUI.MemoryPermissionsControl_Write);
        fAccessButtons.addButton(btnWrite, IMemoryAccess.WRITE_ACCESS);

        Label lblX = new Label(grpAccess, SWT.NONE);
        lblX.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblX.setText(IMemoryAccess.X);

        btnExecute = new Button(grpAccess, SWT.CHECK);
        btnExecute.setEnabled(false);
        btnExecute.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnExecute.setText(CpStringsUI.MemoryPermissionsControl_Execute);
        fAccessButtons.addButton(btnExecute, IMemoryAccess.EXECUTE_ACCESS);

        grpPrivilege = new Group(grpPermissions, SWT.NONE);
        grpPrivilege.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        grpPrivilege.setText(CpStringsUI.MemoryPermissionsControl_Privilege);
        GridLayout gl_grpPrivilege = new GridLayout(2, false);
        gl_grpPrivilege.verticalSpacing = 4;
        gl_grpPrivilege.marginWidth = 4;
        gl_grpPrivilege.marginHeight = 4;
        gl_grpPrivilege.horizontalSpacing = 4;
        grpPrivilege.setLayout(gl_grpPrivilege);

        Label lblPrivilegeUndefined = new Label(grpPrivilege, SWT.NONE);
        lblPrivilegeUndefined.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblPrivilegeUndefined.setText(CmsisConstants.EMPTY_STRING);

        btnPrivelegeUndefined = new Button(grpPrivilege, SWT.RADIO);
        btnPrivelegeUndefined.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnPrivelegeUndefined.setText(CpStringsUI.MemoryPermissionsControl_NotSpecified);
        fPrivilegeButtons.addButton(btnPrivelegeUndefined, ' ');

        Label lblPriv = new Label(grpPrivilege, SWT.NONE);
        lblPriv.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblPriv.setText(EMemoryPrivilege.p);

        btnPrivileged = new Button(grpPrivilege, SWT.RADIO);
        btnPrivileged.addSelectionListener(selectionAdapter);
        btnPrivileged.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnPrivileged.setText(CpStringsUI.MemoryPermissionsControl_Privileged);
        fPrivilegeButtons.addButton(btnPrivileged, IMemoryPriviledge.PRIVILEGED_ACCESS);

        Label lblU = new Label(grpPrivilege, SWT.NONE);
        lblU.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblU.setText(EMemoryPrivilege.u);

        btnUnprivileged = new Button(grpPrivilege, SWT.RADIO);
        btnUnprivileged.addSelectionListener(selectionAdapter);
        btnUnprivileged.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnUnprivileged.setText(CpStringsUI.MemoryPermissionsControl_Unprivileged);
        fPrivilegeButtons.addButton(btnUnprivileged, IMemoryPriviledge.UNPRIVILEGED_ACCESS);

        grpSecure = new Group(grpPermissions, SWT.NONE);
        grpSecure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpSecure.setText(CpStringsUI.MemoryPermissionsControl_Security);
        GridLayout gl_grpSecure = new GridLayout(2, false);
        gl_grpSecure.verticalSpacing = 4;
        gl_grpSecure.marginWidth = 4;
        gl_grpSecure.marginHeight = 4;
        gl_grpSecure.horizontalSpacing = 4;
        grpSecure.setLayout(gl_grpSecure);

        lblSecureUndefined = new Label(grpSecure, SWT.NONE);
        lblSecureUndefined.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblSecureUndefined.setText(CmsisConstants.EMPTY_STRING);

        btnSecureUndefined = new Button(grpSecure, SWT.RADIO);
        btnSecureUndefined.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnSecureUndefined.setText(CpStringsUI.MemoryPermissionsControl_NotSpecified);
        fSecurityButtons.addButton(btnSecureUndefined, ' ');

        lblN = new Label(grpSecure, SWT.NONE);
        lblN.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblN.setText(CpStringsUI.MemoryPermissionsControl_CharNoSecure);

        btnNonsecure = new Button(grpSecure, SWT.RADIO);
        btnNonsecure.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnNonsecure.setText(CpStringsUI.MemoryPermissionsControl_NonSecure);
        fSecurityButtons.addButton(btnNonsecure, IMemorySecurity.NON_SECURE_ACCESS);

        lblC = new Label(grpSecure, SWT.NONE);
        lblC.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblC.setText(CpStringsUI.MemoryPermissionsControl_CharCallable);

        btnCallable = new Button(grpSecure, SWT.RADIO);
        btnCallable.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnCallable.setText(CpStringsUI.MemoryPermissionsControl_NonSecureCallable);
        fSecurityButtons.addButton(btnCallable, IMemorySecurity.CALLABLE_ACCESS);

        lblS = new Label(grpSecure, SWT.NONE);
        lblS.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        lblS.setText(CpStringsUI.MemoryPermissionsControl_CharSecure);

        btnSecure = new Button(grpSecure, SWT.RADIO);
        btnSecure.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnSecure.setText(CpStringsUI.MemoryPermissionsControl_Secure);
        fSecurityButtons.addButton(btnSecure, IMemorySecurity.SECURE_ACCESS);

        enableButtons();
    }

    protected void permissionsModified() {
        if (fbUpdating)
            return;
        fbUpdating = true;
        getPermissions(); // updates internal permissions
        enableButtons();
        emitModifyEvent(fPermissions);
        fbUpdating = false;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    @Override
    public void setEnabled(boolean enabled) {

        if (enabled) {
            enableButtons();
        } else {
            fAccessButtons.enableButtons(CmsisConstants.EMPTY_STRING);
            fPrivilegeButtons.enableButtons(CmsisConstants.EMPTY_STRING);
            if (fbShowSecurity)
                fSecurityButtons.enableButtons(CmsisConstants.EMPTY_STRING);
        }
        super.setEnabled(enabled);
    }

    public void hideSecurePermissions() {
        hideControl(grpSecure);
        layout();
    }

    public void setInitialValues(IMemoryPermissions permissions, IMemoryPermissions parentPermissions,
            boolean bShowSecurity) {
        fParentPermissions = parentPermissions;
        if (permissions != null)
            fPermissions.setPermissions(permissions);
        else
            fPermissions.setPermissions(parentPermissions);
        fbShowSecurity = bShowSecurity;
        if (!fbShowSecurity) {
            hideSecurePermissions();
        }
        setValues();
        enableButtons();
        layout();
    }

    private void hideControl(Control c) {
        c.setVisible(false);
        ((GridData) c.getLayoutData()).exclude = true;
    }

    protected void setValues() {
        fAccessButtons.setValues(fPermissions.getAccessString());
        fPrivilegeButtons.setValues(fPermissions.getPrivilegeString());
        if (fbShowSecurity)
            fSecurityButtons.setValues(fPermissions.getSecurityString());
        else {
            fSecurityButtons.setValues(null);
        }
    }

    protected void enableButtons() {
        String accessMask = null;
        String privilegeMask = null;
        String secureMask = null;

        if (fParentPermissions != null) {
            accessMask = fParentPermissions.getAccessMask();
            privilegeMask = fParentPermissions.getPrivilegeMask();
            secureMask = fParentPermissions.getSecurityMask();
        } else if (fPermissions.isPeripheralAccess()) {
            accessMask = IMemoryAccess.RW;
        }
        fAccessButtons.enableButtons(accessMask);
        fPrivilegeButtons.enableButtons(privilegeMask);
        if (fbShowSecurity) {
            fSecurityButtons.enableButtons(secureMask);
            if (fPermissions.isPeripheralAccess() || !fPermissions.isExecuteAccess()) {
                btnCallable.setEnabled(false);
                if (btnCallable.getSelection()) {
                    btnCallable.setSelection(false);
                    btnSecure.setSelection(true);
                }
            }
        }
    }

    protected void emitModifyEvent(IMemoryPermissions permissions) {
        Event e = new Event();
        e.data = permissions;
        e.display = getDisplay();
        e.widget = this;
        notifyListeners(SWT.Modify, e);
    }

    public IMemoryPermissions getPermissions() {
        // update from control
        fPermissions.setAccessString(fAccessButtons.toString().trim());
        fPermissions.setPrivilegeString(fPrivilegeButtons.toString().trim());
        if (fbShowSecurity) {
            String security = fSecurityButtons.toString().trim();
            if (fPermissions.isPeripheralAccess() || !fPermissions.isExecuteAccess())
                if (security.contains(EMemorySecurity.c)
                        && (fPermissions.isPeripheralAccess() || !fPermissions.isExecuteAccess())) {
                    security = EMemorySecurity.s;
                }
            fPermissions.setSecurityString(security);
        } else {
            fPermissions.setSecurityString(null);
        }
        return fPermissions;
    }

    public static String getDispalyString(EMemorySecurity security) {
        switch (security) {
        case CALLABLE:
            return CpStringsUI.MemoryPermissionsControl_NoSecureCallable;
        case NON_SECURE:
            return CpStringsUI.MemoryPermissionsControl_NonSecure;
        case SECURE:
            return CpStringsUI.MemoryPermissionsControl_Secure;
        case NOT_SPECIFIED:
        default:
            break;
        }
        return CpStringsUI.MemoryPermissionsControl_NotSpecified;
    }

    public static String getDispalyString(EMemoryPrivilege privilege) {
        switch (privilege) {
        case PRIVILEGED:
            return CpStringsUI.MemoryPermissionsControl_Privileged;
        case UNPRIVILEGED:
            return CpStringsUI.MemoryPermissionsControl_Unprivileged;
        case NOT_SPECIFIED:
        default:
            break;
        }
        return CpStringsUI.MemoryPermissionsControl_NotSpecified;
    }
}
