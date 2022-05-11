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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.contrastsecurity.admintool.preference.ConnectionPreferencePage;
import com.contrastsecurity.admintool.preference.MyPreferenceDialog;
import com.contrastsecurity.admintool.preference.OtherPreferencePage;
import com.contrastsecurity.admintool.preference.PreferenceConstants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    public static final int MINIMUM_SIZE_HEIGHT = 360;

    private AdminToolShell shell;

    private List<Button> actionBtns;
    private CTabFolder mainTabFolder;

    private Button scExpBtn;
    private Button scDelBtn;
    private Text scFilterWordTxt;
    private Button scImpBtn;
    private Button scCmpBtn;
    private Button scSklBtn;
    private Button rulesShowBtn;

    private Button exExpBtn;
    private Button exDelBtn;
    private Text exFilterWordTxt;
    private Button exImpBtn;
    private Button exCmpBtn;
    private Button exSklBtn;
    private Button rulesExceptionShowBtn;

    private Button settingBtn;

    private PreferenceStore ps;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    Logger logger = LogManager.getLogger("csvdltool");

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
            this.actionBtns = new ArrayList<Button>();
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
            this.ps.setDefault(PreferenceConstants.OPENED_SUB_SC_TAB_IDX, 0);
            this.ps.setDefault(PreferenceConstants.OPENED_SUB_EX_TAB_IDX, 0);

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
                ps.setValue(PreferenceConstants.OPENED_MAIN_TAB_IDX, main_idx);
                ps.setValue(PreferenceConstants.MEM_WIDTH, shell.getSize().x);
                ps.setValue(PreferenceConstants.MEM_HEIGHT, shell.getSize().y);
                ps.setValue(PreferenceConstants.SANITIZER_FILTER_WORD, scFilterWordTxt.getText());
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
                    actionBtns.forEach(b -> b.setEnabled(false));
                    settingBtn.setText("このボタンから基本設定を行ってください。");
                    uiReset();
                } else {
                    actionBtns.forEach(b -> b.setEnabled(true));
                    settingBtn.setText("設定");
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

        // ========== グループ ==========
        Composite vulButtonGrp = new Composite(assessShell, SWT.NULL);
        GridLayout buttonGrpLt = new GridLayout(2, false);
        buttonGrpLt.marginWidth = 10;
        buttonGrpLt.marginHeight = 10;
        vulButtonGrp.setLayout(buttonGrpLt);
        GridData buttonGrpGrDt = new GridData(GridData.FILL_BOTH);
        // buttonGrpGrDt.horizontalSpan = 3;
        // buttonGrpGrDt.widthHint = 100;
        vulButtonGrp.setLayoutData(buttonGrpGrDt);

        // ========== エクスポートボタン ==========
        scExpBtn = new Button(vulButtonGrp, SWT.PUSH);
        GridData sanitizerExportBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        sanitizerExportBtnGrDt.heightHint = 30;
        sanitizerExportBtnGrDt.horizontalSpan = 2;
        scExpBtn.setLayoutData(sanitizerExportBtnGrDt);
        scExpBtn.setText("エクスポート");
        scExpBtn.setToolTipText("セキュリティ制御(サニタイザ)のエクスポート");
        scExpBtn.setFont(new Font(display, "ＭＳ ゴシック", 13, SWT.NORMAL));
        actionBtns.add(scExpBtn);
        scExpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText("出力先フォルダを指定してください。");
                String dir = dialog.open();
                if (dir == null) {
                    return;
                }
                SecurityControlExportWithProgress progress = new SecurityControlExportWithProgress(shell, ps, getValidOrganizations(), dir);
                ProgressMonitorDialog progDialog = new SecurityControlExportProgressMonitorDialog(shell);
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

        // ========== 削除ボタン ==========
        Composite deleteGrp = new Composite(vulButtonGrp, SWT.NULL);
        GridLayout deleteGrpLt = new GridLayout(2, false);
        deleteGrpLt.marginWidth = 0;
        deleteGrpLt.marginHeight = 0;
        deleteGrp.setLayout(deleteGrpLt);
        GridData deleteGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        deleteGrpGrDt.horizontalSpan = 2;
        // deleteGrpGrDt.heightHint = 140;
        deleteGrp.setLayoutData(deleteGrpGrDt);

        scDelBtn = new Button(deleteGrp, SWT.PUSH);
        GridData sanitizerDeleteBtnGrDt = new GridData(GridData.FILL_BOTH);
        scDelBtn.setLayoutData(sanitizerDeleteBtnGrDt);
        scDelBtn.setText("削除対象を表示");
        scDelBtn.setToolTipText("セキュリティ制御の削除");
        scDelBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        actionBtns.add(scDelBtn);
        scDelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String filterWord = scFilterWordTxt.getText().trim();
                SecurityControlDeleteWithProgress progress = new SecurityControlDeleteWithProgress(shell, ps, getValidOrganizations(), filterWord);
                ProgressMonitorDialog progDialog = new SecurityControlDeleteProgressMonitorDialog(shell);
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
                        MessageDialog.openWarning(shell, "セキュリティ制御のエクスポート", String.format("TeamServerからエラーが返されました。\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "セキュリティ制御のエクスポート", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "セキュリティ制御のエクスポート", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "セキュリティ制御のエクスポート", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", errorMsg));
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
        // deleteControlGrpGrDt.heightHint = 70;
        deleteCtrlGrp.setLayoutData(deleteControlGrpGrDt);

        scFilterWordTxt = new Text(deleteCtrlGrp, SWT.BORDER);
        scFilterWordTxt.setText(ps.getString(PreferenceConstants.SANITIZER_FILTER_WORD));
        scFilterWordTxt.setMessage("例) hoge, foo_*, *bar*, *_baz");
        scFilterWordTxt.setToolTipText("削除対象を指定します。アスタリスク使用で前方、後方、部分一致を指定できます。カンマ区切りで複数指定可能です。");
        scFilterWordTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        scFilterWordTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                scFilterWordTxt.selectAll();
            }
        });

        // ========== インポートボタン ==========
        scImpBtn = new Button(vulButtonGrp, SWT.PUSH);
        GridData sanitizerImportBtnGrDt = new GridData(GridData.FILL_BOTH);
        sanitizerImportBtnGrDt.heightHint = 50;
        sanitizerImportBtnGrDt.horizontalSpan = 2;
        scImpBtn.setLayoutData(sanitizerImportBtnGrDt);
        scImpBtn.setText("インポート");
        scImpBtn.setToolTipText("セキュリティ制御(サニタイザ)のインポート");
        scImpBtn.setFont(new Font(display, "ＭＳ ゴシック", 18, SWT.NORMAL));
        actionBtns.add(scImpBtn);
        scImpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dialog = new FileDialog(shell);
                dialog.setText("インポートするjsonファイルを指定してください。");
                dialog.setFilterExtensions(new String[] { "*.json" });
                String file = dialog.open();
                if (file == null) {
                    return;
                }
                SecurityControlImportWithProgress progress = new SecurityControlImportWithProgress(shell, ps, getValidOrganizations(), file);
                ProgressMonitorDialog progDialog = new SecurityControlImportProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // ========== 差分確認ボタン ==========
        scCmpBtn = new Button(vulButtonGrp, SWT.PUSH);
        GridData sanitizerCompareBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // sanitizerCompareBtnGrDt.heightHint = 30;
        sanitizerCompareBtnGrDt.horizontalSpan = 2;
        scCmpBtn.setLayoutData(sanitizerCompareBtnGrDt);
        scCmpBtn.setText("差分確認");
        scCmpBtn.setToolTipText("セキュリティ制御(サニタイザ)の差分確認");
        scCmpBtn.setFont(new Font(display, "ＭＳ ゴシック", 13, SWT.NORMAL));
        actionBtns.add(scCmpBtn);
        scCmpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dialog = new FileDialog(shell);
                dialog.setText("比較する対象のjsonファイルを指定してください。");
                dialog.setFilterExtensions(new String[] { "*.json" });
                String file = dialog.open();
                if (file == null) {
                    return;
                }
            }
        });

        // ========== スケルトン生成ボタン ==========
        scSklBtn = new Button(vulButtonGrp, SWT.PUSH);
        GridData executeBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        scSklBtn.setLayoutData(executeBtnGrDt);
        scSklBtn.setText("スケルトンJSON出力");
        scSklBtn.setToolTipText("セキュリティ制御(サニタイザ)のインポートJSONファイルのスケルトン生成");
        scSklBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        scSklBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText("出力先フォルダを指定してください。");
                String dir = dialog.open();
                if (dir == null) {
                    return;
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try {
                    String fileName = dir + "\\sanitizer_skeleton.json";
                    Writer writer = new FileWriter(fileName);
                    List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("api", "jp.co.contrast.foo(java.lang.String*)");
                    map.put("language", "Java");
                    map.put("name", "Sanitaizer_foo");
                    map.put("type", "SANITIZER or INPUT_VALIDATOR");
                    map.put("all_rules", true);
                    mapList.add(map);
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("api", "jp.co.contrast.foo(java.lang.String*)");
                    map2.put("language", "Java");
                    map2.put("name", "Sanitaizer_bar");
                    map2.put("type", "SANITIZER or INPUT_VALIDATOR");
                    map2.put("all_rules", false);
                    String[] array = { "hql-injection", "sql-injection" };
                    map2.put("rules", Arrays.asList(array));
                    mapList.add(map2);
                    gson.toJson(mapList, writer);
                    writer.close();
                    MessageDialog.openInformation(shell, "セキュリティ制御(サニタイザ)のスケルトンJSON出力", String.format("スケルトンJSONファイルを出力しました。\r\n%s", fileName));
                } catch (Exception e) {
                    MessageDialog.openError(shell, "セキュリティ制御(サニタイザ)のスケルトンJSON出力", e.getMessage());
                }
            }
        });
        // Label icon = new Label(vulButtonGrp, SWT.NONE);
        // Image iconImg = new Image(shell.getDisplay(), Main.class.getClassLoader().getResourceAsStream("help.png"));
        // icon.setImage(iconImg);
        // icon.setToolTipText("設定するユーザーの権限について\r\n・組織ロールはView権限以上が必要です。\r\n・Admin権限を持つユーザーの場合、アプリケーショングループの情報も取得できます。\r\n・アプリケーションアクセスグループはView権限以上が必要です。");

        rulesShowBtn = new Button(vulButtonGrp, SWT.PUSH);
        rulesShowBtn.setText("ルール一覧");
        rulesShowBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        actionBtns.add(rulesShowBtn);
        rulesShowBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                RulesShowDialog rulesShowDialog = new RulesShowDialog(shell);
                rulesShowDialog.open();
            }
        });

        assessTabItem.setControl(assessShell);

        // #################### 例外 #################### //
        CTabItem exceptionTabItem = new CTabItem(mainTabFolder, SWT.NONE);
        exceptionTabItem.setText("例外");

        Composite exceptionShell = new Composite(mainTabFolder, SWT.NONE);
        exceptionShell.setLayout(new GridLayout(1, false));

        // ========== グループ ==========
        Composite exButtonGrp = new Composite(exceptionShell, SWT.NULL);
        GridLayout exButtonGrpLt = new GridLayout(2, false);
        exButtonGrpLt.marginWidth = 10;
        exButtonGrpLt.marginHeight = 10;
        exButtonGrp.setLayout(exButtonGrpLt);
        GridData exButtonGrpGrDt = new GridData(GridData.FILL_BOTH);
        // exButtonGrpGrDt.horizontalSpan = 3;
        // exButtonGrpGrDt.widthHint = 100;
        exButtonGrp.setLayoutData(exButtonGrpGrDt);

        // ========== エクスポートボタン ==========
        exExpBtn = new Button(exButtonGrp, SWT.PUSH);
        GridData exceptionExportBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        exceptionExportBtnGrDt.heightHint = 30;
        exceptionExportBtnGrDt.horizontalSpan = 2;
        exExpBtn.setLayoutData(exceptionExportBtnGrDt);
        exExpBtn.setText("エクスポート");
        exExpBtn.setToolTipText("セキュリティ制御(サニタイザ)のエクスポート");
        exExpBtn.setFont(new Font(display, "ＭＳ ゴシック", 13, SWT.NORMAL));
        actionBtns.add(exExpBtn);
        exExpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText("出力先フォルダを指定してください。");
                String dir = dialog.open();
                SecurityControlExportWithProgress progress = new SecurityControlExportWithProgress(shell, ps, getValidOrganizations(), dir);
                ProgressMonitorDialog progDialog = new SecurityControlExportProgressMonitorDialog(shell);
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

        // ========== 削除ボタン ==========
        Composite deleteExGrp = new Composite(exButtonGrp, SWT.NULL);
        GridLayout deleteExGrpLt = new GridLayout(2, false);
        deleteExGrpLt.marginWidth = 0;
        deleteExGrpLt.marginHeight = 0;
        deleteExGrp.setLayout(deleteExGrpLt);
        GridData deleteExGrpGrDt = new GridData(GridData.FILL_HORIZONTAL);
        deleteExGrpGrDt.horizontalSpan = 2;
        // deleteExGrpGrDt.heightHint = 140;
        deleteExGrp.setLayoutData(deleteExGrpGrDt);

        exDelBtn = new Button(deleteExGrp, SWT.PUSH);
        GridData exceptionDeleteBtnGrDt = new GridData(GridData.FILL_BOTH);
        exDelBtn.setLayoutData(exceptionDeleteBtnGrDt);
        exDelBtn.setText("削除対象を表示");
        exDelBtn.setToolTipText("セキュリティ制御の削除");
        exDelBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        actionBtns.add(exDelBtn);
        exDelBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String filterWord = exFilterWordTxt.getText().trim();
                SecurityControlDeleteWithProgress progress = new SecurityControlDeleteWithProgress(shell, ps, getValidOrganizations(), filterWord);
                ProgressMonitorDialog progDialog = new SecurityControlDeleteProgressMonitorDialog(shell);
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
                        MessageDialog.openWarning(shell, "セキュリティ制御のエクスポート", String.format("TeamServerからエラーが返されました。\r\n%s", errorMsg));
                    } else if (e.getTargetException() instanceof NonApiException) {
                        MessageDialog.openError(shell, "セキュリティ制御のエクスポート", String.format("想定外のステータスコード: %s\r\nログファイルをご確認ください。", errorMsg));
                    } else if (e.getTargetException() instanceof TsvException) {
                        MessageDialog.openInformation(shell, "セキュリティ制御のエクスポート", errorMsg);
                        return;
                    } else {
                        MessageDialog.openError(shell, "セキュリティ制御のエクスポート", String.format("不明なエラーです。ログファイルをご確認ください。\r\n%s", errorMsg));
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Composite deleteExceptionCtrlGrp = new Composite(deleteExGrp, SWT.NULL);
        GridLayout deleteExceptionCtrlGrpLt = new GridLayout(1, false);
        deleteExceptionCtrlGrpLt.marginWidth = 0;
        deleteExceptionCtrlGrpLt.marginHeight = 1;
        deleteExceptionCtrlGrp.setLayout(deleteExceptionCtrlGrpLt);
        GridData deleteExceptionCtrlGrpGrDt = new GridData(GridData.FILL_BOTH);
        // deleteExceptionCtrlGrpGrDt.heightHint = 70;
        deleteExceptionCtrlGrp.setLayoutData(deleteExceptionCtrlGrpGrDt);

        exFilterWordTxt = new Text(deleteExceptionCtrlGrp, SWT.BORDER);
        exFilterWordTxt.setText(ps.getString(PreferenceConstants.SANITIZER_FILTER_WORD));
        exFilterWordTxt.setMessage("例) hoge, foo_*, *bar*, *_baz");
        exFilterWordTxt.setToolTipText("削除対象を指定します。アスタリスク使用で前方、後方、部分一致を指定できます。カンマ区切りで複数指定可能です。");
        exFilterWordTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        exFilterWordTxt.addListener(SWT.FocusIn, new Listener() {
            public void handleEvent(Event e) {
                exFilterWordTxt.selectAll();
            }
        });

        // ========== インポートボタン ==========
        exImpBtn = new Button(exButtonGrp, SWT.PUSH);
        GridData exceptionImportBtnGrDt = new GridData(GridData.FILL_BOTH);
        exceptionImportBtnGrDt.heightHint = 50;
        exceptionImportBtnGrDt.horizontalSpan = 2;
        exImpBtn.setLayoutData(exceptionImportBtnGrDt);
        exImpBtn.setText("インポート");
        exImpBtn.setToolTipText("セキュリティ制御(サニタイザ)のインポート");
        exImpBtn.setFont(new Font(display, "ＭＳ ゴシック", 18, SWT.NORMAL));
        actionBtns.add(exImpBtn);
        exImpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dialog = new FileDialog(shell);
                dialog.setText("インポートするjsonファイルを指定してください。");
                dialog.setFilterExtensions(new String[] { "*.json" });
                String file = dialog.open();
                SecurityControlImportWithProgress progress = new SecurityControlImportWithProgress(shell, ps, getValidOrganizations(), file);
                ProgressMonitorDialog progDialog = new SecurityControlImportProgressMonitorDialog(shell);
                try {
                    progDialog.run(true, true, progress);
                } catch (InvocationTargetException e) {
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // ========== 差分確認ボタン ==========
        exCmpBtn = new Button(exButtonGrp, SWT.PUSH);
        GridData exceptionCompareBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        // exceptionCompareBtnGrDt.heightHint = 30;
        exceptionCompareBtnGrDt.horizontalSpan = 2;
        exCmpBtn.setLayoutData(exceptionCompareBtnGrDt);
        exCmpBtn.setText("差分確認");
        exCmpBtn.setToolTipText("セキュリティ制御(サニタイザ)の差分確認");
        exCmpBtn.setFont(new Font(display, "ＭＳ ゴシック", 13, SWT.NORMAL));
        actionBtns.add(exCmpBtn);
        exCmpBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                FileDialog dialog = new FileDialog(shell);
                dialog.setText("比較する対象のjsonファイルを指定してください。");
                dialog.setFilterExtensions(new String[] { "*.json" });
                String file = dialog.open();
            }
        });

        // ========== スケルトン生成ボタン ==========
        exSklBtn = new Button(exButtonGrp, SWT.PUSH);
        GridData exceptionSkeletonBtnGrDt = new GridData(GridData.FILL_HORIZONTAL);
        exSklBtn.setLayoutData(exceptionSkeletonBtnGrDt);
        exSklBtn.setText("スケルトンJSON出力");
        exSklBtn.setToolTipText("セキュリティ制御(サニタイザ)のインポートJSONファイルのスケルトン生成");
        exSklBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        exSklBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog dialog = new DirectoryDialog(shell);
                dialog.setText("出力先フォルダを指定してください。");
                String dir = dialog.open();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                try {
                    String fileName = dir + "\\sanitizer_skeleton.json";
                    Writer writer = new FileWriter(fileName);
                    List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("api", "jp.co.contrast.foo(java.lang.String*)");
                    map.put("language", "Java");
                    map.put("name", "Sanitaizer_foo");
                    map.put("type", "SANITIZER or INPUT_VALIDATOR");
                    map.put("all_rules", true);
                    mapList.add(map);
                    Map<String, Object> map2 = new HashMap<String, Object>();
                    map2.put("api", "jp.co.contrast.foo(java.lang.String*)");
                    map2.put("language", "Java");
                    map2.put("name", "Sanitaizer_bar");
                    map2.put("type", "SANITIZER or INPUT_VALIDATOR");
                    map2.put("all_rules", false);
                    String[] array = { "hql-injection", "sql-injection" };
                    map2.put("rules", Arrays.asList(array));
                    mapList.add(map2);
                    gson.toJson(mapList, writer);
                    writer.close();
                    MessageDialog.openInformation(shell, "セキュリティ制御(サニタイザ)のスケルトンJSON出力", String.format("スケルトンJSONファイルを出力しました。\r\n%s", fileName));
                } catch (Exception e) {
                    MessageDialog.openError(shell, "セキュリティ制御(サニタイザ)のスケルトンJSON出力", e.getMessage());
                }
            }
        });
        // Label icon = new Label(vulButtonGrp, SWT.NONE);
        // Image iconImg = new Image(shell.getDisplay(), Main.class.getClassLoader().getResourceAsStream("help.png"));
        // icon.setImage(iconImg);
        // icon.setToolTipText("設定するユーザーの権限について\r\n・組織ロールはView権限以上が必要です。\r\n・Admin権限を持つユーザーの場合、アプリケーショングループの情報も取得できます。\r\n・アプリケーションアクセスグループはView権限以上が必要です。");

        rulesExceptionShowBtn = new Button(exButtonGrp, SWT.PUSH);
        rulesExceptionShowBtn.setText("ルール一覧");
        rulesExceptionShowBtn.setFont(new Font(display, "ＭＳ ゴシック", 10, SWT.NORMAL));
        actionBtns.add(rulesExceptionShowBtn);
        rulesExceptionShowBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                RulesShowDialog rulesShowDialog = new RulesShowDialog(shell);
                rulesShowDialog.open();
            }
        });

        exceptionTabItem.setControl(exceptionShell);

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
                mgr.addToRoot(baseNode);
                mgr.addToRoot(connectionNode);
                mgr.addToRoot(otherNode);
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
