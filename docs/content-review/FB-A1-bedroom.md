# FB-A1-bedroom — preparation (not applied)

Deferred from FB-A1-1; rewritten with `content/leak-exempt.txt` (uses `big`, `new`). `content/cards` is untouched.

| id | old sentence | new sentence | new translation | note |
|---|---|---|---|---|
| a1-bedroom | My bedroom is tidy. | Her bedroom is big, with a new closet and a soft rug. | Onun yatak odası büyük; yeni bir dolabı ve yumuşak bir halısı var. | faithful; 'closet'/'rug' are not A1 answers; scan clean (R6 matches 'odası' ~ clue 'oda'); qa leak check passes only because `big` and `new` are exempt |

## Patch-ready CSV (id, example_en, example_tr)

```csv
id,example_en,example_tr
a1-bedroom,"Her bedroom is big, with a new closet and a soft rug.","Onun yatak odası büyük; yeni bir dolabı ve yumuşak bir halısı var."
```
