package com.uni.ethesis.data.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.OffsetDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity implements Serializable {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("current_timestamp")
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "last_modified_at", insertable = false)
    private OffsetDateTime lastModifiedAt;
}

