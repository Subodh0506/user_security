// package com.security.security;

// import com.security.security.models.Users;
// import com.security.security.repositories.UserRepo;
// import jakarta.persistence.EntityManager;
// import jakarta.persistence.PersistenceContext;
// import lombok.extern.slf4j.Slf4j;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.annotation.Rollback;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.ArrayList;
// import java.util.List;

// @SpringBootTest
// @Slf4j
// class SecurityApplicationTests {

// 	@Autowired
// 	private UserRepo repo;

// 	@PersistenceContext
// 	private EntityManager entityManager;

// 	/**
// 	 * One save per iteration = 1000 transactions/commits and 1000 round-trips.
// 	 * ~61s for 1000 users is expected. Via API it would be worse: each POST = HTTP + auth + 1 insert + response.
// 	 */
// 	@Test
// 	public void addUsers() {
// 		long start = System.currentTimeMillis();
// 		for (int i = 1; i <= 1000; i++) {
// 			repo.save(Users.builder()
// 					.name("test" + i).email("test" + i + "@gmail.com").password("P@sscode123").build());
// 		}
// 		long duration = System.currentTimeMillis() - start;
// 		System.out.println("addUsers() [one-by-one] completed in " + duration + " ms (1000 users)");
// 	}

// 	/**
// 	 * One transaction, saveAll = one commit. Much faster than one-by-one.
// 	 */
// 	@Test
// 	@Transactional
// 	@Rollback(value = false)
// 	public void addUsersSaveAll() {
// 		long start = System.currentTimeMillis();
// 		List<Users> list = new ArrayList<>();
// 		for (int i = 1; i <= 10; i++) {
// 			list.add(Users.builder()
// 					.name("saveAll" + i).email("saveAll" + i + "@gmail.com").password("P@sscode123").build());
// 		}
// 		repo.saveAll(list);
// 		long duration = System.currentTimeMillis() - start;
// 		System.out.println("addUsersSaveAll() [one tx, saveAll] completed in " + duration + " ms (1000 users)");
// 	}

// 	/**
// 	 * Batch insert: persist in chunks and flush every 50 so Hibernate sends batched INSERTs
// 	 * (requires hibernate.jdbc.batch_size=50). Fastest option for bulk insert in test.
// 	 */
// 	@Test
// 	@Transactional
// 	@Rollback(value = false)
// 	public void addUsersBatch() {
// 		int batchSize = 50;
// 		long start = System.currentTimeMillis();
// 		for (int i = 1; i <= 1000; i++) {
// 			Users u = Users.builder()
// 					.name("batch" + i).email("batch" + i + "@gmail.com").password("P@sscode123").build();
// 			entityManager.persist(u);
// 			if (i % batchSize == 0) {
// 				entityManager.flush();
// 				entityManager.clear();
// 			}
// 		}
// 		long duration = System.currentTimeMillis() - start;
// 		System.out.println("addUsersBatch() [batch_size=50] completed in " + duration + " ms (1000 users)");
// 	}
// }
