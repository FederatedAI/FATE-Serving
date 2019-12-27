package com.webank.ai.fate.metrics;


import java.util.Objects;

public abstract class MmMeter {
    private String name;
    private String tagsString;
    private String desc;

    public MmMeter(String name, String desc, String... tags) {
        this.name = name;
        this.desc = desc;
        this.tagsString = kvArray2String(tags);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTagsString() {
        return tagsString;
    }

    public void setTagsString(String tagsString) {
        this.tagsString = tagsString;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, tagsString);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        MmMeter that = (MmMeter) obj;
        return Objects.equals(name, that.name) && Objects.equals(tagsString, that.tagsString);
    }

    @Override
    public String toString() {
        return "name='" + name + '\'' + ", tagsString="
                + tagsString + ", desc=" + desc;
    }

    private String kvArray2String(String... keyValues) {
        String result = "";
        if (keyValues != null && keyValues.length != 0) {
            if (keyValues.length % 2 == 1) {
                throw new IllegalArgumentException("size must be even, it is a set of key=value pairs");
            } else {
                for(int i = 0; i < keyValues.length; i += 2) {
                    result += (keyValues[i] + ':' + keyValues[i + 1] + ';');
                }
            }
        }
        return result;
    }
}
