# CLUE-A2-1 review (65 clues, 34 new proposals, 31 keep)

Source: `docs/content-review/CLUE-A2-1.csv` (edit the CSV, not this file). Scan: `tools/content-qa/clue_scan.py`; standard: `docs/CONTENT.md` "Clue standard (TR)".
Rule hit: C1 single word, C2 more than 8 words, C3 no clear head noun, C4 clue/example mismatch (C3/C4 are advisory; the 50 worst per level are listed). `keep` = the scanner was wrong, reason in the last column.
Approve with: `hepsi onay, hariç: <id> → <clue>`.


## A

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 1 | a2-amount | bir şeyin toplam miktarı (sayılamayan) | Add a small amount of salt. | C3 | bir şeyin ne kadar olduğunu gösteren ölçü |
| 2 | a2-artist | resim ya da heykel gibi sanat eseri yapan kişi | The artist painted a blue horse. | C2 | resim ya da heykel yapan kişi |

## B

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 3 | a2-bell | çalınca ses çıkaran metal alet, çan | The bell rings at nine every morning. | C3 | **keep** (clear head noun "çan"; hit is the -an ending) |
| 4 | a2-bone | vücudun iskeletini oluşturan sert parça | The dog has a bone. | C3 | iskeleti oluşturan sert doku |
| 5 | a2-brake | aracı durdurmaya yarayan fren | He pressed the brake when the light turned red. | C3 | **keep** (clear head noun; hit is the -an ending) |
| 6 | a2-burger | iki ekmek arasında köfteli yemek | Mert eats a burger for lunch. | C3 | **keep** ("yemek" is the noun here, not a verb) |
| 7 | a2-button | giysiyi ilikleyen küçük yuvarlak parça | A button fell off my coat. | C3 | giysiyi kapatan küçük yuvarlak tutturucu |

## C

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 8 | a2-countryside | şehir dışındaki kırsal alan | We live in the countryside. | C3 | **keep** ("alan" is a noun here (area); hit is the -an ending) |
| 9 | a2-curry | Hint mutfağında baharatlı soslu yemek | Mert loves hot curry. | C3 | **keep** ("yemek" is the noun here, not a verb) |

## D

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 10 | a2-danger | can ya da mal kaybı riski olan durum | The red light means danger. | C3 | insana zarar gelme olasılığı |
| 11 | a2-deer | dallı boynuzları olan, ormanda yaşayan hayvan | We saw a deer in the forest. | C3 | **keep** (head noun "hayvan"; hit is the -an ending) |
| 12 | a2-design | bir şeyin nasıl görüneceğini gösteren çizim ya da plan | I like the design of this bag. | C2+C3 | bir şeyin görünüşünü belirleyen çizim |
| 13 | a2-dinosaur | milyonlarca yıl önce yaşamış dev sürüngen | The film has a big dinosaur. | C3 | **keep** (head noun "sürüngen"; hit is the -en ending) |
| 14 | a2-disco | insanların dans edip müzik dinlediği yer | They danced all night at the disco. | C3 | gece dans edilen eğlence yeri |
| 15 | a2-dive | suya başı önde atlamak ya da suyun altına dalmak | He likes to dive into the sea. | C2 | suyun içine başı önde atlamak |

## E

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 16 | a2-enemy | karşı tarafta savaşılan kişi, düşman | The soldiers fought the enemy. | C3 | **keep** (clear head noun "düşman"; hit is the -an ending) |
| 17 | a2-equipment | bir iş ya da spor için gereken araç gereç | The gym has new equipment. | C2 | bir iş için gereken araç gereç |
| 18 | a2-eraser | kurşun kalem izini silen küçük nesne | The pencil has a small eraser on the end. | C3 | kalem izini silen küçük lastik |
| 19 | a2-escalator | alışveriş merkezlerindeki yürüyen merdiven | Take the escalator to the second floor. | C3 | **keep** (clear head noun "merdiven"; hit is the -en ending) |

