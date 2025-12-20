package com.oscc.skillexchange.dto.request;

public class ChatRoomRequest {
    private String user1Id;
    private String user2Id;
    private String exchangeRequestId;

    public String getUser1Id() {
        return user1Id;
    }

    public void setUser1Id(String user1Id) {
        this.user1Id = user1Id;
    }

    public String getUser2Id() {
        return user2Id;
    }

    public void setUser2Id(String user2Id) {
        this.user2Id = user2Id;
    }

    public String getExchangeRequestId() {
        return exchangeRequestId;
    }

    public void setExchangeRequestId(String exchangeRequestId) {
        this.exchangeRequestId = exchangeRequestId;
    }
}
