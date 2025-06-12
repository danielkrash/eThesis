package com.uni.ethesis.utils;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DefenseSessionProfessorKey implements Serializable {
    @Column(name = "defense_session_id")
    private UUID defenseSessionId;

    @Column(name = "professor_id")
    private UUID professorId;
}
