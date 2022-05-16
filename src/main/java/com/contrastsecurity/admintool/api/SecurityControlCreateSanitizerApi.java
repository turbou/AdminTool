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

package com.contrastsecurity.admintool.api;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.exception.JsonException;
import com.contrastsecurity.admintool.json.ContrastJson;
import com.contrastsecurity.admintool.model.Organization;
import com.contrastsecurity.admintool.model.SecurityControl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class SecurityControlCreateSanitizerApi extends Api {

    private SecurityControl control;

    public SecurityControlCreateSanitizerApi(Shell shell, IPreferenceStore ps, Organization org, SecurityControl control) {
        super(shell, ps, org);
        this.control = control;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        return String.format("%s/api/ng/%s/controls/sanitizers?expand=skip_links", this.contrastUrl, orgId);
    }

    @Override
    protected RequestBody getBody() throws Exception {
        MediaType mediaTypeJson = MediaType.parse("application/json; charset=UTF-8");
        String name = this.control.getName();
        String api = this.control.getApi();
        String language = this.control.getLanguage();
        boolean all_rules = this.control.isAll_rules();
        String json = null;
        if (!all_rules) {
            if (this.control.getRules() == null) {
                throw new JsonException("rulesの指定がありません。");
            }
            List<String> rules = this.control.getRules().stream().map(rule -> rule.getName()).collect(Collectors.toList());
            if (rules.isEmpty()) {
                json = String.format("{\"name\":\"%s\", \"api\":\"%s\", \"language\":\"%s\", \"all_rules\":true, \"rules\":[]}", name, api, language);
            } else {
                json = String.format("{\"name\":\"%s\", \"api\":\"%s\", \"language\":\"%s\", \"all_rules\":false, \"rules\":[%s]}", name, api, language,
                        rules.stream().collect(Collectors.joining("\",\"", "\"", "\"")));
            }
        } else {
            json = String.format("{\"name\":\"%s\", \"api\":\"%s\", \"language\":\"%s\", \"all_rules\":true, \"rules\":[]}", name, api, language);
        }
        return RequestBody.create(json, mediaTypeJson);
    }

    @Override
    protected Object convert(String response) {
        Gson gson = new Gson();
        Type contType = new TypeToken<ContrastJson>() {
        }.getType();
        ContrastJson contrastJson = gson.fromJson(response, contType);
        return contrastJson.getSuccess();
    }

}
