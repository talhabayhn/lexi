# American spelling standardization

All answers and `example_en` in `content/cards/**` use American spelling. Card ids unchanged. Script-driven (word map, whole-word, case-preserving); `tools/content-qa/qa.py` clean afterwards.

## Answer changes

| id | before | after |
|---|---|---|
| a1-favourite | FAVOURITE | FAVORITE |
| a1-maths | MATHS | MATH |
| a1-centre | CENTRE | CENTER |
| a1-kilometre | KILOMETRE | KILOMETER |
| a1-theatre | THEATRE | THEATER |
| a1-grey | GREY | GRAY |
| a1-colour | COLOUR | COLOR |
| a2-cheque | CHEQUE | CHECK |
| a2-jewellery | JEWELLERY | JEWELRY |
| a2-neighbourhood | NEIGHBOURHOOD | NEIGHBORHOOD |
| a2-organise | ORGANISE | ORGANIZE |
| a2-practise | PRACTISE | PRACTICE |
| a2-moustache | MOUSTACHE | MUSTACHE |
| a2-programme | PROGRAMME | PROGRAM |
| a2-towards | TOWARDS | TOWARD |
| a2-tyre | TYRE | TIRE |
| a2-traveller | TRAVELLER | TRAVELER |
| a2-neighbour | NEIGHBOUR | NEIGHBOR |
| b1-apologise | APOLOGISE | APOLOGIZE |
| b1-behaviour | BEHAVIOUR | BEHAVIOR |
| b1-criticise | CRITICISE | CRITICIZE |
| b1-flavour | FLAVOUR | FLAVOR |
| b1-humour | HUMOUR | HUMOR |
| b1-licence | LICENCE | LICENSE |
| b1-organisation | ORGANISATION | ORGANIZATION |
| b1-recognise | RECOGNISE | RECOGNIZE |
| b1-enquire | ENQUIRE | INQUIRE |
| b1-pyjamas | PYJAMAS | PAJAMAS |
| b1-harbour | HARBOUR | HARBOR |
| b2-analyse | ANALYSE | ANALYZE |
| b2-fulfil | FULFIL | FULFILL |
| b2-enquiry | ENQUIRY | INQUIRY |
| c1-artefact | ARTEFACT | ARTIFACT |
| c1-jeopardise | JEOPARDISE | JEOPARDIZE |
| c1-endeavour | ENDEAVOUR | ENDEAVOR |
| c1-enrol | ENROL | ENROLL |
| c1-mobilise | MOBILISE | MOBILIZE |
| c1-optimise | OPTIMISE | OPTIMIZE |
| c1-penalise | PENALISE | PENALIZE |
| c1-polarise | POLARISE | POLARIZE |
| nat-candour | CANDOUR | CANDOR |
| nat-clamour | CLAMOUR | CLAMOR |
| nat-demeanour | DEMEANOUR | DEMEANOR |
| nat-dishevelled | DISHEVELLED | DISHEVELED |
| nat-gruelling | GRUELLING | GRUELING |
| nat-lacklustre | LACKLUSTRE | LACKLUSTER |
| nat-meagre | MEAGRE | MEAGER |
| nat-chequered | CHEQUERED | CHECKERED |
| nat-rancour | RANCOUR | RANCOR |
| nat-sombre | SOMBRE | SOMBER |

Follow-ups needed because the new spelling broke a rule:
- `a2-cheque` (CHECK duplicated `a2-check`) -> answer replaced by QUILT (Q, STARTS).
- `b1-pyjamas` letter Y -> J (PAJAMAS has no Y); `nat-chequered` letter Q -> K.
- `b1-recognise` (RECOGNIZE duplicated `b2-recognize`) -> answer replaced by RELY (R, STARTS).

## Example sentence changes

