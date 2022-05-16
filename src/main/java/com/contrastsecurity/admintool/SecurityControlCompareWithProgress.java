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
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.api.Api;
import com.contrastsecurity.admintool.api.SecurityControlsApi;
import com.contrastsecurity.admintool.json.RuleDeserializer;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.Rule;
import com.contrastsecurity.admintool.model.SecurityControl;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import difflib.Chunk;
import difflib.Delta;
import difflib.Delta.TYPE;
import difflib.DiffUtils;
import difflib.Patch;

public class SecurityControlCompareWithProgress implements IRunnableWithProgress {

    private Shell shell;
    private PreferenceStore ps;
    private List<Organization> orgs;
    private String filePath;
    private List<SecurityControl> successControls;
    private List<SecurityControl> failureControls;

    Logger logger = LogManager.getLogger("admintool");

    public SecurityControlCompareWithProgress(Shell shell, PreferenceStore ps, List<Organization> orgs, String filePath) {
        this.shell = shell;
        this.ps = ps;
        this.orgs = orgs;
        this.filePath = filePath;
        this.successControls = new ArrayList<SecurityControl>();
        this.failureControls = new ArrayList<SecurityControl>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("セキュリティ制御の差分確認...", 100 * this.orgs.size());
        Thread.sleep(300);
        List<SecurityControl> impList = null;
        List<SecurityControl> expList = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(filePath));
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(Rule.class, new RuleDeserializer());
            impList = gsonBuilder.create().fromJson(reader, new TypeToken<List<SecurityControl>>() {
            }.getType());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        for (Organization org : this.orgs) {
            try {
                monitor.setTaskName(org.getName());
                Api securityControlsApi = new SecurityControlsApi(this.shell, this.ps, org);
                expList = (List<SecurityControl>) securityControlsApi.get();
                Thread.sleep(500);
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
        monitor.done();

        // Patch<SecurityControl> diff = DiffUtils.diff(expList, impList);
        // List<Delta<SecurityControl>> deltas = diff.getDeltas();
        // for (Delta<SecurityControl> delta : deltas) {
        // TYPE type = delta.getType();
        // System.out.println(type);
        // Chunk<SecurityControl> oc = delta.getOriginal();
        // System.out.printf("del: position=%d, lines=%s%n", oc.getPosition(), oc.getLines());
        // Chunk<SecurityControl> rc = delta.getRevised();
        // System.out.printf("add: position=%d, lines=%s%n", rc.getPosition(), rc.getLines());
        // }
        // List<SecurityControl> onlyImpList = new ArrayList<SecurityControl>();
        // List<SecurityControl> onlyExpList = new ArrayList<SecurityControl>();
        List<String> impNames = impList.stream().map(rule -> rule.toString()).collect(Collectors.toList());
        List<String> expNames = expList.stream().map(rule -> rule.toString()).collect(Collectors.toList());
        Patch<String> diff = DiffUtils.diff(impNames, expNames);
        List<Delta<String>> deltas = diff.getDeltas();
        for (Delta<String> delta : deltas) {
            TYPE type = delta.getType();
            System.out.println(type);
            Chunk<String> oc = delta.getOriginal();
            System.out.printf("del: position=%d, lines=%s%n", oc.getPosition(), oc.getLines());
            Chunk<String> rc = delta.getRevised();
            System.out.printf("add: position=%d, lines=%s%n", rc.getPosition(), rc.getLines());
        }
        // System.out.println("Json側にだけある");
        // for (String impName : impNames) {
        // if (!expNames.contains(impName)) {
        // System.out.println(impName);
        // }
        // }
        // System.out.println("TeamServer側にだけある");
        // for (String expName : expNames) {
        // if (!impNames.contains(expName)) {
        // System.out.println(expName);
        // }
        // }
        //
        // for (SecurityControl control : impList) {
        // System.out.println(control.getName());
        // }
        // for (SecurityControl control : expList) {
        // System.out.println(control.getName());
        // }
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
