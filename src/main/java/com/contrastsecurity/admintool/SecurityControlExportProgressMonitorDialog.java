package com.contrastsecurity.admintool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.model.Organization;

public class SecurityControlExportProgressMonitorDialog extends ProgressMonitorDialog {

    private Organization org;

    public SecurityControlExportProgressMonitorDialog(Shell parent, Organization org) {
        super(parent);
        this.org = org;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(String.format("セキュリティ制御のエクスポート - %s", this.org.getName()));
    }

}
