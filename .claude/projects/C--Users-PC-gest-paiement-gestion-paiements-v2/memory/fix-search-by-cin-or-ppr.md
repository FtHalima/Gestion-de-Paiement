---
name: fix-search-by-cin-or-ppr
description: Correct the repository query for searching professor by CIN or PPR to avoid returning all records when one parameter is empty
metadata:
  type: project
---
# Fix for professor search by CIN or PPR

## Problem
When entering any value in either the CIN or PPR field (leaving the other blank), the search returned the same professor (often the first record in the database). This happened because the JPQL query used an OR condition that treated a NULL parameter as always true, causing the query to match all professors when one side was NULL.

## Root Cause
The original query in `ProfesseurRepository.findByCinOrPpr` was:
```sql
SELECT p FROM Professeur p WHERE (:cin IS NULL OR p.cin = :cin) OR (:ppr IS NULL OR p.ppr = :ppr)
```
If, for example, `:cin` was a non‑null value and `:ppr` was NULL, the second sub‑expression `:ppr IS NULL` evaluated to TRUE, making the whole OR expression TRUE for every row, thus returning all professors (and the service returned the first one).

## Solution
Changed the query to only consider a side when its parameter is NOT NULL:
```sql
SELECT p FROM Professeur p WHERE ((:cin IS NOT NULL AND p.cin = :cin) OR (:ppr IS NOT NULL AND p.ppr = :ppr))
```
Now:
- If only CIN is provided, the query matches professors where `p.cin = :cin`.
- If only PPR is provided, it matches professors where `p.ppr = :ppr`.
- If both are provided, it matches professors where either field matches.
- If both are NULL/empty, the query would match nothing (the service already blocks this case with an error).

## Files Modified
- `src/main/java/com/gestionpaiements/app/dao/ProfesseurRepository.java` – updated the `@Query` annotation.

## Result
The search now behaves correctly:
- Entering a valid CIN returns the professor with that CIN (if any).
- Entering a valid PPR returns the professor with that PPR (if any).
- Entering non‑matching values shows “Professeur introuvable.”
- Leaving both fields empty shows the validation error “Veuillez saisir le CIN ou le PPR.”

The project compiles successfully (`mvn compile` → BUILD SUCCESS).