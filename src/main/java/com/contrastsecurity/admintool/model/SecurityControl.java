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

package com.contrastsecurity.admintool.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

public class SecurityControl {
    @Expose(serialize = true)
    private String api;
    @Expose(serialize = true)
    private String language;
    @Expose(serialize = true)
    private String name;
    @Expose(serialize = true)
    private String type;
    @Expose(serialize = true)
    private List<Rule> rules;
    @Expose(serialize = true)
    private boolean all_rules;

    @Expose(serialize = false)
    private int id;
    @Expose(serialize = false)
    private String hash;
    @Expose(serialize = false)
    private boolean enabled;

    @Expose(serialize = false)
    private boolean deleteFlg;
    @Expose(serialize = false)
    private String remarks;

    public SecurityControl() {
    }

    public SecurityControl(Map<String, Object> map) {
        String name = (String) map.get("name");
        String api = (String) map.get("api");
        String language = (String) map.get("language");
        boolean all_rules = ((Boolean) map.get("all_rules"));
        this.name = name;
        this.api = api;
        this.language = language;
        this.all_rules = all_rules;
        if (map.containsKey("remarks")) {
            this.remarks = (String) map.get("remarks");
        }
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAll_rules() {
        return all_rules;
    }

    public void setAll_rules(boolean all_rules) {
        this.all_rules = all_rules;
    }

    public boolean isDeleteFlg() {
        return deleteFlg;
    }

    public void setDeleteFlg(boolean deleteFlg) {
        this.deleteFlg = deleteFlg;
    }

    public String getRemarks() {
        if (remarks == null) {
            return "";
        }
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        List<String> strList = new ArrayList<String>();
        strList.add("id: " + this.id);
        strList.add("name: " + this.name);
        strList.add("type: " + this.type);
        return String.join(", ", strList);
    }

}
