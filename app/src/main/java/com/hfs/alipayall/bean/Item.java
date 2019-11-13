package com.hfs.alipayall.bean;

import java.util.List;

/**
 *
 * @author HuangFusheng
 * @date 2019-11-11
 * description 数据源
 */
public class Item {

    public String name;

    public List<SubItem> mSubItems;

    public static class SubItem {
        private boolean mAdded;
        private String id;
        private String name;
        private String desc;

        public SubItem(String name, String desc, String id) {
            this.name = name;
            this.desc = desc;
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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

        public boolean isAdded() {
            return mAdded;
        }

        public void setAdded(boolean added) {
            mAdded = added;
        }
    }
}