## F

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 20 | a2-field | ekin ya da çimen ekili geniş açık alan | The cows are in the field. | C3 | **keep** ("alan" is a noun here (area); hit is the -an ending) |
| 21 | a2-flu | ateş ve öksürükle gelen bulaşıcı hastalık, grip | I have the flu. | C3 | **keep** (clear head noun "hastalık"; hit is the -ip ending of "grip") |
| 22 | a2-fox | tilki | The fox runs very fast. | C1 | kızıl tüylü, kurnaz bilinen orman hayvanı |

## G

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 23 | a2-gallery | resim ve heykellerin sergilendiği yer | The gallery is free on Sundays. | C3 | resim ve heykellerin sergilendiği salon |
| 24 | a2-gardener | bahçe işleriyle uğraşan kişi, bahçıvan | The gardener waters the flowers. | C3 | **keep** (clear head noun; hit is the -an ending) |
| 25 | a2-gold | parlak sarı, değerli maden | The ring is made of gold. | C3 | **keep** (clear head noun "maden"; hit is the -en ending) |
| 26 | a2-group | bir arada olan insanlar ya da şeyler | A group of students is waiting. | C3 | birlikte duran ya da çalışan topluluk |
| 27 | a2-gum | ağızda çiğnenen tatlı şey | Don't chew gum in class. | C3 | ağızda çiğnenen, yutulmayan şekerleme |
| 28 | a2-guy | günlük dilde adam, tip | He is a really nice guy. | C3 | **keep** (colloquial synonyms "adam, tip" are clear noun heads) |

## H

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 29 | a2-handsome | yakışıklı | My uncle is tall and very handsome. | C1 | yüzü çekici ve hoş görünen |
| 30 | a2-hero | cesaretiyle örnek olan kişi, kahraman | The pilot is a real hero. | C3 | **keep** (clear head noun "kahraman"; hit is the -an ending) |

## I

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 31 | a2-invitation | birini bir yere çağıran yazılı ya da sözlü davet | I got an invitation to her party. | C2 | birini bir etkinliğe çağıran mesaj |

## J

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 32 | a2-jungle | sıcak ülkelerdeki sık ve yoğun orman | Monkeys live in the jungle. | C3 | **keep** (clear head noun "orman"; hit is the -an ending) |

## L

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 33 | a2-lazy | tembel | Don't be lazy. Do your homework. | C1 | çalışmak ya da iş yapmak istemeyen |
| 34 | a2-lifeguard | plajda yüzenleri koruyan görevli, cankurtaran | The lifeguard saved the little boy. | C3 | **keep** (clear head noun; hit is the -an ending) |
| 35 | a2-lizard | kuru derili, dört bacaklı küçük sürüngen | A lizard sat on the warm wall. | C3 | **keep** (head noun "sürüngen"; hit is the -en ending) |

## M

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 36 | a2-mark | okulda bir sınava verilen puan | She got a high mark in math. | C3 | **keep** ("puan" is a noun here; hit is the -an ending) |
| 37 | a2-metal | demir ve bakır gibi sert madde | The key is made of metal. | C3 | demir ve bakır gibi sert, parlak malzeme |
| 38 | a2-moment | çok kısa bir zaman dilimi, an | Please wait a moment, I will be right back. | C3 | **keep** ("an" (instant) is the exact head noun) |

## N

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 39 | a2-nationality | bir kişinin hangi ülkenin vatandaşı olduğu | The man asked about her name and nationality. | C3 | bir kişinin hangi ülkeden olduğu bilgisi |

## O

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 40 | a2-olive | zeytinyağı da veren küçük yeşil ya da siyah meyve | He put an olive on the pizza. | C2 | yeşil ya da siyah küçük yağlı meyve |

## P

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 41 | a2-parking | aracı bir yere bırakma ya da bırakılan yer | There is free parking behind the shop. | C3 | araçların bırakılması için ayrılmış bölge |
| 42 | a2-pass | bir sınavı başarıyla geçmek ya da bir şeyi uzatmak | I want to pass the exam. | C2 | bir sınavı başarıyla geçmek |
| 43 | a2-pirate | denizde gemileri soyan haydut, korsan | The pirate has a wooden leg. | C3 | **keep** (clear head noun; hit is the -an ending) |
| 44 | a2-project | üzerinde bir süre çalışılan ödev ya da plan | We have a history project this week. | C3 | **keep** ("plan" is a noun here; hit is the -an ending) |
| 45 | a2-pump | su veya havayı basan alet | Ali uses a pump for the bike tyre. | C3 | su veya havayı basan makine |

