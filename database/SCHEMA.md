# Legacy quiz database

This directory preserves the quiz content before modernization.

## Source database

- Engine and file format: H2 MVStore 1.4.197
- Original runtime file: `../quizdb.mv.db`
- Read-only backup: `backup/quizdb-original.mv.db`
- Original and backup SHA-256: `9051E8D48F6B3344F7BB866E70A90E980BFBC04C807E560997421CD50C6C2112`
- SQL recovery export: `quiz-content.sql`

The SQL export was recovered with the backup-compatible H2 1.4.197 tool and then reduced to the two public table definitions and their data rows.

## Schema

### `PUBLIC.QUESTIONS`

| Column | Type | Nullable |
| --- | --- | --- |
| `QUESTION` | `VARCHAR(255)` | No |
| `QUESTIONID` | `INTEGER` | Yes |
| `DISPLAYTYPE` | `INTEGER` | Yes |
| `ANSWER` | `VARCHAR(255)` | No |

### `PUBLIC.ANSWERS`

| Column | Type | Nullable |
| --- | --- | --- |
| `ANSWERS` | `VARCHAR(255)` | No |
| `QUESTIONID` | `INTEGER` | Yes |
| `ID` | `INTEGER` | Yes |

The legacy schema defines no primary keys, foreign keys, unique constraints, or indexes for these tables.

## Baseline row counts

| Data set | Rows |
| --- | ---: |
| `QUESTIONS` | 50 |
| `ANSWERS` | 126 |

All 50 question IDs are distinct and span 1 through 50. Answer rows reference 42 distinct question IDs; true/false options are normally supplied by the Swing interface rather than stored as answer rows.

Question counts by display type:

| Display type | Legacy presentation | Questions |
| ---: | --- | ---: |
| 1 | Four buttons | 15 |
| 2 | Drop-down | 10 |
| 3 | True/false | 16 |
| 4 | Fill in the blank | 9 |

## Content Verification

Stage 1 content verification used H2 1.4.197. The original database SHA-256 was
`9051E8D48F6B3344F7BB866E70A90E980BFBC04C807E560997421CD50C6C2112`.

Canonical content represented every value as `<length>:<value>`, separated columns with `|`, and separated rows with LF (`CHAR(10)`). The checksums were calculated with `HASH('SHA256', STRINGTOUTF8(canonical_content), 1)`.

| Table | Rows | Canonical column order | Canonical row order | SHA-256 |
| --- | ---: | --- | --- | --- |
| `PUBLIC.QUESTIONS` | 50 | `QUESTIONID`, `QUESTION`, `DISPLAYTYPE`, `ANSWER` | `QUESTIONID` | `216B45179E5AD80DFB6A3EF95DBA84C4D2B755A3E0E9BB2859976F18B167170B` |
| `PUBLIC.ANSWERS` | 126 | `QUESTIONID`, `ID`, `ANSWERS` | `QUESTIONID`, `ID`, `ANSWERS` | `02A648A1B6F840A73FC08CC8B0BBDAAB6A314CC1783E1873A483DE23B01664CB` |

Checksums calculated from a temporary read-only copy of the original database matched the checksums from a fresh restoration of `quiz-content.sql`. The preserved original and backup databases remained unchanged, and both retained the documented original database SHA-256.

## Preservation rule

Do not edit or open the original `quizdb.mv.db` with a newer H2 version. Later migration work should use the SQL export or a copy of the backup.
