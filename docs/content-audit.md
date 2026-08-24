# Stage 7 quiz-content audit

## Scope and safeguards

This audit covers every row in the 50-question legacy quiz imported by
`V2__quiz_content.sql`. The preserved V2 migration and the historical SQL export were
not edited. Corrections are applied only by `V3__quiz_content_corrections.sql` to a
disposable or configured runtime database.

The root `quizdb.mv.db` and `database/backup/quizdb-original.mv.db` were never
connected to or opened through H2; they were read only by the required filesystem
hash check. Before Stage 7, each protected file had this SHA-256 hash:

`9051E8D48F6B3344F7BB866E70A90E980BFBC04C807E560997421CD50C6C2112`

## Complete 50-question disposition

| ID | Audit result | ID | Audit result | ID | Audit result | ID | Audit result |
|---:|---|---:|---|---:|---|---:|---|
| 1 | Clarified | 14 | No content issue | 27 | Corrected | 40 | Dated and scoped |
| 2 | Dated; required wording | 15 | No content issue | 28 | Fill fragments repaired | 41 | Metric clarified |
| 3 | Clarified | 16 | Answer and metric corrected | 29 | Choices repaired | 42 | Dated; answer corrected |
| 4 | Choices repaired | 17 | Terminology clarified | 30 | Fill fragments repaired | 43 | Choices repaired |
| 5 | Fill fragments repaired | 18 | Category clarified | 31 | Required wording, answer, choices | 44 | Choices repaired |
| 6 | Unresolved; unchanged | 19 | No content issue | 32 | Dated and scoped; choices repaired | 45 | Choices repaired |
| 7 | Choices repaired | 20 | Scope and choice corrected | 33 | No content issue | 46 | Metric clarified |
| 8 | Claim clarified; choices repaired | 21 | Scope clarified | 34 | Dated | 47 | Wording clarified |
| 9 | Claim corrected; choices repaired | 22 | Answer and fill fragments corrected | 35 | No content issue | 48 | Scope clarified; choices repaired |
| 10 | Choices repaired | 23 | Fill claim corrected | 36 | Ambiguity removed | 49 | Species and answer corrected |
| 11 | Fill fragments repaired | 24 | Fill claim corrected | 37 | Missing correct choice repaired | 50 | Dated and scoped |
| 12 | Wording and missing choice repaired | 25 | Fill claim dated and scoped | 38 | Choices repaired |  |  |
| 13 | No content issue | 26 | Fill claim dated and repaired | 39 | Choices repaired |  |  |

## Problems found and approved corrections

In the table below, “original wording” for a fill-in-the-blank question is its
rendered V2 fragments. A source marked “structural validation” supports a database
integrity correction rather than a factual claim, so no external factual source is
applicable.

