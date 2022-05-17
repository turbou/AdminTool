package com.contrastsecurity.admintool;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.model.Organization;

public class SecurityControlCompareProgressMonitorDialog extends ProgressMonitorDialog {

    private Organization org;

    public SecurityControlCompareProgressMonitorDialog(Shell parent, Organization org) {
        super(parent);
        this.org = org;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(String.format("セキュリティ制御のインポート済み確認 - %s", this.org.getName()));
    }

}
