package com.contrastsecurity.admintool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

public class SecurityControlCompareProgressMonitorDialog extends ProgressMonitorDialog {

    public SecurityControlCompareProgressMonitorDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("セキュリティ制御の差分確認");
    }

}
