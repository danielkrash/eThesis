package com.uni.ethesis.unit.repository.config;

import jakarta.persistence.EntityManager;

public class PopulateDb {

    public static <T> T persistEntity(EntityManager entityManager, T entity) {
        entityManager.persist(entity);
        entityManager.flush(); // Ensure the entity is persisted immediately
        entityManager.clear(); // Detach the entity to ensure it's reloaded from DB in tests
        return entity;
    }
}





