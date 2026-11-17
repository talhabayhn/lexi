# CLUE-NAT-1 review (18 clues, 14 new proposals, 4 keep)

Source: `docs/content-review/CLUE-NAT-1.csv` (edit the CSV, not this file). Scan: `tools/content-qa/clue_scan.py`; standard: `docs/CONTENT.md` "Clue standard (TR)".
Rule hit: C1 single word, C2 more than 8 words, C3 no clear head noun, C4 clue/example mismatch (C3/C4 are advisory; the 50 worst per level are listed). `keep` = the scanner was wrong, reason in the last column.
Approve with: `hepsi onay, hariç: <id> → <clue>`.


## A

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 1 | nat-anachronism | ait olduğu çağın dışında kalmış şey | A fax machine feels like an anachronism today. | C3 | kendi çağına uymayan, zamanı geçmiş unsur |
| 2 | nat-anathema | birine son derece itici, kabul edilemez gelen şey | Debt was anathema to my grandfather. | C3 | birine kesinlikle kabul edilemez gelen düşünce |

## B

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 3 | nat-bellwether | bir eğilimin önceden belirtisi sayılan şey | The town is a bellwether for the national vote. | C3 | sonucu önceden gösteren öncü belirti |
| 4 | nat-boon | büyük nimet, çok işe yarayan şey | The new bridge is a boon to local shops. | C3 | beklenmedik ve çok işe yarayan nimet |

## C

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 5 | nat-connoisseur | (sanat, yemek, şarap) ehli, uzman | He is a connoisseur of fine wine. | C3 | **keep** (clear head noun "uzman"; hit is the -an ending) |

## D

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 6 | nat-desolate | hem ıssız hem de iç karartıcı (yer) | We drove along a desolate road. | C3 | ıssız, boş ve iç karartıcı |

## E

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 7 | nat-enigma | anlaşılmaz, esrarengiz kişi veya şey | My new neighbor remains an enigma to me. | C3 | çözülmesi çok zor bir gizem |
| 8 | nat-euphemism | acı ya da kaba bir şeyi yumuşatan dolaylı söz | "Let go" is a euphemism for "fired". | C2 | kaba gerçeği yumuşatan dolaylı söz |
| 9 | nat-eyesore | göz zevkini bozan çirkin yapı veya şey | That old car park is an eyesore. | C3 | göz zevkini bozan çirkin yapı |

## G

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 10 | nat-gaunt | hastalık ya da açlıktan bir deri bir kemik kalmış | He looked gaunt after his long illness. | C2 | aşırı zayıflamış, çökük yüzlü |

## J

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 11 | nat-jovial | güler yüzlü ve hoş sohbet (çoğu zaman yaşlıca adam) | Our jovial uncle told jokes all night. | C2 | güler yüzlü ve şen, hoş sohbet |

## L

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 12 | nat-linchpin | her şeyin ona bağlı olduğu kilit kişi veya şey | She is the linchpin of the whole team. | C2+C3 | bir işin bağlı olduğu kilit kişi |
| 13 | nat-loathe | iğrenircesine nefret etmek | I loathe having to wait in queues. | C4 | **keep** (accurate verb gloss; C4 is a stem mismatch) |

## O

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 14 | nat-odyssey | (destansı) uzun ve olaylı serüven | Their trip became a three-month odyssey. | C3 | **keep** (clear head noun "serüven"; hit is the -en ending) |

## P

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 15 | nat-pundit | medyada yorum yapan uzman | One TV pundit predicted a close race. | C3 | **keep** (clear head noun "uzman"; hit is the -an ending) |

## Q

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 16 | nat-quagmire | içinden çıkılmaz bataklık gibi durum | The war became a political quagmire. | C3 | içinden çıkılması zor, bataklık gibi sorun |

## S

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 17 | nat-shambles | darmadağın, rezalet halinde durum | The meeting was a complete shambles. | C3 | tam bir karmaşa ve düzensizlik |
| 18 | nat-spate | (soygun, saldırı) kısa sürede sel gibi patlak veren çokluk | There has been a spate of burglaries. | C2 | kısa sürede art arda yaşanan olaylar |
