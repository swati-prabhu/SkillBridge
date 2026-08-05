package com.skillbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApplicationStatusEvent {
    private Long applicationId;
    private Long studentId;
    private String internshipTitle;
    private String company;
    private String status;
    private String message;
}
