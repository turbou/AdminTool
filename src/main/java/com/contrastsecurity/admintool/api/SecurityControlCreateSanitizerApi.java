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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.contrastsecurity.admintool.json.ContrastJson;
import com.contrastsecurity.admintool.model.Organization;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class SecurityControlCreateSanitizerApi extends Api {

    private Map<String, Object> map;

    public SecurityControlCreateSanitizerApi(Shell shell, IPreferenceStore ps, Organization org, Map<String, Object> map) {
        super(shell, ps, org);
        this.map = map;
    }

    @Override
    protected String getUrl() {
        String orgId = this.org.getOrganization_uuid();
        return String.format("%s/api/ng/%s/controls/sanitizers?expand=skip_links", this.contrastUrl, orgId);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected RequestBody getBody() {
        MediaType mediaTypeJson = MediaType.parse("application/json; charset=UTF-8");
        String name = (String) this.map.get("name");
        String api = (String) this.map.get("api");
        String language = (String) this.map.get("language");
        boolean all_rules = Boolean.valueOf((String) this.map.get("all_rules"));
        List<String> rules = null;
        if (!all_rules) {
            rules = (List<String>) this.map.get("rules");
        } else {
            rules = new ArrayList<String>();
        }
        String json = String.format("{\"name\":\"%s\", \"api\":\"%s\", \"language\":\"%s\", \"all_rules\":\"%s\", \"rules\":\"[%s]\"}", name, api, language,
                String.valueOf(all_rules), rules.stream().collect(Collectors.joining("\",\"", "\"", "\"")));
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
