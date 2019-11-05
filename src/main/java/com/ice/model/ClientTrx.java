package com.ice.model;

public class ClientTrx {

    private String clientId;
    private String transanctionInJson;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTransanctionInJson() {
        return transanctionInJson;
    }

    public void setTransanctionInJson(String transanctionInJson) {
        this.transanctionInJson = transanctionInJson;
    }
}
