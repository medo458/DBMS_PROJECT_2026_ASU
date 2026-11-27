/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.asu.databaseproject;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;

/**
 *
 * @author INFA
 */
public class Databaseproject {

    // Same connection settings as META-INF/persistence.xml
    private static final String DB_URL = "jdbc:postgresql://localhost:5433/library";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "admin123";

    private static final String PERSISTENCE_UNIT = "com.asu_databaseproject_jar_1.0-SNAPSHOTPU";

    public static void main(String[] args) {
        // 1. Apply pending migrations from classpath:db/migration (already-applied ones are skipped)
        Flyway flyway = Flyway.configure()
                .dataSource(DB_URL, DB_USER, DB_PASSWORD)
                .locations("classpath:db/migration")
                .load();
        MigrateResult result = flyway.migrate();
        System.out.println("Flyway: applied " + result.migrationsExecuted
                + " migration(s), schema now at version " + result.targetSchemaVersion);

        // 2. Start Hibernate; with action=validate it fails fast if the entities don't match the schema
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT)) {
            System.out.println("Hibernate: schema validation passed");
        }
    }
}
