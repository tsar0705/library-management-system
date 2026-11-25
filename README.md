# Library Management System: Low-Level Design in Java

![CI](https://github.com/tsar0705/library-management-system/actions/workflows/ci.yml/badge.svg)

A Java 17 library domain model built as a low-level design exercise: clean layers, SOLID, and the parts that usually get skipped in toy projects, namely **business rules, concurrency safety and automated tests**. There is no UI or database on purpose; storage sits behind interfaces so one can be added without touching the services.

## What it does

- **Catalog:** add books, look them up, case-insensitive "contains" search by title or author, list available books.
- **Members:** register members with validated input (non-blank fields, well-formed e-mail).
- **Issue / return:** every loan gets a due date; every issue and return is kept as an audit record (`IssueRecord`).
- **Business rules** (all configurable through `LibraryPolicy`):

| Rule | Behaviour |
|---|---|
| Loan period | Due date = issue time + policy days (default 14) |
| Loan limit | A member cannot hold more than N active loans (default 3) -> `LoanLimitExceededException` |
| Availability | A book that is out cannot be issued -> `BookNotAvailableException` |
| Fines | Late returns are charged per **started** day via a pluggable `FineCalculator` strategy (`BigDecimal`, default 5.00/day) |
| Reservations | FIFO queue per book. After a return, only the first member in the queue may borrow it -> `BookReservedException` for everyone else |
| Reporting | Overdue loans, outstanding fine for a loan still out, active loans per member |

All domain errors extend one base type, `LibraryException`, so a caller can handle every business-rule failure with a single `catch`.

## Architecture

```
model        -> entities: Book, Member, IssueRecord, Reservation, BookStatus
policy       -> LibraryPolicy (rules), FineCalculator (strategy) + DailyRateFineCalculator
repository   -> interfaces + thread-safe in-memory implementations
service      -> LibraryCatalog, MemberService, IssueService, ReservationService
concurrent   -> KeyedLocks (one lock per key)
exception    -> LibraryException and its subtypes
util         -> argument validation
```

`Main` is the composition root: it wires implementations into services by constructor injection and runs a console walkthrough of every rule above.

### Design decisions

- **Dependency inversion.** Services depend on repository *interfaces*; the in-memory classes are injected at the composition root. Moving to JDBC/JPA means writing new repository classes, not changing services.
- **Strategy for fines.** `FineCalculator` is an interface, so a different pricing scheme (grace period, cap, per-category rates) is a new class, not an edit to `IssueServiceImpl`.
- **Injected `Clock`.** Services take a `java.time.Clock`, so due dates, overdue checks and fines are tested deterministically with a controllable clock instead of `Thread.sleep`.
- **Money as `BigDecimal`**, never `double`.
- **Domain exceptions instead of nulls and booleans.** Failures are explicit and carry the offending ids.
- **Intention-revealing entities.** `Book` exposes `markIssued()`/`markAvailable()` rather than a raw status setter; `IssueRecord` refuses to be returned twice.

### Concurrency

Issuing a book is a *check-then-act* sequence (is it free? has the member room? -> mark issued, save a record). Without protection, two simultaneous requests can both pass the checks.

- `issueBook` takes the **member lock, then the book lock**; `returnBook` takes only the **book lock**. A single global acquisition order (member before book) means two operations can never wait on each other, so there is no deadlock.
- Locks are per key (`KeyedLocks`), so unrelated books and members never block each other.
- Shared state is thread-safe: `volatile` fields on entities, `ConcurrentSkipListMap`/`ConcurrentHashMap` in repositories, and an atomic `enqueueIfAbsent` for reservations.

## Tests

**65 JUnit 5 tests** (about 900 lines) covering:

| Area | Tests |
|---|---|
| Entities and validation (`Book`, `Member`, `IssueRecord`) | 12 |
| Policy and fine calculation (including boundary cases: exactly due, 1 second late, exact multiples of a day) | 6 |
| Repositories (ordering, search, active-loan queries, FIFO queue) | 11 |
| Services: catalog, members, issue/return, reservations | 28 |
| Concurrency and locks | 7 |
| End-to-end demo smoke test | 1 |

The concurrency tests use a start gate so all threads are released together, and a 30-second watchdog so a deadlock fails the build instead of hanging it:

- 32 members race for one book -> exactly one succeeds.
- One member races for 6 different books with a limit of 3 -> exactly 3 succeed. This test uses a deliberately slow repository to widen the race window, so it fails reliably if the member lock is removed (verified: without the lock all 6 succeed).
- Many threads issuing and returning in a loop -> state stays consistent and nothing deadlocks.
- One member reserving from 16 threads -> queued exactly once.

```bash
mvn test            # run the suite
mvn verify          # build the jar as well
```

CI (`.github/workflows/ci.yml`) runs `mvn verify` and the console demo on every push and pull request.

## Run

```bash
mvn clean package
java -jar target/library-management-system.jar
```

Without Maven (JDK 17+):

```bash
find src/main -name "*.java" > sources.txt
mkdir -p out && javac -d out @sources.txt
java -cp out com.library.Main
```

The demo shows: search, issuing, the loan limit rejecting a third book, an issued book rejecting a second borrower, a reservation queue blocking the previous holder after a return, and a late return being charged a fine.

## Limitations

- **In-memory only.** Data is lost on exit. The locking is per JVM; with several application instances you would rely on database transactions or row locks instead.
- **Reservations never expire.** A member at the front of the queue who never returns to borrow blocks the book. A hold timeout would fix this.
- **Locks are never evicted**, one lock object per id stays in memory. Fine for a demo; a long-running service would use striped locks.
- **No persistence layer, REST API or authentication**; this is a domain-model exercise.
- Fines use "every started day counts" and a flat daily rate; amounts are currency-agnostic.

## Project layout

```
.github/workflows/ci.yml
src/main/java/com/library/Main.java
src/main/java/com/library/concurrent/KeyedLocks.java
src/main/java/com/library/exception/BookNotAvailableException.java
src/main/java/com/library/exception/BookNotFoundException.java
src/main/java/com/library/exception/BookReservedException.java
src/main/java/com/library/exception/DuplicateEntityException.java
src/main/java/com/library/exception/LibraryException.java
src/main/java/com/library/exception/LoanLimitExceededException.java
src/main/java/com/library/exception/MemberNotFoundException.java
src/main/java/com/library/exception/NoActiveIssueException.java
src/main/java/com/library/exception/ReservationException.java
src/main/java/com/library/model/Book.java
src/main/java/com/library/model/BookStatus.java
src/main/java/com/library/model/IssueRecord.java
src/main/java/com/library/model/Member.java
src/main/java/com/library/model/Reservation.java
src/main/java/com/library/policy/DailyRateFineCalculator.java
src/main/java/com/library/policy/FineCalculator.java
src/main/java/com/library/policy/LibraryPolicy.java
src/main/java/com/library/repository/BookRepository.java
src/main/java/com/library/repository/InMemoryBookRepository.java
src/main/java/com/library/repository/InMemoryIssueRecordRepository.java
src/main/java/com/library/repository/InMemoryMemberRepository.java
src/main/java/com/library/repository/InMemoryReservationRepository.java
src/main/java/com/library/repository/IssueRecordRepository.java
src/main/java/com/library/repository/MemberRepository.java
src/main/java/com/library/repository/ReservationRepository.java
src/main/java/com/library/service/IssueService.java
src/main/java/com/library/service/IssueServiceImpl.java
src/main/java/com/library/service/LibraryCatalog.java
src/main/java/com/library/service/LibraryCatalogImpl.java
src/main/java/com/library/service/MemberService.java
src/main/java/com/library/service/MemberServiceImpl.java
src/main/java/com/library/service/ReservationService.java
src/main/java/com/library/service/ReservationServiceImpl.java
src/main/java/com/library/util/Validation.java
src/test/java/com/library/MainSmokeTest.java
src/test/java/com/library/concurrent/KeyedLocksTest.java
src/test/java/com/library/model/BookTest.java
src/test/java/com/library/model/IssueRecordTest.java
src/test/java/com/library/model/MemberTest.java
src/test/java/com/library/policy/DailyRateFineCalculatorTest.java
src/test/java/com/library/policy/LibraryPolicyTest.java
src/test/java/com/library/repository/InMemoryBookRepositoryTest.java
src/test/java/com/library/repository/InMemoryIssueRecordRepositoryTest.java
src/test/java/com/library/repository/InMemoryReservationRepositoryTest.java
src/test/java/com/library/service/ConcurrencyTest.java
src/test/java/com/library/service/IssueServiceTest.java
src/test/java/com/library/service/LibraryCatalogTest.java
src/test/java/com/library/service/MemberServiceTest.java
src/test/java/com/library/service/ReservationServiceTest.java
src/test/java/com/library/testsupport/Concurrently.java
src/test/java/com/library/testsupport/MutableClock.java
src/test/java/com/library/testsupport/TestLibrary.java
```
