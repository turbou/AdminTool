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

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.api.Api;
import com.contrastsecurity.admintool.api.SecurityControlCreateSanitizerApi;
import com.contrastsecurity.admintool.api.SecurityControlCreateValidatorApi;
import com.contrastsecurity.admintool.exception.ApiException;
import com.contrastsecurity.admintool.json.RuleDeserializer;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.Rule;
import com.contrastsecurity.admintool.model.SecurityControl;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class SecurityControlImportWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private Organization org;
    private String filePath;
    private List<SecurityControl> successControls;
    private List<SecurityControl> failureControls;

    Logger logger = LogManager.getLogger("admintool");

    public SecurityControlImportWithProgress(Shell shell, PreferenceStore ps, Organization org, String filePath) {
        this.shell = shell;
        this.ps = ps;
        this.org = org;
        this.filePath = filePath;
        this.successControls = new ArrayList<SecurityControl>();
        this.failureControls = new ArrayList<SecurityControl>();
    }

    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("セキュリティ制御のインポート...", 100);
        Thread.sleep(300);
        List<SecurityControl> mapList = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(filePath));
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Rule.class, new RuleDeserializer());
            mapList = gsonBuilder.create().fromJson(reader, new TypeToken<List<SecurityControl>>() {
            }.getType());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        try {
            monitor.setTaskName(org.getName());
            for (SecurityControl control : mapList) {
                if (monitor.isCanceled()) {
                    throw new InterruptedException("キャンセルされました。");
                }
                monitor.subTask(String.format("セキュリティ制御をインポート...%s", control.getName()));
                String type = control.getType();
                Api api = null;
                try {
                    if (type.equals("SANITIZER")) {
                        api = new SecurityControlCreateSanitizerApi(shell, this.ps, org, control);
                    } else if (type.equals("INPUT_VALIDATOR")) {
                        api = new SecurityControlCreateValidatorApi(shell, this.ps, org, control);
                    } else {
                        control.setRemarks(String.format("セキュリティ制御のタイプが判別できません。%s", type));
                        this.failureControls.add(control);
                        continue;
                    }
                    String msg = (String) api.post();
                    if (Boolean.valueOf(msg)) {
                        this.successControls.add(control);
                    } else {
                        this.failureControls.add(control);
                    }
                } catch (ApiException apie) {
                    control.setRemarks(apie.getMessage());
                    this.failureControls.add(control);
                }
            }
            Thread.sleep(500);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
        monitor.done();
        SecurityControlImportResultDialog dialog = new SecurityControlImportResultDialog(shell, this.successControls, this.failureControls);
        this.shell.getDisplay().syncExec(new Runnable() {
            public void run() {
                int result = dialog.open();
                if (IDialogConstants.OK_ID != result) {
                    monitor.setCanceled(true);
                }
            }
        });

    }
}
