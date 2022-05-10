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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class SecurityControlDeleteConfirmDialog extends Dialog {

    private List<com.contrastsecurity.admintool.model.Control> controls;
    private Table controlsTable;
    private List<Button> checkBoxList = new ArrayList<Button>();
    private List<Integer> selectedIdxes = new ArrayList<Integer>();

    public SecurityControlDeleteConfirmDialog(Shell parentShell, List<com.contrastsecurity.admintool.model.Control> controls) {
        super(parentShell);
        this.controls = controls;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));
        Label titleLbl = new Label(composite, SWT.LEFT);
        controlsTable = new Table(composite, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        controlsTable.setLayoutData(tableGrDt);
        controlsTable.setLinesVisible(true);
        controlsTable.setHeaderVisible(true);
        TableColumn column0 = new TableColumn(controlsTable, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(controlsTable, SWT.CENTER);
        column1.setWidth(50);
        column1.setText("削除");
        TableColumn column2 = new TableColumn(controlsTable, SWT.LEFT);
        column2.setWidth(50);
        column2.setText("ID");
        TableColumn column3 = new TableColumn(controlsTable, SWT.LEFT);
        column3.setWidth(120);
        column3.setText("名前");
        TableColumn column4 = new TableColumn(controlsTable, SWT.CENTER);
        column4.setWidth(100);
        column4.setText("言語");
        TableColumn column5 = new TableColumn(controlsTable, SWT.LEFT);
        column5.setWidth(250);
        column5.setText("API");
        TableColumn column6 = new TableColumn(controlsTable, SWT.LEFT);
        column6.setWidth(50);
        column6.setText("有効");

        for (com.contrastsecurity.admintool.model.Control control : controls) {
            addColToControlTable(control, -1);
        }
        if (selectedIdxes.isEmpty()) {
            titleLbl.setText("削除対象のセキュリティ制御はありません。");
        } else {
            titleLbl.setText("チェックされているセキュリティ制御が削除対象となります。");
        }
        Composite chkButtonGrp = new Composite(composite, SWT.NONE);
        chkButtonGrp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        chkButtonGrp.setLayout(new GridLayout(2, true));

        final Button allOnBtn = new Button(chkButtonGrp, SWT.NULL);
        allOnBtn.setLayoutData(new GridData());
        allOnBtn.setText("すべてオン");
        if (selectedIdxes.isEmpty()) {
            allOnBtn.setEnabled(false);
        }
        allOnBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (com.contrastsecurity.admintool.model.Control control : controls) {
                    if (control.isDeleteFlg()) {
                        selectedIdxes.add(controls.indexOf(control));
                    }
                }
                for (Button button : checkBoxList) {
                    button.setSelection(true);
                }
            }
        });

        final Button allOffBtn = new Button(chkButtonGrp, SWT.NULL);
        allOffBtn.setLayoutData(new GridData());
        allOffBtn.setText("すべてオフ");
        if (selectedIdxes.isEmpty()) {
            allOffBtn.setEnabled(false);
        }
        allOffBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (Button button : checkBoxList) {
                    button.setSelection(false);
                }
                selectedIdxes.clear();
            }
        });

        return composite;
    }

    private void addColToControlTable(com.contrastsecurity.admintool.model.Control control, int index) {
        if (control == null) {
            return;
        }
        TableItem item = null;
        if (index > 0) {
            item = new TableItem(controlsTable, SWT.CENTER, index);
        } else {
            item = new TableItem(controlsTable, SWT.CENTER);
        }
        TableEditor editor = new TableEditor(controlsTable);
        Button button = new Button(controlsTable, SWT.CHECK);
        if (control.isDeleteFlg()) {
            button.setEnabled(true);
            button.setSelection(true);
            checkBoxList.add(button);
            selectedIdxes.add(controls.indexOf(control));
        } else {
            button.setEnabled(false);
        }
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedIdxes.clear();
                for (Button button : checkBoxList) {
                    if (button.getSelection()) {
                        selectedIdxes.add(checkBoxList.indexOf(button));
                    }
                }
            }
        });
        button.pack();
        editor.minimumWidth = button.getSize().x;
        editor.horizontalAlignment = SWT.CENTER;
        editor.setEditor(button, item, 1);
        item.setText(2, String.valueOf(control.getId()));
        item.setText(3, control.getName());
        item.setText(4, control.getLanguage());
        item.setText(5, control.getApi());
        item.setText(6, String.valueOf(control.isEnabled()));
    }

    public List<Integer> getSelectedIdxes() {
        return selectedIdxes;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "削除実行", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        if (this.selectedIdxes.isEmpty()) {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        }
    }

    @Override
    protected Point getInitialSize() {
        return new Point(480, 360);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("セキュリティ制御(サニタイザ)の一括削除");
    }
}