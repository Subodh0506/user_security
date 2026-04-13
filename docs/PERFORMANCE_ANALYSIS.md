# Performance analysis: getByUser and request timing


CREATE TABLE users (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,

    user_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    verified bool DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
               ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_name UNIQUE (user_name),
    CONSTRAINT uk_users_email UNIQUE (email)
);


## What you're seeing

| Scenario              | ServiceImpl.getByUser() | Overall request |
|-----------------------|-------------------------|------------------|
| Hit DB                | 834 ms                  | ~2000 ms         |
| Hit cache             | 1 ms                    | ~327 ms          |

So:
- **DB case:** ~1166 ms is spent *outside* the measured service method (filters, security, transaction, connection, response).
- **Cache case:** ~326 ms is spent *outside* the service (no DB, but still filters, security, serialization, etc.).

---

## 1. Why is the DB lookup ~834 ms when `user_name` is unique (indexed)?

The **query itself** on an indexed `user_name` with 8 rows should be well under 10 ms. So most of the 834 ms is usually **not** the SELECT execution.

Typical causes:

| Cause | What happens | Typical cost |
|-------|----------------|--------------|
| **Connection acquisition** | First request (or cold pool): get connection from pool or open new one (TCP, auth, maybe SSL). | 50–500+ ms |
| **Hibernate / JPA startup** | First query in a session: metadata, SQL generation, session setup. | 10–200 ms |
| **Transaction begin/commit** | Each `findByName` runs in a transaction; begin + commit each do a round-trip. | 1–2 round-trips |
| **Network to DB** | If MySQL is on another host/Docker, each round-trip adds latency. | 1–50+ ms per round-trip |

So the 834 ms is likely: **connection + first-query overhead + transaction + network**, not “index lookup is slow.”

**How to confirm:**

- Run in MySQL: `EXPLAIN SELECT * FROM users WHERE user_name = 'subodh';`  
  You should see `key = uk_users_name` (or similar).
- Enable timing in logs (see below) to see “time to get connection” vs “time of query.”

---

## 2. Where does the rest of the time go? (e.g. 2 s total − 834 ms ≈ 1.2 s)

Rough breakdown of “overall request” time:

| Layer | When DB is used | When cache is used |
|-------|------------------|---------------------|
| **Before controller** | Request parsing, **Spring Security** (Basic auth: decode header, load user, check password). | Same. |
| **Controller + service** | Your aspect measures this (834 ms or 1 ms). | Same. |
| **After service** | Transaction commit, release connection, **JSON serialization**, **response write**. | No DB; mainly serialization + response. |
| **Connection pool** | If a new connection was created, it’s returned to the pool (usually fast). | N/A |

So the extra ~1.2 s when using DB can be:

- **Security filter chain** (usually small for Basic auth, unless something blocks).
- **Connection creation** (if it happens *outside* the first repository call in some path).
- **Transaction commit** and returning the connection.
- **Logging / I/O** (e.g. sync appenders, slow console).
- **Cold JVM** (first requests slower due to JIT).

The ~327 ms when using cache is “cost of the rest of the stack” without DB: security, controller, serialization, response. That’s a bit high for a tiny JSON response; often it’s security + first-request or environment overhead.

---

## 2b. Why Spring Security adds ~200–500 ms (50–100 ms → 250–600 ms)

With Spring Security enabled, every request goes through the **security filter chain** and, for authenticated endpoints, through **HTTP Basic authentication**. That adds roughly 200–500 ms.

| Cause | What happens | Typical cost |
|-------|----------------|--------------|
| **Security filter chain** | Many filters run in order: `SecurityContextPersistenceFilter`, `RequestCacheAwareFilter`, `BasicAuthenticationFilter`, `FilterSecurityInterceptor`, etc. Each does work (context setup, request wrapping, auth decision). | 10–50 ms |
| **HTTP Basic on every request** | For each authenticated request the client sends `Authorization: Basic base64(username:password)`. Spring Security must decode the header, load `UserDetails`, and **verify the password**. | 5–20 ms (decode + lookup) |
| **Password verification (BCrypt)** | If the configured `PasswordEncoder` is BCrypt (default in many setups), **every** authenticated request runs `BCrypt.matches(plainPassword, storedHash)`. BCrypt is intentionally CPU-heavy (e.g. strength 10 ≈ 50–150 ms per check). | **50–200+ ms per request** |
| **SecurityContext + thread** | Creating/clearing `SecurityContextHolder` and any session/context logic. | 1–10 ms |

So the jump from 50–100 ms to 250–600 ms is largely:

