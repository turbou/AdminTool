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

import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.api.Api;
import com.contrastsecurity.admintool.api.ControlsApi;
import com.contrastsecurity.admintool.model.Control;
import com.contrastsecurity.admintool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class SecurityControlExportWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private List<Organization> orgs;
    private String dirPath;

    Logger logger = LogManager.getLogger("admintool");

    public SecurityControlExportWithProgress(Shell shell, PreferenceStore ps, List<Organization> orgs, String filePath) {
        this.shell = shell;
        this.ps = ps;
        this.orgs = orgs;
        this.dirPath = filePath;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("セキュリティ制御の読み込み...", 100 * this.orgs.size());
        Thread.sleep(300);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        for (Organization org : this.orgs) {
            try {
                Writer writer = new FileWriter(dirPath + "\\" + org.getName() + ".json");
                monitor.setTaskName(org.getName());
                // フィルタの情報を取得
                monitor.subTask("フィルタの情報を取得...");
                // アプリケーション一覧を取得
                monitor.subTask("アプリケーション一覧の情報を取得...");
                Api applicationsApi = new ControlsApi(this.shell, this.ps, org);
                List<Control> controls = (List<Control>) applicationsApi.get();
                SubProgressMonitor sub3Monitor = new SubProgressMonitor(monitor, 80);
                sub3Monitor.beginTask("", controls.size());
                List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
                for (Control control : controls) {
                    monitor.subTask(String.format("アプリケーション一覧の情報を取得...%s", control.getName()));
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("all_rules", Boolean.valueOf(control.isAll_rules()));
                    map.put("api", control.getApi());
                    map.put("language", control.getLanguage());
                    map.put("name", control.getName());
                    mapList.add(map);
                    sub3Monitor.worked(1);
                }
                sub3Monitor.done();
                gson.toJson(mapList, writer);
                writer.close();
                Thread.sleep(500);
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
        monitor.done();
    }
}
