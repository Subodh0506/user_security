// package com.security.security.config;

// import com.security.security.repositories.UserRepo;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.stereotype.Component;

// /**
//  * Warms the Hikari connection pool and JPA/Hibernate before the web server
//  * starts accepting traffic. Ensures the first user request does not pay
//  * connection creation and first-query cost. Important for Kubernetes where
//  * new pods should serve fast from the first request.
//  */
// @Component
// @RequiredArgsConstructor
// @Slf4j
// public class DataSourceWarmupRunner implements org.springframework.context.SmartLifecycle {

//     /** Run before web server (Spring Boot 3.x server phase is -1024). */
//     private static final int PHASE_BEFORE_WEB_SERVER = -1025;

//     private final JdbcTemplate jdbcTemplate;
//     private final UserRepo userRepo;

//     private boolean running;

//     @Override
//     public void start() {
//         long start = System.currentTimeMillis();
//         try {
//             jdbcTemplate.queryForObject("SELECT 1", Integer.class);
//             log.info("Hikari pool warm-up: connection and query OK");
//             userRepo.count();
//             log.info("JPA/Hibernate warm-up: repository query OK");
//         } catch (Exception e) {
//             log.warn("Startup warm-up failed (DB may be unavailable): {}", e.getMessage());
//         }
//         log.info("DataSource warm-up completed in {} ms", System.currentTimeMillis() - start);
//         running = true;
//     }

//     @Override
//     public void stop() {
//         running = false;
//     }

//     @Override
//     public boolean isRunning() {
//         return running;
//     }

//     @Override
//     public int getPhase() {
//         return PHASE_BEFORE_WEB_SERVER;
//     }
// }
