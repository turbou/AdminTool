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

import com.google.gson.annotations.Expose;

public class Exclusion {
    @Expose(serialize = true)
    private String name;

    @Expose(serialize = true)
    private String type;

    @Expose(serialize = true)
    private List<String> urls;

    @Expose(serialize = true)
    private List<String> codes;

    @Expose(serialize = true)
    private boolean assess;

    @Expose(serialize = true)
    private boolean defend;

    @Expose(serialize = false)
    private int exception_id;

    @Expose(serialize = true)
    private String input_type;

    @Expose(serialize = true)
    private String input_name;

    @Expose(serialize = true)
    private String url_pattern_type;

    @Expose(serialize = true)
    private boolean all_rules;

    @Expose(serialize = true)
    private List<AssessRule> assess_rules;

    @Expose(serialize = true)
    private List<ProtectRule> protection_rules;

    @Expose(serialize = false)
    private boolean deleteFlg;
    @Expose(serialize = false)
    private String remarks;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        strList.add("name: " + this.name);
        strList.add("type: " + this.type);
        // strList.add("all_rules: " + this.all_rules);
        // if (this.rules != null) {
        // Collections.sort(this.rules);
        // strList.add("rules: " + String.join(", ", this.rules.stream().map(rule -> rule.getName()).collect(Collectors.toList())));
        // }
        return String.join(", ", strList);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Exclusion) {
            Exclusion other = (Exclusion) obj;
            return other.name.equals(this.name) && other.type.equals(this.type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode()) + ((type == null) ? 0 : type.hashCode());
        return result;
    }
}
