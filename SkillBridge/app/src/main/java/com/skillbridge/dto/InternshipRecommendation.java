package com.skillbridge.dto;

import com.skillbridge.entity.Internship;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class InternshipRecommendation {
    private Internship internship;
    private int matchPercent;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String explanation;
}
