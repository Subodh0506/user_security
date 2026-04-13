# Code Coverage Strategy & Best Practices

---

## 1. Enforce Coverage at the Gate (Already in Place)

- **SonarQube Quality Gate**: `sonar.qualitygate.wait=true` ensures the pipeline fails if coverage drops below the configured threshold. Every merge request must pass this.
- **Rule**: No merge if the quality gate fails. Fix or add tests before merging.

---

## 2. Team Rules Everyone Must Follow

### 2.1 For Every New or Changed Code

- **New code**: Write tests **with the same PR** that introduces the code. No “tests in a follow-up” unless explicitly agreed.
- **Modified code**: If you touch existing logic, ensure:
  - Existing tests still pass.
  - New or changed branches are covered by new/updated tests.
- **Refactors**: Keep or improve coverage. Do not remove tests unless the code they tested was removed.

### 2.2 Coverage Expectations

- **New files**: Aim for ≥ 80% line coverage for new code (or the project’s agreed minimum).
- **Critical paths**: Handlers, services, and business logic must have tests; coverage alone is not enough—focus on meaningful cases.
- **Do not**: Add tests only to hit a number; avoid testing implementation details or trivial getters just for coverage.

### 2.3 Before You Push / Open an MR

1. Run tests locally: `go test ./...`
2. Run coverage: `make coverage` (or `go test -coverprofile=coverage.out ./...`)
3. Check: `go tool cover -func=coverage.out` and fix any drop in covered lines for changed packages.
4. Run lint: `make lint` (or `golangci-lint run`)

---

## 3. Best Practices to Prevent Coverage Regression

| Practice | Description |
|----------|-------------|
| **Test with the code** | Same PR contains implementation + tests. No code-only PRs for features/fixes that affect coverage. |
| **Coverage in CI** | CI already runs tests and coverage and sends results to SonarQube. Rely on the quality gate. |
| **Own your package** | When you change a package, you own keeping its coverage stable or improving it. |
| **Review coverage in MRs** | Reviewers check SonarQube (or coverage report) for the changed files and reject MRs that lower coverage without justification. |
| **Baseline and thresholds** | Define a minimum coverage % (e.g. 70–80%) in SonarQube and a rule: “new code must not decrease overall coverage.” |
| **Document exceptions** | If code is excluded from coverage (e.g. main, wire, mocks), document why in code or in this doc. |

---

## 4. Process Checklist for Developers

- [ ] New/changed code has corresponding tests in the same MR.
- [ ] `go test ./...` passes locally.
- [ ] Coverage run locally shows no unintended drop for changed packages.
- [ ] SonarQube quality gate passes on the MR.
- [ ] No unnecessary exclusions added to `sonar.exclusions` or coverage config to “pass” the gate.

---

## 5. Process Checklist for Reviewers

- [ ] MR includes tests for new or modified behavior.
- [ ] SonarQube (or coverage report) shows no coverage regression for changed code.
- [ ] Tests are meaningful (correct assertions, relevant cases), not only coverage-oriented.

---

## 6. Handling Legitimate Exceptions

- **Temporary drop**: If a rare, agreed exception is needed (e.g. large refactor with tests in a follow-up), it must be:
  - Documented in the MR.
  - Tracked (e.g. ticket) to restore coverage by a set date.
  - Approved by tech lead or maintainer.

---

## 7. Keeping the Baseline Healthy

- **Regular reviews**: Periodically review packages with the lowest coverage and add tests in dedicated “coverage improvement” tasks or during feature work in that area.
- **Onboarding**: New members read this doc and run `make coverage` and `make lint` as part of their first PR.
- **Single source of truth**: SonarQube (and `coverage.out` in CI) are the source of record; local coverage should align with what CI produces.

---

## 8. Quick Reference Commands

```bash
# Run all tests
go test ./...

# Run tests with coverage (align with CI)
go test -v -coverprofile=coverage.out ./...

# See coverage by function/file
go tool cover -func=coverage.out

# HTML report (optional)
go tool cover -html=coverage.out -o coverage.html
```

---

*Last updated: Feb 2025. Adjust thresholds and rules in SonarQube and this doc as the team agrees.*