| id | before | after |
|---|---|---|
| a1-angry | My neighbour is angry with me. | My neighbor is angry with me. |
| a1-married | My neighbour is married. | My neighbor is married. |
| a1-new | We have new neighbours. | We have new neighbors. |
| a1-boring | This programme is boring. | This program is boring. |
| a1-together | We practise together. | We practice together. |
| a1-favourite | That is my favourite song. | That is my favorite song. |
| a1-maths | We have maths on Tuesdays. | We have math on Tuesdays. |
| a1-cafe | This cafe is cosy. | This cafe is cozy. |
| a1-centre | Ali works in the centre of Ankara. | Ali works in the center of Ankara. |
| a1-eighty | Our neighbour is eighty. | Our neighbor is eighty. |
| a1-magazine | This magazine has colourful pictures. | This magazine has colorful pictures. |
| a1-plant | My neighbour gave me a plant. | My neighbor gave me a plant. |
| a1-russian | Ali's neighbour speaks Russian. | Ali's neighbor speaks Russian. |
| a1-friendly | Our neighbours are friendly. | Our neighbors are friendly. |
| a1-kilometre | The pharmacy is a kilometre from the bakery. | The pharmacy is a kilometer from the bakery. |
| a1-theatre | Selin works at the theatre. | Selin works at the theater. |
| a1-dancer | Selin's mum is a dancer. | Selin's mom is a dancer. |
| a1-ill | My neighbour is ill. | My neighbor is ill. |
| a1-number | Mert dialled the number. | Mert dialed the number. |
| a1-grey | The pigeon is grey. | The pigeon is gray. |
| a1-visit | We visit our neighbours on Sundays. | We visit our neighbors on Sundays. |
| a1-hat | My hat is woollen. | My hat is woolen. |
| a1-colour | Ali likes this colour. | Ali likes this color. |
| a1-old | My neighbour is old but strong. | My neighbor is old but strong. |
| a1-man | That man is my neighbour. | That man is my neighbor. |
| a1-big | He has a big moustache. | He has a big mustache. |
| a1-cake | Mum made a cake for the guests. | Mom made a cake for the guests. |
| a1-kitchen | Mum is cooking in the kitchen. | Mom is cooking in the kitchen. |
| a1-january | Mum arrived in January. | Mom arrived in January. |
| a2-cheque | He paid by cheque. | He paid by check. |
| a2-jewellery | She keeps her jewellery in a box. | She keeps her jewelry in a box. |
| a2-subject | Maths is my favourite subject. | Math is my favorite subject. |
| a2-neighbourhood | This is a quiet neighbourhood. | This is a quiet neighborhood. |
| a2-sweatshirt | He wears a grey sweatshirt. | He wears a gray sweatshirt. |
| a2-organise | I will organise the party. | I will organize the party. |
| a2-activity | Swimming is my favourite activity. | Swimming is my favorite activity. |
| a2-chemistry | Chemistry is my favourite lesson. | Chemistry is my favorite lesson. |
| a2-practise | I practise the piano every day. | I practice the piano every day. |
| a2-distance | The distance to the airport is ten kilometres. | The distance to the airport is ten kilometers. |
| a2-mark | She got a high mark in maths. | She got a high mark in math. |
| a2-moustache | My grandfather has a white moustache. | My grandfather has a white mustache. |
| a2-programme | My favourite TV programme starts at eight. | My favorite TV program starts at eight. |
| a2-writer | My favourite writer lives in London. | My favorite writer lives in London. |
| a2-season | Summer is my favourite season. | Summer is my favorite season. |
| a2-song | This is my favourite song. | This is my favorite song. |
| a2-towards | The boy ran towards the sea. | The boy ran toward the sea. |
| a2-tyre | The car has a flat tyre. | The car has a flat tire. |
| a2-traveller | The traveller carries a big bag. | The traveler carries a big bag. |
| a2-neighbour | Our neighbour is very kind. | Our neighbor is very kind. |
| b1-apologise | I must apologise for being late. | I must apologize for being late. |
| b1-behaviour | His behaviour in class is very good. | His behavior in class is very good. |
| b1-character | My favourite character in the film is the old man. | My favorite character in the film is the old man. |
| b1-criticise | Don't criticise her; she is trying. | Don't criticize her; she is trying. |
| b1-flavour | Which flavour of ice cream do you want? | Which flavor of ice cream do you want? |
| b1-humour | I love his dry humour. | I love his dry humor. |
| b1-licence | You need a licence to drive a car. | You need a license to drive a car. |
| b1-organisation | She works for a big organisation. | She works for a big organization. |
| b1-protest | There was a large protest in the city centre. | There was a large protest in the city center. |
| b1-recognise | I did not recognise him with a beard. | I did not recognize him with a beard. |
| b1-series | My favourite series starts again tonight. | My favorite series starts again tonight. |
| b1-enquire | Please enquire at the front desk. | Please inquire at the front desk. |
| b1-pyjamas | She sleeps in blue pyjamas. | She sleeps in blue pajamas. |
| b1-harbour | The boats are in the harbour. | The boats are in the harbor. |
| b1-select | Please select your favourite colour. | Please select your favorite color. |
| b1-tutor | She has a tutor for maths. | She has a tutor for math. |
| b2-analyse | Scientists analyse the data every week. | Scientists analyze the data every week. |
| b2-artificial | The cake has no artificial colours. | The cake has no artificial colors. |
| b2-dispute | The neighbours had a dispute about the fence. | The neighbors had a dispute about the fence. |
| b2-fulfil | She worked hard to fulfil her dream. | She worked hard to fulfill her dream. |
| b2-get-along | Do you get along with your neighbours? | Do you get along with your neighbors? |
| b2-irritate | Loud music can irritate the neighbours. | Loud music can irritate the neighbors. |
| b2-rule-of-thumb | As a rule of thumb, drink two litres of water. | As a rule of thumb, drink two liters of water. |
| b2-enquiry | Send your enquiry by email. | Send your inquiry by email. |
| b2-adventurous | He is an adventurous traveller. | He is an adventurous traveler. |
| b2-harmony | The neighbours live in harmony. | The neighbors live in harmony. |
| b2-undermine | Rumours can undermine trust. | Rumors can undermine trust. |
| c1-allege | Neighbours allege that the factory pollutes the river. | Neighbors allege that the factory pollutes the river. |
| c1-artefact | The museum shows an artefact from ancient Rome. | The museum shows an artifact from ancient Rome. |
| c1-jeopardise | Lying could jeopardise your career. | Lying could jeopardize your career. |
| c1-justification | There is no justification for such behaviour. | There is no justification for such behavior. |
| c1-charitable | She gave money to a charitable organisation. | She gave money to a charitable organization. |
| c1-endeavour | The team's endeavour was finally rewarded. | The team's endeavor was finally rewarded. |
| c1-enrol | You can enrol online before September. | You can enroll online before September. |
| c1-exploit | Some firms exploit cheap labour. | Some firms exploit cheap labor. |
| c1-mobilise | The party tried to mobilise young voters. | The party tried to mobilize young voters. |
| c1-oblige | The law may oblige drivers to carry a licence. | The law may oblige drivers to carry a license. |
| c1-optimise | We can optimise the schedule to save time. | We can optimize the schedule to save time. |
| c1-penalise | Schools should not penalise honest mistakes. | Schools should not penalize honest mistakes. |
| c1-polarise | The issue could polarise the nation. | The issue could polarize the nation. |
| c1-predominant | Red is the predominant colour in the room. | Red is the predominant color in the room. |
| c1-prosperous | The town became a prosperous trading centre. | The town became a prosperous trading center. |
| c1-reside | Many students reside in the city centre. | Many students reside in the city center. |
| c1-synthetic | The jacket is made of synthetic fibre. | The jacket is made of synthetic fiber. |
| c1-whimsical | She has a whimsical sense of humour. | She has a whimsical sense of humor. |
| nat-candour | I appreciate your candour. | I appreciate your candor. |
| nat-cantankerous | Our cantankerous neighbour complains about everything. | Our cantankerous neighbor complains about everything. |
| nat-clamour | The clamour for reform grew louder. | The clamor for reform grew louder. |
| nat-demeanour | His calm demeanour hid his fear. | His calm demeanor hid his fear. |
| nat-dishevelled | He arrived late and dishevelled. | He arrived late and disheveled. |
| nat-enigma | My new neighbour remains an enigma to me. | My new neighbor remains an enigma to me. |
| nat-gruelling | After a gruelling match, the players collapsed. | After a grueling match, the players collapsed. |
| nat-heresy | In this office, criticising the boss is heresy. | In this office, criticizing the boss is heresy. |
| nat-irreverent | He has an irreverent sense of humour. | He has an irreverent sense of humor. |
| nat-subjugate | The empire tried to subjugate its neighbours. | The empire tried to subjugate its neighbors. |
| nat-lacklustre | The team gave a lacklustre performance. | The team gave a lackluster performance. |
| nat-meagre | He lives on a meagre pension. | He lives on a meager pension. |
| nat-chequered | He has a chequered past. | He has a checkered past. |
| nat-exquisite | She has exquisite taste in jewellery. | She has exquisite taste in jewelry. |
| nat-rancour | There was no rancour between the old rivals. | There was no rancor between the old rivals. |
| nat-sombre | The funeral was a sombre occasion. | The funeral was a somber occasion. |
| nat-uncouth | His uncouth behaviour embarrassed us. | His uncouth behavior embarrassed us. |
| nat-wry | He has a wry sense of humour. | He has a wry sense of humor. |

