package com.uni.ethesis.enums;

import lombok.ToString;

@ToString
public enum ThesisStatus {
    WAITING_FOR_REVIEW,
    READY_FOR_DEFENSE,
    WAITING_FOR_DEFENSE,
    IN_DEFENSE_PROCESS,
    DEFENDED,
    FAILED;
}