| Question ID | Original wording | Original answer | Problem | Corrected wording | Corrected answer | Authoritative source URL | Reason for correction |
|---:|---|---|---|---|---|---|---|
| 1 | Which is the largest state in USA? | Alaska | “Largest” did not name a metric. | Which U.S. state has the largest total area? | Alaska | [U.S. Census Bureau](https://www.census.gov/about/history/stories/monthly/2024/january-2024.html) | Alaska is largest by total area; the metric makes the question unambiguous. |
| 2 | What country has the largest population? | China | Time-sensitive and undated. | As of 2021, which country was the world's most populous? | China | [United Nations World Population Prospects](https://population.un.org/wpp/assets/Files/WPP2022_Release-Note-rev1.pdf) | The required historical date preserves China as the correct answer and prevents the question from aging incorrectly. |
| 3 | Which one is the smallest country? | Vatican City | “Smallest” and “country” were imprecise. | Which is the world's smallest independent state by area? | Vatican City | [Vatican City State](https://www.vaticanstate.va/en/state-and-government/history/vatican-city-today.html) | The official description supports the independent-state and area qualification. |
| 5 | An ____ measures intelligence | IQ | The sentence incorrectly described an abbreviation as a measuring instrument. | The abbreviation for intelligence quotient is ____ . | IQ | [APA Dictionary of Psychology](https://dictionary.apa.org/iq) | The repaired fragments accurately ask for the abbreviation and remain usable by the fill-in UI. |
| 8 | Australia is wider than the moon | true | “Wider” did not identify either measurement. | Australia is wider from east to west than the Moon's diameter. | true | [Geoscience Australia](https://www.ga.gov.au/scientific-topics/national-location-information/dimensions/continental-extremities); [NASA](https://science.nasa.gov/moon/facts/) | The named dimensions make the comparison testable. |
| 9 | Coffee is made from berries | true | Coffee is not literally made from berries; the quiz was referring to the seed inside coffee fruit. | Coffee beans are seeds found inside coffee fruit. | true | [USDA APHIS](https://direct.aphis.usda.gov/traveling-with-ag-products/coffee-tea-honey-nuts-spices) | The correction uses accurate botanical/product terminology. |
| 11 | Do not use low-fat milk ____ it will the taste (affect vs effect) | affect | The fragments were ungrammatical and did not form a valid prompt. | Using low-fat milk will ____ the taste. (affect or effect) | affect | [Merriam-Webster](https://www.merriam-webster.com/dictionary/affect) | “Affect” is the verb meaning to influence; both fragments now form a complete sentence. |
| 12 | What is the tallest mountain in Canada | Mount Logan | “Highest” is the relevant elevation term, and the correct answer was absent from the choices. | What is the highest mountain in Canada? | Mount Logan | [Parks Canada](https://www.parks.canada.ca/pn-np/yt/kluane/nature/geomorph) | Parks Canada identifies Mount Logan as Canada's highest mountain; the Mount Everest distractor was replaced with Mount Logan. |
| 16 | Which is the largest freshwater lake in the world | Lake Michigan | The metric was unstated and the answer was incorrect under the normal surface-area interpretation. | Which is the world's largest freshwater lake by surface area? | Lake Superior | [U.S. Environmental Protection Agency](https://www.epa.gov/sites/default/files/2016-10/documents/lake_superior_lamp_2015-2019.pdf) | The explicit metric makes Lake Superior the supported, unique choice. |
| 17 | What is the scientific name of the knee cap | Patella | “Scientific name” was imprecise anatomical terminology. | What is the anatomical name for the kneecap? | Patella | [MedlinePlus](https://medlineplus.gov/ency/article/001070.htm) | The source identifies the kneecap as the patella. |
| 18 | What is the fastest land animal? | Cheetah | “Animal” was broad; the supported category is land mammal. | What is the world's fastest land mammal? | Cheetah | [Smithsonian's National Zoo](https://nationalzoo.si.edu/animals/cheetah) | The precise category matches the authoritative claim. |
| 20 | What is the oldest Disney film? | Snow White and the Seven Dwarfs | “Oldest Disney film” was overly broad, and the matching choice used “7” instead of the official title. | What was Walt Disney's first feature-length animated film? | Snow White and the Seven Dwarfs | [The Walt Disney Company](https://thewaltdisneycompany.com/news/film-archive-walt-disney-studios/) | The wording names the intended category and the choice now exactly matches the correct answer. |
| 21 | Which scientific unit is named after an Italian nobleman | Volt | “Scientific unit” and “nobleman” were vague. | Which SI derived unit is named after Italian scientist Alessandro Volta? | Volt | [NIST](https://www.nist.gov/pml/owm/si-units-electric-current) | NIST identifies the volt and its namesake; the revised wording is specific. |
| 22 | Joe Biden is the ____ president of the U.S | 47 | The ordinal was wrong and the fragments lacked the dated office context. | Joe Biden was the ____ president of the United States. | 46th | [U.S. National Archives](https://www.archives.gov/presidential-records/vice-presidential-records/selected-vice-presidential-records/biden-records) | The National Archives identifies Biden as the 46th president. |
| 23 | The ____ is smaller than proton | sun | The completed statement was false and ungrammatical. | The ____ is the star at the center of our solar system. | sun | [NASA](https://science.nasa.gov/sun/facts/) | The repaired statement is a stable, authoritative fact and preserves the answer. |
| 24 | A rain ____ is made up of water and dust | cloud | The fragments formed an inaccurate definition. | A ____ is made of water droplets or ice crystals. | cloud | [NOAA NESDIS](https://www.nesdis.noaa.gov/our-environment/clouds) | NOAA's definition supports the corrected prompt. |
| 25 | The country with the ____ GDP is U.S | highest | GDP ranking is time-sensitive and the measure was not stated. | In 2021, the country with the ____ GDP in current U.S. dollars was the United States. | highest | [World Bank](https://databankfiles.worldbank.org/public/ddpext_download/GDP.PDF) | Adding the year and nominal current-dollar measure makes the historical answer verifiable. |
| 26 | Prime Minister ____ is in charge of Canada | Justin Trudeau | The sentence was awkward, time-sensitive, and undated. | In 2021, Canada's prime minister was ____ . | Justin Trudeau | [Government of Canada](https://www.canada.ca/en/canadian-heritage/corporate/transparency/open-government/standing-committee/dm-transition-material-2021/ministers-letters-mandate.html) | The date fixes the claim to the quiz's historical period and the fragments now read naturally. |
| 27 | What is the legal age of drinking in the U.S? | 21 | “Drinking age” can vary by circumstance; the nationwide rule concerns purchase. | What is the minimum age to purchase alcohol in all 50 U.S. states? | 21 | [NHTSA](https://www.nhtsa.gov/book/countermeasures-that-work/alcohol-impaired-driving/countermeasures/legislation-and-licensing-5) | The new wording states the nationally consistent rule precisely. |
| 28 | The ____ seperates both hemispheres | Equator | Misspelling and ambiguous “both hemispheres.” | The ____ separates the Northern and Southern Hemispheres. | Equator | [NOAA Ocean Service](https://oceanservice.noaa.gov/facts/latitude.html) | The repaired sentence names the two hemispheres and corrects the spelling. |
| 30 | The founder of ____ is Bill Gates | Microsoft | The claim omitted co-founder Paul Allen and treated a two-founder history as singular. | Bill Gates and Paul Allen co-founded ____ . | Microsoft | [Microsoft](https://news.microsoft.com/announcement/microsoft-is-born/) | Microsoft's history names both founders. |
| 31 | Harvard University is located in which city | Boston | The requested Harvard Yard location was wrong, Cambridge appeared twice, and one answer ID was duplicated. | In which city is Harvard Yard located? | Cambridge | [Harvard University](https://seas.harvard.edu/tour/cambridge/1/harvard-yard) | The required correction uses four ordered, unique choices: Boston, Cambridge, New Haven, and New York City. |
| 32 | Atlanta has the busiest airport | true | Time-sensitive, with no year or traffic metric. | In 2021, Hartsfield-Jackson Atlanta International Airport was the world's busiest airport by passenger traffic. | true | [Hartsfield-Jackson Atlanta International Airport](https://www.atl.com/media-center/press-releases/read/?id=62549668c92fda0019f1c241) | The year, airport, worldwide scope, and passenger metric make the claim durable and verifiable. |
| 34 | What is the capital of Indonesia | Jakarta | The capital question is time-sensitive and undated. | As of 2021, what was the capital of Indonesia? | Jakarta | [Republic of Indonesia, Law No. 3 of 2022](https://ikn.go.id/storage/regulasi/law-number-3-2022-english.pdf) | The historical date avoids future capital-transition ambiguity. |
| 36 | The idea of Socialism was articulated and advanced by whom | Karl Marx | The broad historical claim was subjective and had no uniquely defensible answer. | Who co-authored The Communist Manifesto with Friedrich Engels? | Karl Marx | [Library of Congress](https://www.loc.gov/resource/gdcmassbookdig.manifestoofcommu00marx_1/?st=grid) | The replacement asks a specific authorship fact while preserving the supported answer. |
| 37 | Which country is the Taedong River in? | North Korea | The correct answer was missing from the choices; “South Korea” appeared instead. | Which country is the Taedong River in? | North Korea | [United Nations](https://www.un.org/sg/en/content/highlight/2005-04-21.html) | The incorrect distractor was replaced with the supported correct choice. |
| 40 | The Empire State Building is the tallest building | false | The claim omitted a location and date. | As of 2021, the Empire State Building was the tallest building in New York City. | false | [New York City Department of Buildings](https://www.nyc.gov/assets/buildings/pdf/singapore_presentation.pdf) | Scope and date turn the fragment into a clear historical false statement. |
| 41 | Asia is the largest continent | true | “Largest” did not name a metric. | Asia is the world's largest continent by land area. | true | [U.S. Geological Survey](https://store.usgs.gov/assets/MOD/StoreFiles/PDF/EAA/EAA2_Western_Asia.pdf) | The metric makes the claim precise. |
| 42 | Georgia FBLA chapter has the most members | false | No event/date/scope was supplied, and the legacy answer contradicted the official 2021 recognition. | At the 2021 National Leadership Conference, Georgia FBLA was recognized as the largest state chapter. | true | [Georgia FBLA](https://georgiafbla.org/nlc-award-review/) | The official organization reports the 2021 award; the answer is corrected from false to true. |
| 46 | What is the biggest planet in the solar system | Jupiter | “Biggest” did not name a measurement. | Which is the largest planet in the solar system by equatorial diameter? | Jupiter | [NASA](https://science.nasa.gov/solar-system/planets/planet-sizes-and-locations-in-our-solar-system/) | The named measurement removes ambiguity and preserves the supported answer. |
| 47 | What is the galaxy that we live in | Milky Way | Colloquial wording implied people inhabit a galaxy directly. | Which galaxy contains our solar system? | Milky Way | [NASA](https://science.nasa.gov/universe/galaxies/our-milky-way-galaxy/) | The corrected wording expresses the astronomical relationship precisely. |
| 48 | There is a black hole at the center of the galaxy | true | “The galaxy” and “a black hole” were underspecified. | A supermassive black hole called Sagittarius A* is at the center of the Milky Way. | true | [NASA](https://www.nasa.gov/missions/ixpe/milky-ways-central-black-hole-woke-up-200-years-ago-nasas-ixpe-finds/) | The revised claim identifies both the galaxy and black hole. |
| 49 | What is the tallest tree species | Redwoods | “Redwoods” was not a species name and could refer to more than one redwood species. | Which tree species includes the world's tallest living trees? | Coast redwood | [U.S. National Park Service](https://www.nps.gov/redw/learn/park-facts.htm) | The wording and answer now identify coast redwood specifically, and the choice exactly matches. |
| 50 | What country has the highest average height | Netherlands | Average height depends on year, study, and population group. | According to a 2016 Imperial College London study, men from which country were tallest on average in 2014? | Netherlands | [Imperial College London](https://www.imperial.ac.uk/news/173634/dutch-latvian-women-tallest-world-according/amp/) | The source, gender, and measurement year make the answer reproducible. |
| 4, 7–10, 29, 32, 38–45, 48 | Existing question wording | Existing true/false answer | Every true/false question had zero or one stored choices instead of exactly two. | Existing wording, plus any factual correction listed above | Existing answer, except Q42 corrected to true | `V2__quiz_content.sql` structural validation (no external factual source applicable) | V3 replaces the incomplete rows with ordered `true` and `false` choices for all 16 true/false questions. |
| 2, 3, 5, 23, 24, 27, 28, 30, 31, 35, 37, 38, 40, 42, 44, 45, 50 | Existing question wording | Existing answer | V2 reused answer IDs 54, 59, 63, 81, 88, 94, 118, and 121 across questions; Q31 also duplicated ID 69 within the question. | Existing wording, plus corrections listed above | Existing answer, plus corrections listed above | `V2__quiz_content.sql` structural validation (no external factual source applicable) | V3 rekeys every answer deterministically as `question ID × 10 + displayed position`, preserving order and producing 150 positive, globally unique IDs. |

## Unresolved item left unchanged

Question 6, “What is the rarest M&M color?” with answer “Brown,” remains unchanged.
The claim is common in secondary trivia sources, but the audit did not locate a
manufacturer-published color-frequency dataset or another sufficiently authoritative
primary source. The [official M&M's site](https://www.mms.com/en-us) does not provide
the evidence needed to approve a correction. No guess was made.

## Checksums and runtime invariants

The checksums are SHA-256 values over length-prefixed, pipe-separated database row
values in deterministic query order. They intentionally distinguish the immutable
historical import from the corrected runtime content.

| Dataset | Questions checksum | Answers checksum |
|---|---|---|
| Preserved legacy content after V1 + V2 | `216B45179E5AD80DFB6A3EF95DBA84C4D2B755A3E0E9BB2859976F18B167170B` | `02A648A1B6F840A73FC08CC8B0BBDAAB6A314CC1783E1873A483DE23B01664CB` |
| Corrected runtime content after V1 + V2 + V3 | `69728FED47DFC100F805D9A48C0017514A85ADCB8DB0201F9B4EB7E08FB053D2` | `0A9BE30CA16B40D53D533D8292F194CA36DC387B7777F9E5E3E8EB5DF6C94334` |

After V3, automated checks require exactly 50 questions with IDs 1–50 and exactly
150 answer rows. All required fields are nonblank; display types are valid; option
counts match their UI contracts; normalized choices are unique; each applicable
correct answer appears once; answer IDs are positive and globally unique; and no
answer is orphaned.

## Final verification

`mvnw.cmd clean verify` completed successfully with 50 tests, zero failures, zero
errors, and zero skipped tests. The build continued to use H2 2.4.240. The runnable
JAR contains V1, V2, and V3, and contains no `.mv.db`, trace, lock, or runtime-data
file.

The protected hashes were identical before and after Stage 7:

| Protected file | Before SHA-256 | After SHA-256 |
|---|---|---|
| `quizdb.mv.db` | `9051E8D48F6B3344F7BB866E70A90E980BFBC04C807E560997421CD50C6C2112` | `9051E8D48F6B3344F7BB866E70A90E980BFBC04C807E560997421CD50C6C2112` |
| `database/backup/quizdb-original.mv.db` | `9051E8D48F6B3344F7BB866E70A90E980BFBC04C807E560997421CD50C6C2112` | `9051E8D48F6B3344F7BB866E70A90E980BFBC04C807E560997421CD50C6C2112` |
