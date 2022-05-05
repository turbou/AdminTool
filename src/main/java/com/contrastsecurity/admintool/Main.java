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

package com.contrastsecurity.admintool;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.OS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.yaml.snakeyaml.Yaml;

import com.contrastsecurity.admintool.exception.ApiException;
import com.contrastsecurity.admintool.exception.NonApiException;
import com.contrastsecurity.admintool.exception.TsvException;
import com.contrastsecurity.admintool.model.ContrastSecurityYaml;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.preference.AboutPage;
import com.contrastsecurity.admintool.preference.BasePreferencePage;
import com.contrastsecurity.admintool.preference.CSVPreferencePage;
import com.contrastsecurity.admintool.preference.ConnectionPreferencePage;
import com.contrastsecurity.admintool.preference.LibCSVColumnPreferencePage;
import com.contrastsecurity.admintool.preference.MyPreferenceDialog;
import com.contrastsecurity.admintool.preference.OtherPreferencePage;
import com.contrastsecurity.admintool.preference.PreferenceConstants;
import com.contrastsecurity.admintool.preference.VulCSVColumnPreferencePage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class Main implements PropertyChangeListener {

    public static final String WINDOW_TITLE = "AdminTool - %s";
    // 以下のMASTER_PASSWORDはプロキシパスワードを保存する際に暗号化で使用するパスワードです。
    // 本ツールをリリース用にコンパイルする際はchangemeを別の文字列に置き換えてください。
    public static final String MASTER_PASSWORD = "changeme!";

    // 各出力ファイルの文字コード
    public static final String CSV_WIN_ENCODING = "Shift_JIS";
    public static final String CSV_MAC_ENCODING = "UTF-8";
    public static final String FILE_ENCODING = "UTF-8";

    public static final int MINIMUM_SIZE_WIDTH = 480;
    public static final int MINIMUM_SIZE_HEIGHT = 400;

    private AdminToolShell shell;

    // ASSESS
    private CTabFolder mainTabFolder;
    private CTabFolder subTabFolder;

    private Button sanitizerExportBtn;
    private Button sanitizerDeleteBtn;
    private Text sanitizerDelIncludeTxt;
    private Text sanitizerDelExcludeTxt;
    private Button sanitizerImportBtn;
    private Button sanitizerCompareBtn;
    private Button sanitizerSkeletonBtn;

    private Button libExecuteBtn;
    private Button onlyHasCVEChk;
    private Button includeCVEDetailChk;

    private Button settingBtn;

    private PreferenceStore ps;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    Logger logger = LogManager.getLogger("csvdltool");

    String currentTitle;

    /**
     * @param args
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.initialize();
        main.createPart();
    }

    private void initialize() {
        try {
            String homeDir = System.getProperty("user.home");
            this.ps = new PreferenceStore(homeDir + "\\admintool.properties");
            if (OS.isFamilyMac()) {
                this.ps = new PreferenceStore(homeDir + "/admintool.properties");
            }
            try {
                this.ps.load();
            } catch (FileNotFoundException fnfe) {
                this.ps = new PreferenceStore("admintool.properties");
                this.ps.load();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.ps.setDefault(PreferenceConstants.TSV_STATUS, TsvStatusEnum.NONE.name());
            this.ps.setDefault(PreferenceConstants.PROXY_AUTH, "none");
            this.ps.setDefault(PreferenceConstants.CONNECTION_TIMEOUT, 3000);
            this.ps.setDefault(PreferenceConstants.SOCKET_TIMEOUT, 3000);

            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_VUL, VulCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.SLEEP_VUL, 300);
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_VUL, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_VUL, "'vul'_yyyy-MM-dd_HHmmss");

            this.ps.setDefault(PreferenceConstants.CSV_COLUMN_LIB, LibCSVColmunEnum.defaultValuesStr());
            this.ps.setDefault(PreferenceConstants.SLEEP_LIB, 300);
            this.ps.setDefault(PreferenceConstants.CSV_OUT_HEADER_LIB, true);
            this.ps.setDefault(PreferenceConstants.CSV_FILE_FORMAT_LIB, "'lib'_yyyy-MM-dd_HHmmss");

            this.ps.setDefault(PreferenceConstants.OPENED_MAIN_TAB_IDX, 0);
            this.ps.setDefault(PreferenceConstants.OPENED_SUB_TAB_IDX, 0);

            Yaml yaml = new Yaml();
            InputStream is = new FileInputStream("contrast_security.yaml");
            ContrastSecurityYaml contrastSecurityYaml = yaml.loadAs(is, ContrastSecurityYaml.class);
            is.close();
            this.ps.setDefault(PreferenceConstants.CONTRAST_URL, contrastSecurityYaml.getUrl());
            this.ps.setDefault(PreferenceConstants.SERVICE_KEY, contrastSecurityYaml.getServiceKey());
            this.ps.setDefault(PreferenceConstants.USERNAME, contrastSecurityYaml.getUserName());
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    private void createPart() {
        Display display = new Display();
        shell = new AdminToolShell(display, this);
        shell.setMinimumSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
        Image[] imageArray = new Image[5];
        imageArray[0] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon16.png"));
        imageArray[1] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon24.png"));
        imageArray[2] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon32.png"));
        imageArray[3] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon48.png"));
        imageArray[4] = new Image(display, Main.class.getClassLoader().getResourceAsStream("icon128.png"));
        shell.setImages(imageArray);
        Window.setDefaultImages(imageArray);
        setWindowTitle();
        shell.addShellListener(new ShellListener() {
            @Override
            public void shellIconified(ShellEvent event) {
            }

            @Override
            public void shellDeiconified(ShellEvent event) {
            }

            @Override
            public void shellDeactivated(ShellEvent event) {
            }

            @Override
            public void shellClosed(ShellEvent event) {
                int main_idx = mainTabFolder.getSelectionIndex();
                int sub_idx = subTabFolder.getSelectionIndex();
                ps.setValue(PreferenceConstants.OPENED_MAIN_TAB_IDX, main_idx);
                ps.setValue(PreferenceConstants.OPENED_SUB_TAB_IDX, sub_idx);
                ps.setValue(PreferenceConstants.MEM_WIDTH, shell.getSize().x);
                ps.setValue(PreferenceConstants.MEM_HEIGHT, shell.getSize().y);
                ps.setValue(PreferenceConstants.ONLY_HAS_CVE, onlyHasCVEChk.getSelection());
                ps.setValue(PreferenceConstants.INCLUDE_CVE_DETAIL, includeCVEDetailChk.getSelection());
                ps.setValue(PreferenceConstants.PROXY_TMP_USER, "");
                ps.setValue(PreferenceConstants.PROXY_TMP_PASS, "");
                ps.setValue(PreferenceConstants.TSV_STATUS, "");
                try {
                    ps.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            @Override
            public void shellActivated(ShellEvent event) {
                List<Organization> orgs = getValidOrganizations();
                if (orgs.isEmpty()) {
                    settingBtn.setText("このボタンから基本設定を行ってください。");
                    currentTitle = "";
                    uiReset();
                } else {
                    settingBtn.setText("設定");
                    List<String> orgNameList = new ArrayList<String>();
                    String title = String.join(", ", orgNameList);
                    if (currentTitle != null && !currentTitle.equals(title)) {
                        uiReset();
                        currentTitle = title;
                    }
                }
                setWindowTitle();
                if (ps.getBoolean(PreferenceConstants.PROXY_YUKO) && ps.getString(PreferenceConstants.PROXY_AUTH).equals("input")) {
                    String usr = ps.getString(PreferenceConstants.PROXY_TMP_USER);
                    String pwd = ps.getString(PreferenceConstants.PROXY_TMP_PASS);
                    if (usr == null || usr.isEmpty() || pwd == null || pwd.isEmpty()) {
                        ProxyAuthDialog proxyAuthDialog = new ProxyAuthDialog(shell);
                        int result = proxyAuthDialog.open();
                        if (IDialogConstants.CANCEL_ID == result) {
                            ps.setValue(PreferenceConstants.PROXY_AUTH, "none");
                        } else {
                            ps.setValue(PreferenceConstants.PROXY_TMP_USER, proxyAuthDialog.getUsername());
                            ps.setValue(PreferenceConstants.PROXY_TMP_PASS, proxyAuthDialog.getPassword());
                        }
                    }
                }
            }
        });

        Listener listener = new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.stateMask == SWT.CTRL) {
                    int num = Character.getNumericValue(event.character);
                    if (num > -1) {
                        support.firePropertyChange("userswitch", 0, num);
                    }
                }
            }
        };
        display.addFilter(SWT.KeyUp, listener);

        GridLayout baseLayout = new GridLayout(1, false);
        baseLayout.marginWidth = 8;
        baseLayout.marginBottom = 8;
        baseLayout.verticalSpacing = 8;
        shell.setLayout(baseLayout);

        mainTabFolder = new CTabFolder(shell, SWT.NONE);
        GridData mainTabFolderGrDt = new GridData(GridData.FILL_BOTH);
        mainTabFolder.setLayoutData(mainTabFolderGrDt);
        mainTabFolder.setSelectionBackground(new Color[] { display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
                new int[] { 100 }, true);
        mainTabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });

        // #################### セキュリティ制御 #################### //
        CTabItem assessTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        assessTabItem.setText("セキュリティ制御");

        Composite assessShell = new Composite(mainTabFolder, SWT.NONE);
        assessShell.setLayout(new GridLayout(1, false));

        subTabFolder = new CTabFolder(assessShell, SWT.NONE);
        GridData tabFolderGrDt = new GridData(GridData.FILL_HORIZONTAL);
        subTabFolder.setLayoutData(tabFolderGrDt);
        subTabFolder.setSelectionBackground(new Color[] { display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND), display.getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
                new int[] { 100 }, true);

        // #################### Sanitizer #################### //
        CTabItem vulTabItem = new CTabItem(subTabFolder, SWT.NONE);
        vulTabItem.setText("Sanitizer");

        // ========== グループ ==========
        Composite vulButtonGrp = new Composite(subTabFolder, SWT.NULL);
        GridLayout buttonGrpLt = new GridLayout(1, false);
        buttonGrpLt.marginWidth = 10;
        buttonGrpLt.marginHeight = 10;
        vulButtonGrp.setLayout(buttonGrpLt);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // buttonGrpGrDt.horizontalSpan = 3;
        // buttonGrpGrDt.widthHint = 100;
        vulButtonGrp.setLayoutData(buttonGrpGrDt);

        // ========== エクスポートボタン ==========
        sanitizerExportBtn = new Button(vulButtonGrp, SWT.PUSH);
        GridData sanitizerExportBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        sanitizerExportBtnGrDt.heightHint = 30;
        sanitizerExportBtnGrDt.horizontalSpan = 2;
        sanitizerExportBtn.setLayoutData(sanitizerExportBtnGrDt);
        sanitizerExportBtn.setText("エクスポート");
        sanitizerExportBtn.setToolTipText("セキュリティ制御(サニタイザ)のエクスポート");
        sanitizerExportBtn.setFont(new Font(display, "ＭＳ ゴシック", 13, SWT.NORMAL));
        sanitizerExportBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText("出力先フォルダを指定してください。");
                String dir = dialog.open();
                SanitizerExportWithProgress progress = new SanitizerExportWithProgress(shell, ps, getValidOrganizations(), dir);
                ProgressMonitorDialog progDialog = new SanitizerExportProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "セキュリティ制御(サニタイザ)のエクスポート", String.format("TeamServerからエラーが返されました。\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "セキュリティ制御(サニタイザ)のエクスポート", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "セキュリティ制御(サニタイザ)のエクスポート", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "セキュリティ制御(サニタイザ)のエクスポート", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // ========== 一括削除ボタン ==========
        Composite deleteGrp = new Composite(vulButtonGrp, SWT.NULL);
        GridLayout deleteGrpLt = new GridLayout(2, false);
        deleteGrpLt.marginWidth = 0;
        deleteGrpLt.marginHeight = 0;
        deleteGrp.setLayout(deleteGrpLt);
        GridData deleteGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        deleteGrpGrDt.heightHint = 70;
        deleteGrp.setLayoutData(deleteGrpGrDt);

        sanitizerDeleteBtn = new Button(deleteGrp, SWT.PUSH);
        GridData sanitizerDeleteBtnGrDt = new GridData(GridData.FILL_BOTH);
        sanitizerDeleteBtn.setLayoutData(sanitizerDeleteBtnGrDt);
        sanitizerDeleteBtn.setText("一括削除");
        sanitizerDeleteBtn.setToolTipText("セキュリティ制御(サニタイザ)の全削除");
        sanitizerDeleteBtn.setFont(new Font(display, "ＭＳ ゴシック", 11, SWT.NORMAL));
        sanitizerDeleteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String includeName = sanitizerDelIncludeTxt.getText().trim();
                String excludeName = sanitizerDelExcludeTxt.getText().trim();
                if (!includeName.isEmpty() && !excludeName.isEmpty()) {
                    MessageDialog.openError(shell, "セキュリティ制御(サニタイザ)の一括削除", "削除対象と残す対象の両方を指定することはできません。");
                    return;
                }
                SanitizerDeleteWithProgress progress = new SanitizerDeleteWithProgress(shell, ps, getValidOrganizations(), includeName, excludeName);
                ProgressMonitorDialog progDialog = new SanitizerDeleteProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    String trace = stringWriter.toString();
                    if (!(e.getTargetException() instanceof TsvException)) {
                        logger.error(trace);
                    }
                    String errorMsg = e.getTargetException().getMessage();
                    if (e.getTargetException() instanceof ApiException) {
                        MessageDialog.openWarning(shell, "セキュリティ制御(サニタイザ)のエクスポート", String.format("TeamServerからエラーが返されました。\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "セキュリティ制御(サニタイザ)のエクスポート", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "セキュリティ制御(サニタイザ)のエクスポート", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "セキュリティ制御(サニタイザ)のエクスポート", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Composite deleteCtrlGrp = new Composite(deleteGrp, SWT.NULL);
        GridLayout deleteCtrlGrpLt = new GridLayout(1, false);
        deleteCtrlGrpLt.marginWidth = 0;
        deleteCtrlGrpLt.marginHeight = 1;
        deleteCtrlGrp.setLayout(deleteCtrlGrpLt);
        GridData deleteControlGrpGrDt = new GridData(GridData.FILL_BOTH);
        deleteControlGrpGrDt.heightHint = 70;
        deleteCtrlGrp.setLayoutData(deleteControlGrpGrDt);

        sanitizerDelIncludeTxt = new Text(deleteCtrlGrp, SWT.BORDER);
        sanitizerDelIncludeTxt.setMessage("対象の名前を指定してください。(任意)");
        sanitizerDelIncludeTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sanitizerDelIncludeTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                sanitizerDelIncludeTxt.selectAll();
            }
        });

        Composite inExcludeGrp = new Composite(deleteCtrlGrp, SWT.NULL);
        GridLayout inExcludeGrpLt = new GridLayout(3, false);
        inExcludeGrpLt.marginWidth = 0;
        inExcludeGrpLt.marginHeight = 1;
        inExcludeGrp.setLayout(inExcludeGrpLt);
        GridData inExcludeGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        inExcludeGrp.setLayoutData(inExcludeGrpGrDt);

        Label cludeLbl = new Label(inExcludeGrp, SWT.LEFT);
        cludeLbl.setText("この名前を:");
        GridData cludeLblGrDt = new GridData();
        cludeLblGrDt.widthHint = 60;
        cludeLbl.setLayoutData(cludeLblGrDt);

        Button include = new Button(inExcludeGrp, SWT.RADIO);
        include.setText("削除");
        Button exclude = new Button(inExcludeGrp, SWT.RADIO);
        exclude.setText("残す");

        Composite compareGrp = new Composite(deleteCtrlGrp, SWT.NULL);
        GridLayout compareGrpLt = new GridLayout(4, false);
        compareGrpLt.marginWidth = 0;
        compareGrpLt.marginHeight = 1;
        compareGrp.setLayout(compareGrpLt);
        GridData compareGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        compareGrp.setLayoutData(compareGrpGrDt);

        Label compareLbl = new Label(compareGrp, SWT.LEFT);
        compareLbl.setText("比較方法:");
        GridData compareLblGrDt = new GridData();
        compareLblGrDt.widthHint = 60;
        compareLbl.setLayoutData(compareLblGrDt);

        Button startsWith = new Button(compareGrp, SWT.RADIO);
        startsWith.setText("前方一致");
        Button endsWith = new Button(compareGrp, SWT.RADIO);
        endsWith.setText("後方一致");
        Button contains = new Button(compareGrp, SWT.RADIO);
        contains.setText("部分一致");

        // ========== インポートボタン ==========
        sanitizerImportBtn = new Button(vulButtonGrp, SWT.PUSH);
        GridData sanitizerImportBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        sanitizerImportBtnGrDt.heightHint = 50;
        sanitizerImportBtnGrDt.horizontalSpan = 2;
        sanitizerImportBtn.setLayoutData(sanitizerImportBtnGrDt);
        sanitizerImportBtn.setText("インポート");
        sanitizerImportBtn.setToolTipText("セキュリティ制御(サニタイザ)のインポート");
        sanitizerImportBtn.setFont(new Font(display, "ＭＳ ゴシック", 18, SWT.NORMAL));
        sanitizerImportBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dialog = new FileDialog(shell);
                dialog.setText("インポートするjsonファイルを指定してください。");
                dialog.setFilterExtensions(new String[] { "*.json" });
                String file = dialog.open();
                System.out.println(file);
            }
        });

        // ========== 差分確認ボタン ==========
        sanitizerCompareBtn = new Button(vulButtonGrp, SWT.PUSH);
        GridData sanitizerCompareBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        sanitizerCompareBtnGrDt.heightHint = 30;
        sanitizerCompareBtnGrDt.horizontalSpan = 2;
        sanitizerCompareBtn.setLayoutData(sanitizerCompareBtnGrDt);
        sanitizerCompareBtn.setText("差分確認");
        sanitizerCompareBtn.setToolTipText("セキュリティ制御(サニタイザ)の差分確認");
        sanitizerCompareBtn.setFont(new Font(display, "ＭＳ ゴシック", 13, SWT.NORMAL));
        sanitizerCompareBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dialog = new FileDialog(shell);
                dialog.setText("比較する対象のjsonファイルを指定してください。");
                dialog.setFilterExtensions(new String[] { "*.json" });
                String file = dialog.open();
                System.out.println(file);
            }
        });

        // ========== スケルトン生成ボタン ==========
        sanitizerSkeletonBtn = new Button(vulButtonGrp, SWT.PUSH);
        GridData executeBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        executeBtnGrDt.horizontalSpan = 2;
        sanitizerSkeletonBtn.setLayoutData(executeBtnGrDt);
        sanitizerSkeletonBtn.setText("スケルトンJSON出力");
        sanitizerSkeletonBtn.setToolTipText("セキュリティ制御(サニタイザ)のインポートJSONファイルのスケルトン生成");
        sanitizerSkeletonBtn.setFont(new Font(display, "ＭＳ ゴシック", 11, SWT.NORMAL));
        sanitizerSkeletonBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText("出力先フォルダを指定してください。");
                String dir = dialog.open();
                System.out.println(dir);
            }
        });

        vulTabItem.setControl(vulButtonGrp);

        // #################### ライブラリ #################### //
        CTabItem libTabItem = new CTabItem(subTabFolder, SWT.NONE);
        libTabItem.setText("Input Validator");

        // ========== グループ ==========
        Composite libButtonGrp = new Composite(subTabFolder, SWT.NULL);
        GridLayout libButtonGrpLt = new GridLayout(1, false);
        libButtonGrpLt.marginWidth = 10;
        libButtonGrpLt.marginHeight = 10;
        libButtonGrp.setLayout(libButtonGrpLt);
        GridData libButtonGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // libButtonGrpGrDt.horizontalSpan = 3;
        // libButtonGrpGrDt.widthHint = 100;
        libButtonGrp.setLayoutData(libButtonGrpGrDt);

        // ========== 取得ボタン ==========
        libExecuteBtn = new Button(libButtonGrp, SWT.PUSH);
        GridData libExecuteBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        libExecuteBtnGrDt.heightHint = 50;
        libExecuteBtn.setLayoutData(libExecuteBtnGrDt);
        libExecuteBtn.setText("取得");
        libExecuteBtn.setToolTipText("ライブラリ情報を取得し、CSV形式で出力します。");
        libExecuteBtn.setFont(new Font(display, "ＭＳ ゴシック", 20, SWT.NORMAL));
        libExecuteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
            }
        });

        onlyHasCVEChk = new Button(libButtonGrp, SWT.CHECK);
        onlyHasCVEChk.setText("CVE（脆弱性）を含むライブラリのみ出力する。");
        if (this.ps.getBoolean(PreferenceConstants.ONLY_HAS_CVE)) {
            onlyHasCVEChk.setSelection(true);
        }
        includeCVEDetailChk = new Button(libButtonGrp, SWT.CHECK);
        includeCVEDetailChk.setText("CVEの詳細情報も出力する。（フォルダ出力）");
        includeCVEDetailChk.setToolTipText("CVEの詳細情報が添付ファイルで出力されます。");
        if (this.ps.getBoolean(PreferenceConstants.INCLUDE_CVE_DETAIL)) {
            includeCVEDetailChk.setSelection(true);
        }
        libTabItem.setControl(libButtonGrp);

        int sub_idx = this.ps.getInt(PreferenceConstants.OPENED_SUB_TAB_IDX);
        subTabFolder.setSelection(sub_idx);

        assessTabItem.setControl(assessShell);

        int main_idx = this.ps.getInt(PreferenceConstants.OPENED_MAIN_TAB_IDX);
        mainTabFolder.setSelection(main_idx);

        // ========== 設定ボタン ==========
        settingBtn = new Button(shell, SWT.PUSH);
        settingBtn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        settingBtn.setText("設定");
        settingBtn.setToolTipText("動作に必要な設定を行います。");
        settingBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                PreferenceManager mgr = new PreferenceManager();
                PreferenceNode baseNode = new PreferenceNode("base", new BasePreferencePage());
                PreferenceNode connectionNode = new PreferenceNode("connection", new ConnectionPreferencePage());
                PreferenceNode otherNode = new PreferenceNode("other", new OtherPreferencePage());
                PreferenceNode csvNode = new PreferenceNode("csv", new CSVPreferencePage());
                PreferenceNode vulCsvColumnNode = new PreferenceNode("vulcsvcolumn", new VulCSVColumnPreferencePage());
                PreferenceNode libCsvColumnNode = new PreferenceNode("libcsvcolumn", new LibCSVColumnPreferencePage());
                mgr.addToRoot(baseNode);
                mgr.addToRoot(connectionNode);
                mgr.addToRoot(otherNode);
                mgr.addToRoot(csvNode);
                mgr.addTo(csvNode.getId(), vulCsvColumnNode);
                mgr.addTo(csvNode.getId(), libCsvColumnNode);
                PreferenceNode aboutNode = new PreferenceNode("about", new AboutPage());
                mgr.addToRoot(aboutNode);
                PreferenceDialog dialog = new MyPreferenceDialog(shell, mgr);
                dialog.setPreferenceStore(ps);
                dialog.open();
                try {
                    ps.save();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        uiUpdate();
        int width = this.ps.getInt(PreferenceConstants.MEM_WIDTH);
        int height = this.ps.getInt(PreferenceConstants.MEM_HEIGHT);
        if (width > 0 && height > 0) {
            shell.setSize(width, height);
        } else {
            shell.setSize(MINIMUM_SIZE_WIDTH, MINIMUM_SIZE_HEIGHT);
            // shell.pack();
        }
        shell.open();
        try {
            while (!shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } catch (Exception e) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            String trace = stringWriter.toString();
            logger.error(trace);
        }
        display.dispose();
    }

    private void uiReset() {
    }

    private void uiUpdate() {
    }

    public PreferenceStore getPreferenceStore() {
        return ps;
    }

    public Organization getValidOrganization() {
        String orgJsonStr = ps.getString(PreferenceConstants.TARGET_ORGS);
        if (orgJsonStr.trim().length() > 0) {
            try {
                List<Organization> orgList = new Gson().fromJson(orgJsonStr, new TypeToken<List<Organization>>() {
                }.getType());
                for (Organization org : orgList) {
                    if (org != null && org.isValid()) {
                        return org;
                    }
                }
            } catch (JsonSyntaxException e) {
                return null;
            }
        }
        return null;
    }

    public List<Organization> getValidOrganizations() {
        List<Organization> orgs = new ArrayList<Organization>();
        String orgJsonStr = ps.getString(PreferenceConstants.TARGET_ORGS);
        if (orgJsonStr.trim().length() > 0) {
            try {
                List<Organization> orgList = new Gson().fromJson(orgJsonStr, new TypeToken<List<Organization>>() {
                }.getType());
                for (Organization org : orgList) {
                    if (org != null && org.isValid()) {
                        orgs.add(org);
                    }
                }
            } catch (JsonSyntaxException e) {
                return orgs;
            }
        }
        return orgs;
    }

    public void setWindowTitle() {
        String text = null;
        List<Organization> validOrgs = getValidOrganizations();
        if (!validOrgs.isEmpty()) {
            List<String> orgNameList = new ArrayList<String>();
            for (Organization validOrg : validOrgs) {
                orgNameList.add(validOrg.getName());
            }
            text = String.join(", ", orgNameList);
        }
        if (text == null || text.isEmpty()) {
            this.shell.setText(String.format(WINDOW_TITLE, "組織未設定"));
        } else {
            this.shell.setText(String.format(WINDOW_TITLE, text));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if ("tsv".equals(event.getPropertyName())) {
            System.out.println("tsv main");
        }
    }

    /**
     * @param listener
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    /**
     * @param listener
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }
}
