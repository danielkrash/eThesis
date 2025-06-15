package com.uni.ethesis.data.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @ColumnDefault("gen_random_uuid()")
    private UUID id;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("current_timestamp")
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();;

    @LastModifiedDate
    @Column(name = "last_modified_at", insertable = false)
    private OffsetDateTime lastModifiedAt;
}