## Not changed (out of scope: `content/exam-packs/`)

British spellings remain in exam-pack cards (only `content/cards/**` was editable). Run this mapping over `content/exam-packs/cards` as a follow-up:

- a2-blog: favourite->favorite (example_en)
- a2-chore: favourite->favorite (example_en)
- a2-cookbook: mum->mom (example_en)
- a2-meatball: favourite->favorite (example_en)
- a2-mint: mum->mom (example_en)
- a2-mixer: mum->mom (example_en)
- a2-monument: centre->center (example_en)
- a2-parachute: metres->meters (example_en)
- a2-surfing: favourite->favorite (example_en)
- b2-amongst: amongst->among (answer), amongst->among (example_en)
- b2-archaeology: archaeology->archeology (answer), archaeology->archeology (example_en)
- b2-behavioural: behavioural->behavioral (answer), behavioural->behavioral (example_en)
- b2-diameter: centimetres->centimeters (example_en)
- b2-digestion: fibre->fiber (example_en)
- b2-due-to: cancelled->canceled (example_en)
- b2-gene: colour->color (example_en)
- b2-hereditary: colour->color (example_en)
- b2-in-favour-of: favour->favor (answer), favour->favor (example_en)
- b2-labour: labour->labor (answer), labour->labor (example_en)
- b2-prism: colours->colors (example_en)
- b2-radius: centimetres->centimeters (example_en)
- b2-sulphur: sulphur->sulfur (answer), sulphur->sulfur (example_en)
- b2-tumour: tumour->tumor (answer), tumour->tumor (example_en)