1. **BCrypt (or similar) on every request** when using HTTP Basic: the same password is re-verified for every call to `/v1/users`, `GET /v1/{username}`, etc.
2. **Filter chain overhead** for every request (including permitAll like `/v1/sign-up`).

**Ways to reduce:**

- **Token-based auth (e.g. JWT):** Authenticate once (login with username/password); for subsequent requests only validate the token (signature + expiry). No password check per request → removes most of the 50–200 ms.
- **Session-based auth:** Password checked once per session; later requests use session id (fast lookup). Similar effect.
- **Permit more paths:** If some endpoints are read-only and safe, use `requestMatchers("/v1/users").permitAll()` (or anonymous) so those requests skip auth and password check.
- **Faster encoder (dev only):** Using `NoOpPasswordEncoder` or a weaker BCrypt strength reduces cost but is **not** recommended for production passwords.

---

## 3. Code issue in `getByUser`: double cache lookup + possible NPE

Current pattern:

```java
Users users = localCache.get(username);
if (localCache.containsKey(username)) { ... }
else {
  users = userRepo.findByName(username);  // can be null
  ...
  localCache.put(users.getName(), users); // NPE if users == null
}
```

Problems:

- **Two lookups:** `get()` then `containsKey()`; one `get()` and a null check is enough.
- **Null:** `findByName` can return `null` if user doesn’t exist → NPE when building response or calling `users.getName()`.

Fixing this doesn’t fix 834 ms, but it’s correct and avoids NPE.

---

## 4. What to learn (in order of impact)

1. **Connection pooling (e.g. HikariCP)**  
   - How the pool works, `minimumIdle`, connection creation cost.  
   - Why “first request” or “first use of DB in request” can be slow.

2. **Spring request lifecycle**  
   - Filters → DispatcherServlet → controller → service → transaction/session.  
   - Where security runs and what runs after your `@Bean` (e.g. transaction commit, serialization).

3. **JPA / Hibernate and transactions**  
   - When a transaction starts/ends for a repository call.  
   - That “service time” can include: get connection, begin tx, run query, commit, release.

4. **SQL and indexing**  
   - `EXPLAIN` to confirm the unique index on `user_name` is used.  
   - Why “unique column” usually means “fast lookup” and your 834 ms is likely not the index.

5. **Profiling and observability**  
   - Where exactly time goes: filters, security, DB connection, query, commit, serialization.  
   - Tools: Micrometer, custom filters/interceptors, or a profiler (e.g. JProfiler, VisualVM).

6. **Caching**  
   - You already see cache vs DB; next: cache key design, invalidation, and when not to cache.

---

## 5. Quick improvements (already applied or suggested)

- **Pre-warm connection pool** in `application.properties` so first request doesn’t pay full connection cost.
- **Single cache lookup** and **null-safe** handling in `getByUser` (see code changes).
- **Optional:** add a filter or interceptor to log “time before controller” and “time after controller” to see how much of the 2 s is outside the service.

After that, measure again: DB path and overall request time; then drill into “before service” vs “inside service” vs “after service” if needed.

---

## 6. Bulk insert: why 1000 saves took ~61 s, and saving via API

**Why ~61 s for 1000 users in the test?**

- Each `repo.save()` = **one transaction** (begin + commit) and **one round-trip** to the DB.
- So 1000 commits + 1000 network round-trips. At ~60 ms per save, that's ~60 s.

**What if clients save via the Spring Boot API (e.g. 1000 POST /v1/sign-up)?**

- Each request = **HTTP parsing + Security (auth) + controller + service + one insert + commit + JSON response**.
- So **per user** you pay: DB insert (~60 ms) **plus** API overhead (e.g. 50–200 ms).
- **1000 sequential requests** ≈ 1000 × (60 + 100) ms ≈ **160+ seconds** (worse than the test).
- If clients run in parallel, total wall-clock time can drop, but the DB and app still do 1000 separate transactions and commits.

**What to do for bulk or many inserts:**

| Approach | When to use |
|----------|-------------|
| **Batch insert in one transaction** | Test data, admin tools, imports. Use `saveAll(list)` in one `@Transactional` method, or persist in chunks and `flush()` every N (e.g. 50) with `hibernate.jdbc.batch_size=50`. |
| **Dedicated bulk API** | When the client needs to create many users in one call. Add e.g. `POST /v1/users/bulk` that accepts a list and uses batch insert (one transaction, batched INSERTs). |
| **Keep one user per request** | Normal sign-up: one POST per user is fine; optimize with connection pool and warm-up so each request is fast. |

The tests `addUsersSaveAll()` and `addUsersBatch()` in `SecurityApplicationTests` show the same 1000 users in one transaction (and with JDBC batching in the batch test); run them to compare timings.

---

## 7. How batch insert works (step-by-step)

