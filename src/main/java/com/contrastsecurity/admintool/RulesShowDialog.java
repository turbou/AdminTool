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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class RulesShowDialog extends Dialog {

    private Table table;

    public RulesShowDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));
        table = new Table(composite, SWT.BORDER);
        GridData tableGrDt = new GridData(GridData.FILL_BOTH);
        table.setLayoutData(tableGrDt);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableColumn column0 = new TableColumn(table, SWT.NONE);
        column0.setWidth(0);
        column0.setResizable(false);
        TableColumn column1 = new TableColumn(table, SWT.LEFT);
        column1.setWidth(200);
        column1.setText("日本語名");
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setWidth(200);
        column2.setText("設定値");
        addColTable("DynamoDBのNoSQLインジェクション", "nosql-injection-dynamodb");
        addColTable("ELインジェクション", "expression-language-injection");
        addColTable("HQLインジェクション", "hql-injection");
        addColTable("LDAPインジェクション", "ldap-injection");
        addColTable("NoSQLインジェクション", "nosql-injection");
        addColTable("OSコマンドインジェクション", "cmd-injection");
        addColTable("SMTPインジェクション", "smtp-injection");
        addColTable("SQLインジェクション", "sql-injection");
        addColTable("XML外部実体参照(XXE)", "xxe");
        addColTable("XPathインジェクション", "xpath-injection");
        addColTable("クロスサイトスクリプティング", "reflected-xss");
        addColTable("サーバサイドリクエストフォージェリ(SSRF)", "ssrf");
        addColTable("パストラバーサル", "path-traversal");
        addColTable("リフレクションインジェクション", "reflection-injection");
        addColTable("任意に実行されるサーバサイドの転送", "unvalidated-forward");
        addColTable("信頼できないストリームでのreadLineの使用", "unsafe-readline");
        addColTable("信頼できないデータのデシリアライゼーション", "untrusted-deserialization");
        addColTable("信頼境界線違反", "trust-boundary-violation");
        addColTable("安全ではないXMLデコード", "unsafe-xml-decode");
        addColTable("未検証のリダイレクト", "unvalidated-redirect");
        addColTable("格納型クロスサイトスクリプティング", "stored-xss");
        return composite;
    }

    private void addColTable(String name, String value) {
        TableItem item = new TableItem(table, SWT.LEFT);
        item.setText(1, name);
        TableEditor editor2 = new TableEditor(table);
        Text text = new Text(table, SWT.NONE);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setText(value);
        text.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                text.selectAll();
            }
        });
        text.pack();
        editor2.grabHorizontal = true;
        editor2.horizontalAlignment = SWT.LEFT;
        editor2.setEditor(text, item, 2);

    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    @Override
    protected Point getInitialSize() {
        return new Point(480, 560);
    }

    @Override
    protected void setShellStyle(int newShellStyle) {
        super.setShellStyle(SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("ルール一覧");
    }
}
