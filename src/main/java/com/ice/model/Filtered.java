package com.ice.model;

public class Filtered {

    private String clientId;

    private String creditDebitIndicator;

    private String status;

    private String bookingDateTime;

    private String accountId;

    private String amountAmount;

    private String amountCurrency;

    private String TransactionInformation;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public void setCreditDebitIndicator(String creditDebitIndicator) {
        this.creditDebitIndicator = creditDebitIndicator;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookingDateTime() {
        return bookingDateTime;
    }

    public void setBookingDateTime(String bookingDateTime) {
        this.bookingDateTime = bookingDateTime;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAmountAmount() {
        return amountAmount;
    }

    public void setAmountAmount(String amountAmount) {
        this.amountAmount = amountAmount;
    }

    public String getAmountCurrency() {
        return amountCurrency;
    }

    public void setAmountCurrency(String amountCurrency) {
        this.amountCurrency = amountCurrency;
    }

    public String getTransactionInformation() {
        return TransactionInformation;
    }

    public void setTransactionInformation(String transactionInformation) {
        TransactionInformation = transactionInformation;
    }
}
