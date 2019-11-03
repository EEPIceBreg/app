package com.ice.model;

public class ResponseJson {
    private String clientId;
    private String transactionInJson;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getTransactionInJson() {
        return transactionInJson;
    }

    public void setTransactionInJson(String transactionInJson) {
        this.transactionInJson = transactionInJson;
    }
}
