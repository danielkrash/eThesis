package com.uni.ethesis.data.entities;

import com.nimbusds.oauth2.sdk.util.date.DateWithTimeZoneOffset;
import jakarta.persistence.*;
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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
public class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ColumnDefault("gen_random_uuid()")
    private UUID id;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("current_timestamp")
    private OffsetDateTime createdAt;
    @LastModifiedDate
    @Column(name = "last_modified_at", insertable = false)
    private OffsetDateTime lastModifiedAt;
}

