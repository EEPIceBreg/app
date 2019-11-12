package com.ice.model;

public class FilterConfig {

    private String bankAISP;

    private String filter_Path;

    private String filter_Table_Schema;

    public String getBankAISP() {
        return bankAISP;
    }

    public void setBankAISP(String bankAISP) {
        this.bankAISP = bankAISP;
    }

    public String getFilter_Path() {
        return filter_Path;
    }

    public void setFilter_Path(String filter_Path) {
        this.filter_Path = filter_Path;
    }

    public String getFilter_Table_Schema() {
        return filter_Table_Schema;
    }

    public void setFilter_Table_Schema(String filter_Table_Schema) {
        this.filter_Table_Schema = filter_Table_Schema;
    }
}
