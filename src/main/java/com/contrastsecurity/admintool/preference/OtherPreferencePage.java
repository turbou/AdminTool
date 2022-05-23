/*
 * MIT License
 * Copyright (c) 2020 Contrast Security Japan G.K.
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
 * 
 */

package com.contrastsecurity.admintool.preference;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class OtherPreferencePage extends PreferencePage {

    private Text vulSleepTxt;
    private Text libSleepTxt;

    public OtherPreferencePage() {
        super("その他設定");
    }

    @Override
    protected Control createContents(Composite parent) {
        IPreferenceStore ps = getPreferenceStore();

        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout compositeLt = new GridLayout(1, false);
        compositeLt.marginHeight = 15;
        compositeLt.marginWidth = 5;
        compositeLt.horizontalSpacing = 10;
        compositeLt.verticalSpacing = 20;
        composite.setLayout(compositeLt);

        Group ctrlGrp = new Group(composite, SWT.NONE);
        GridLayout proxyGrpLt = new GridLayout(2, false);
        proxyGrpLt.marginWidth = 15;
        proxyGrpLt.horizontalSpacing = 10;
        ctrlGrp.setLayout(proxyGrpLt);
        GridData proxyGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // proxyGrpGrDt.horizontalSpan = 4;
        ctrlGrp.setLayoutData(proxyGrpGrDt);
        ctrlGrp.setText("スリープ制御");

        // ========== 脆弱性取得ごとスリープ ========== //
        new Label(ctrlGrp, SWT.LEFT).setText("脆弱性取得間隔スリープ（ミリ秒）：");
        vulSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        vulSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        vulSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_EX_DEL));

        // ========== ライブラリ取得ごとスリープ ========== //
        new Label(ctrlGrp, SWT.LEFT).setText("ライブラリ取得間隔スリープ（ミリ秒）：");
        libSleepTxt = new Text(ctrlGrp, SWT.BORDER);
        libSleepTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        libSleepTxt.setText(ps.getString(PreferenceConstants.SLEEP_LIB));

        Composite buttonGrp = new Composite(parent, SWT.NONE);
        GridLayout buttonGrpLt = new GridLayout(2, false);
        buttonGrpLt.marginHeight = 15;
        buttonGrpLt.marginWidth = 5;
        buttonGrpLt.horizontalSpacing = 7;
        buttonGrpLt.verticalSpacing = 20;
        buttonGrp.setLayout(buttonGrpLt);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        buttonGrpGrDt.horizontalAlignment = SWT.END;
        buttonGrp.setLayoutData(buttonGrpGrDt);

        Button defaultBtn = new Button(buttonGrp, SWT.NULL);
        GridData defaultBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        defaultBtnGrDt.widthHint = 90;
        defaultBtn.setLayoutData(defaultBtnGrDt);
        defaultBtn.setText("デフォルトに戻す");
        defaultBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                vulSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_EX_DEL));
                libSleepTxt.setText(ps.getDefaultString(PreferenceConstants.SLEEP_LIB));
            }
        });

        Button applyBtn = new Button(buttonGrp, SWT.NULL);
        GridData applyBtnGrDt = new GridData(SWT.RIGHT, SWT.BOTTOM, true, true, 1, 1);
        applyBtnGrDt.widthHint = 90;
        applyBtn.setLayoutData(applyBtnGrDt);
        applyBtn.setText("適用");
        applyBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                performOk();
            }
        });

        noDefaultAndApplyButton();
        return composite;
    }

    @Override
    public boolean performOk() {
        IPreferenceStore ps = getPreferenceStore();
        if (ps == null) {
            return true;
        }
        List<String> errors = new ArrayList<String>();
        if (this.vulSleepTxt.getText().isEmpty()) {
            errors.add("・脆弱性取得間隔スリープを指定してください。");
        } else {
            if (!StringUtils.isNumeric(this.vulSleepTxt.getText())) {
                errors.add("・脆弱性取得間隔スリープは数値を指定してください。");
            }
        }
        if (this.libSleepTxt.getText().isEmpty()) {
            errors.add("・ライブラリ取得間隔スリープを指定してください。");
        } else {
            if (!StringUtils.isNumeric(this.libSleepTxt.getText())) {
                errors.add("・ライブラリ取得間隔スリープは数値を指定してください。");
            }
        }
        ps.setValue(PreferenceConstants.SLEEP_EX_DEL, this.vulSleepTxt.getText());
        ps.setValue(PreferenceConstants.SLEEP_LIB, this.libSleepTxt.getText());
        if (!errors.isEmpty()) {
            MessageDialog.openError(getShell(), "その他設定", String.join("\r\n", errors));
            return false;
        }
        return true;
    }
}