## Q

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 46 | a2-quite | oldukça | The story is quite good. | C1 | fazla olmasa da epeyce |

## R

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 47 | a2-reach | bir yere varmak ya da uzanıp bir şeye ulaşmak | We reach the village at noon. | C2 | gidilen bir yere varmak |
| 48 | a2-record | müzik ya da konuşmanın kaydedildiği plak ya da dosya | I bought a record of old songs. | C2 | müziğin kaydedildiği plak |

## S

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 49 | a2-sand | plajda bulunan ince taneli madde | The children play in the sand. | C3 | çölde ve plajda bulunan ince taneler |
| 50 | a2-shovel | toprağı kazmak için kullanılan geniş uçlu alet | Dad digs with a shovel. | C3 | **keep** ("alet" is the right head here, qualifiers are specific) |
| 51 | a2-skyscraper | çok katlı çok yüksek bina, gökdelen | I work in a tall skyscraper. | C3 | **keep** (clear head noun "bina"; hit is the -en ending) |
| 52 | a2-slice | ekmek ya da pastadan kesilen ince parça | Would you like a slice of cake? | C3 | pastadan ya da ekmekten kesilen ince bölüm |
| 53 | a2-space | dünyanın dışındaki sonsuz boşluk ya da boş yer | There is no space in my bag. | C3 | içine bir şey sığabilen boşluk |
| 54 | a2-squid | kalamar, denizde yaşayan yumuşakça hayvan | We eat fried squid by the sea. | C3 | **keep** (head noun "hayvan"; hit is the -an ending) |

## T

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 55 | a2-takeaway | dışarı alınıp evde yenen yemek | Let's get a takeaway tonight. | C3 | **keep** ("yemek" is the noun here, not a verb) |
| 56 | a2-taste | yemeğin tadına bakmak ya da belli bir tadı olmak | Taste the soup before you add salt. | C2 | yemeğin tadına bakmak |
| 57 | a2-telescope | uzaktaki yıldızları yakın gösteren alet | We looked at the stars with a telescope. | C3 | uzaktaki yıldızları yakına getiren gözlem aracı |
| 58 | a2-tool | çekiç gibi iş yapmaya yarayan alet | A spade is a tool. | C3 | çekiç ya da tornavida gibi iş gereci |
| 59 | a2-turtle | sırtında sert kabuk taşıyan yavaş hayvan | A turtle walks on the beach. | C3 | **keep** (head noun "hayvan"; hit is the -an ending) |

## U

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 60 | a2-underground | şehirde yer altından giden tren | I go to work by underground. | C3 | **keep** (clear head noun "tren"; hit is the -en ending) |

## W

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 61 | a2-weight | bir şeyin kaç kilo geldiği | What is the weight of this box? | C3 | terazide ölçülen kilo değeri |
| 62 | a2-wish | birine iyi bir şey dilemek ya da dilek tutmak | I wish you a happy birthday. | C2 | birine iyi bir şey dilemek |
| 63 | a2-wolf | ormanda sürü halinde yaşayan vahşi hayvan | The wolf howled at the moon. | C3 | **keep** (head noun "hayvan"; hit is the -an ending) |

## Y

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 64 | a2-yard | evin yanındaki taş döşeli açık alan | Our yard is very small. | C3 | **keep** ("alan" is a noun here (area); hit is the -an ending) |

## Z

| # | id | current | example | rule | proposed |
|---|---|---|---|---|---|
| 65 | a2-zebra-crossing | yayaların yolu geçtiği çizgili alan | Wait at the zebra crossing. | C3 | **keep** ("alan" is a noun here (area); hit is the -an ending) |
