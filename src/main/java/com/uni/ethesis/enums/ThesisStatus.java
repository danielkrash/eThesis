package com.uni.ethesis.enums;

import lombok.ToString;

@ToString
public enum ThesisStatus {
    WAITING_FOR_REVIEW,
    WAITING_FOR_DEFENSE,
    DEFENDED,
    FAILED;
}
