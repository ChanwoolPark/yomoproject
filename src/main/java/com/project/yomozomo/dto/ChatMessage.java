// ChatMessage DTO (데이터베이스 ID 필드 추가)
class ChatMessage {
    // ... (기존 필드) ...
    private String webSocketRoomId; // 웹소켓 라우팅용
    private Long dbRoomId; // ⭐ 데이터베이스 ChatRoom ID ⭐

    public String getWebSocketRoomId() {
        return webSocketRoomId;
    }

    public void setWebSocketRoomId(String webSocketRoomId) {
        this.webSocketRoomId = webSocketRoomId;
    }

    public Long getDbRoomId() { // ⭐ 새로 추가된 getter/setter ⭐
        return dbRoomId;
    }

    public void setDbRoomId(Long dbRoomId) { // ⭐ 새로 추가된 getter/setter ⭐
        this.dbRoomId = dbRoomId;
    }
}