### What “batch” means here

Normally, each `INSERT` is sent to the database in its own **round-trip**:

```
App: INSERT INTO users (...) VALUES (1, ...);   →  DB
App: INSERT INTO users (...) VALUES (2, ...);   →  DB
...
(1000 round-trips)
```

With **JDBC batching**, the driver can send multiple `INSERT` statements in **one** round-trip (or fewer round-trips):

```
App: INSERT (...); INSERT (...); ... INSERT (...);   →  DB  (e.g. 50 INSERTs in one go)
App: INSERT (...); INSERT (...); ... INSERT (...);   →  DB
...
(20 round-trips for 1000 rows if batch_size=50)
```

So you save **network round-trips** and the DB can process a batch of inserts together.

---

### The two settings

| Setting | Purpose |
|--------|--------|
| **`hibernate.jdbc.batch_size=50`** | Tells Hibernate: “When flushing, send up to 50 statements in one JDBC batch to the DB.” So instead of 50 separate round-trips, you get one (or fewer) per 50 inserts. |
| **`hibernate.order_inserts=true`** | Groups INSERTs together so the JDBC driver can batch them. Without this, Hibernate might interleave other statements and batching is less effective. |

---

### Why “one-by-one” is slow (`addUsers()`)

```text
for (i = 1 to 1000) {
    repo.save(user);   // each save():
}                        // 1. begin transaction (if new)
                         // 2. INSERT one row
                         // 3. commit transaction
                         // 4. round-trip to DB for (2) and (3)
```

So you get **1000 transactions** and **1000 round-trips**. That’s why it takes ~61 s.

---

### Why `addUsersSaveAll()` is faster (but not batched)

```text
@Transactional
list = [user1, user2, ..., user1000]
repo.saveAll(list);   // internally: for each user → save(user)
// end of method → single commit
```

- **One transaction, one commit** for all 1000 rows ⇒ much less overhead than 1000 commits.
- Spring Data’s `saveAll()` usually calls `save()` per entity, so Hibernate still issues **1000 INSERT statements**. With `batch_size` and `order_inserts`, Hibernate *can* batch them when it flushes, but the default flush point is often at commit. So you get one big flush at the end: 1000 INSERTs may go in one or a few batches depending on driver and Hibernate behavior. Even without batching, one transaction already makes it faster than 1000 commits.

---

### How `addUsersBatch()` uses batch insert (the one that really batches)

```text
@Transactional
batchSize = 50

for (i = 1 to 1000) {
    entityManager.persist(user);   // no SQL yet; Hibernate queues the INSERT

    if (i % 50 == 0) {
        entityManager.flush();     // “Send everything queued so far to the DB”
                                  // Hibernate sends up to 50 INSERTs as one JDBC batch
        entityManager.clear();    // Clear the persistence context so memory doesn’t grow
    }
}
// end of method → one commit (for any remaining changes)
```

Step-by-step:

1. **`persist(user)`**  
   Hibernate puts the entity into the **persistence context** and marks it for insert. It does **not** send SQL yet.

2. **When you call `flush()`**  
   Hibernate writes all pending changes to the DB. With `batch_size=50` and `order_inserts=true`, it groups INSERTs and sends them as **JDBC batches** (e.g. 50 INSERTs in one batch). So every 50 users you get **one** (or a few) round-trip(s) instead of 50.

3. **`clear()`**  
   Clears the persistence context so you don’t keep 1000 entities in memory. The rows are already in the DB after `flush()`, so it’s safe. Next loop iteration starts with an empty context again.

4. **End of method**  
   One transaction commit. If there are any remaining pending changes (e.g. last 0–49 rows when 1000 % 50 == 0), they get committed here.

So:

- **Without batching:** 1000 INSERTs ⇒ 1000 round-trips.
- **With batch_size=50 and flush every 50:** 1000 INSERTs ⇒ about **20 round-trips** (20 × 50). That’s why `addUsersBatch()` is the fastest in the test.

---

### Summary

| Approach | Transactions | Round-trips (approx) | Main reason |
|----------|--------------|-----------------------|------------|
| `addUsers()` (one-by-one) | 1000 | 1000 | One commit and one round-trip per row. |
| `addUsersSaveAll()` | 1 | 1000 (or batched at commit) | One commit; INSERTs may be batched at flush/commit. |
| `addUsersBatch()` | 1 | ~20 (if batch_size=50) | One commit + explicit flush every 50 so Hibernate sends JDBC batches. |

Batch insert here = **fewer network round-trips** by sending multiple INSERTs in one JDBC batch, controlled by `hibernate.jdbc.batch_size` and by calling `flush()` (and optionally `clear()`) every N entities.
