package com.contrastsecurity.admintool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class SecurityControlDeleteProgressMonitorDialog extends ProgressMonitorDialog {

    public SecurityControlDeleteProgressMonitorDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("セキュリティ制御(サニタイザ)の一括削除");
    }

}
