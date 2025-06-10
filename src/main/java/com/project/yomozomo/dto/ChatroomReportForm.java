package com.project.yomozomo.dto;

import lombok.Data;

@Data
public class ChatroomReportForm {
    private Long chatRoomId;
    private Long reporterId;
    private Long reportedId;
    private String title;
    private String content;
}