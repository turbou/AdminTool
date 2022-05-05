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

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.api.Api;
import com.contrastsecurity.admintool.api.ControlDeleteApi;
import com.contrastsecurity.admintool.api.ControlsApi;
import com.contrastsecurity.admintool.model.Control;
import com.contrastsecurity.admintool.model.Organization;

public class SanitizerDeleteWithProgress implements IRunnableWithProgress {

    public enum FILTER_MODE {
        NONE,
        INCLUDE,
        EXCLUDE
    }

    public enum COMPARE_MODE {
        STARTSWITH,
        ENDSWITH,
        CONTAINS,
        FULLMATCH
    }

    private Shell shell;
    private PreferenceStore ps;
    private List<Organization> orgs;
    private String includeName;
    private String excludeName;
    private FILTER_MODE filterMode;

    Logger logger = LogManager.getLogger("admintool");

    public SanitizerDeleteWithProgress(Shell shell, PreferenceStore ps, List<Organization> orgs, String includeName, String excludeName) {
        this.shell = shell;
        this.ps = ps;
        this.orgs = orgs;
        this.includeName = includeName;
        this.excludeName = excludeName;
        if (this.includeName.isEmpty() && this.excludeName.isEmpty()) {
            this.filterMode = FILTER_MODE.NONE;
        } else if (this.excludeName.isEmpty()) {
            this.filterMode = FILTER_MODE.INCLUDE;
        } else {
            this.filterMode = FILTER_MODE.EXCLUDE;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("セキュリティ制御(サニタイザ)の削除...", 100 * this.orgs.size());
        Thread.sleep(300);
        for (Organization org : this.orgs) {
            try {
                monitor.setTaskName(org.getName());
                // アプリケーション一覧を取得
                monitor.subTask("セキュリティ制御(サニタイザ)の情報を取得...");
                Api applicationsApi = new ControlsApi(this.shell, this.ps, org);
                List<Control> controls = (List<Control>) applicationsApi.get();
                SubProgressMonitor sub3Monitor = new SubProgressMonitor(monitor, 80);
                sub3Monitor.beginTask("", controls.size());
                for (Control control : controls) {
                    if (monitor.isCanceled()) {
                        throw new InterruptedException("キャンセルされました。");
                    }
                    monitor.subTask(String.format("セキュリティ制御(サニタイザ)を削除...%s", control.getName()));
                    switch (this.filterMode) {
                        case INCLUDE:
                            break;
                        case EXCLUDE:
                            if (control.getName().startsWith(excludeName)) {
                                continue;
                            }
                            break;
                        case NONE:
                            break;
                    }
                    Api controlDeleteApi = new ControlDeleteApi(this.shell, this.ps, org, control.getId());
                    controlDeleteApi.delete();
                    sub3Monitor.worked(1);
                }
                sub3Monitor.done();
                Thread.sleep(500);
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
        monitor.done();
    }
}
