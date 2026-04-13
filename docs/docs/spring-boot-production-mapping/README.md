# Production-level Entity → DTO mapping (Spring Boot)

Use **MapStruct** for type-safe, compile-time mapping with null safety. Copy these into your Spring Boot project.

## 1. Dependencies and compiler config (pom.xml)

```xml
<dependencies>
    <!-- MapStruct: generates mapper impl at compile time -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
        <scope>provided</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>1.5.5.Final</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok-mapstruct-binding</artifactId>
                        <version>0.2.0</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Important:** Lombok and MapStruct must run together; `lombok-mapstruct-binding` sets the order so both work.

---

## 2. Entity and DTO

See **User-entity-and-dto.md** for `User` and `UserDto` with Lombok (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`). Keep same field names for auto-mapping; use `@Mapping(ignore = true)` for sensitive fields (e.g. password).

---

## 3. Mapper (MapStruct)

See **UserMapper-MapStruct.java**. Copy to `mapper/UserMapper.java`.

- `componentModel = "spring"` → injectable `@Component`
- `toDto(User)` and `toDtoList(List<User>)` for `findAll()` → list of DTOs
- `nullValuePropertyMappingStrategy = IGNORE` for safe partial updates
- Explicit `@Mapping(target = "passwordHash", ignore = true)` so secrets never leak into DTOs

---

## 4. Service

See **UserService-example.java**. Use `userMapper.toDtoList(repo.findAll())` for production-level mapping: one place, type-safe, no reflection at runtime.

---

## Why this is production-level

| Aspect | Approach |
|--------|----------|
| **Performance** | MapStruct generates plain Java at compile time; no reflection. |
| **Type safety** | Compile fails if entity/DTO fields change and mapping breaks. |
| **Null safety** | `toDtoList` and generated code handle null list / null elements as configured. |
| **Security** | Sensitive fields (e.g. password) explicitly ignored in mapper. |
| **Testability** | Mock `UserMapper` in service tests; unit-test mapper separately if needed. |
| **Consistency** | Single mapper interface for all User → UserDto conversions. |
| **Maintainability** | Adding a new DTO field = add to DTO + optional `@Mapping`; compiler guides you. |
