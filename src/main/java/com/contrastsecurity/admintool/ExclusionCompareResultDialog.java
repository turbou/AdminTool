/*
 * MIT License
 * Copyright (c) 2015-2019 Tabocom
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.contrastsecurity.admintool;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ExclusionCompareResultDialog extends Dialog {

    private List<String> problemStrs;
    private Table failedControlsTable;

    public ExclusionCompareResultDialog(Shell parentShell, List<String> problemStrs) {
        super(parentShell);
        this.problemStrs = problemStrs;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));

        Label tableTitleLbl = new Label(composite, SWT.LEFT);
        tableTitleLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tableTitleLbl.setText("TeamServer上に存在しない例外の一覧です。");

        Label descLbl = new Label(composite, SWT.LEFT);
        descLbl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        descLbl.setText("※ 例外の名前が一致しないだけでなく、設定内容に差異がある場合も一覧に表示されます。");

        failedControlsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        failedControlsTable.setLayoutData(tableGrDt);
        failedControlsTable.setLinesVisible(true);
        failedControlsTable.setHeaderVisible(true);
        TableColumn column0 = new TableColumn(failedControlsTable, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(failedControlsTable, SWT.LEFT);
        column1.setWidth(640);
        column1.setText("存在しない例外");

        problemStrs.forEach(str -> addColToControlTable(str, -1));

        return composite;
    }

    private void addColToControlTable(String str, int index) {
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(failedControlsTable, SWT.CENTER, index);
        } else {
            item = new TableItem(failedControlsTable, SWT.CENTER);
        }
        item.setText(1, str);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CANCEL_ID, "閉じる", true);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(640, 480);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("例外のインポート済み確認結果");
    }
}
