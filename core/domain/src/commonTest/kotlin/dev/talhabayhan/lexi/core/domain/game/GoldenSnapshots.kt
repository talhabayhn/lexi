package dev.talhabayhan.lexi.core.domain.game

// Golden snapshots for GoldenTest. Change only deliberately (see GoldenTest KDoc); review the JSON diff.

/** Final state of `GoldenTest.CLASSIC_EVENTS` (seed 2024). */
internal const val GOLDEN_CLASSIC: String = """{
    "seed": 2024,
    "mode": "CLASSIC",
    "level": "A1",
    "wheel": {
        "slots": [
            {
                "letter": "A",
                "card": {
                    "id": "a1-apple",
                    "level": "A1",
                    "category": "food-drink",
                    "answer": "APPLE",
                    "letter": "A",
                    "rule": "STARTS",
                    "partOfSpeech": "NOUN",
                    "clue": "ipucu",
                    "example": "An example with APPLE.",
                    "exampleTranslation": "Bir örnek.",
                    "difficulty": 1
                },
                "state": "CORRECT"
            },
            {
                "letter": "B",
                "card": {
                    "id": "a1-bread",
                    "level": "A1",
                    "category": "food-drink",
                    "answer": "BREAD",
                    "letter": "B",
                    "rule": "STARTS",
                    "partOfSpeech": "NOUN",
                    "clue": "ipucu",
                    "example": "An example with BREAD.",
                    "exampleTranslation": "Bir örnek.",
                    "difficulty": 1
                },
                "state": "PASSED"
            },
            {
                "letter": "C",
                "card": {
                    "id": "a1-cat",
                    "level": "A1",
                    "category": "food-drink",
                    "answer": "CAT",
                    "letter": "C",
                    "rule": "STARTS",
                    "partOfSpeech": "NOUN",
                    "clue": "ipucu",
                    "example": "An example with CAT.",
                    "exampleTranslation": "Bir örnek.",
                    "difficulty": 1
                },
                "state": "WRONG"
            },
            {
                "letter": "D",
                "card": {
                    "id": "a1-dog",
                    "level": "A1",
                    "category": "food-drink",
                    "answer": "DOG",
                    "letter": "D",
                    "rule": "STARTS",
                    "partOfSpeech": "NOUN",
                    "clue": "ipucu",
                    "example": "An example with DOG.",
                    "exampleTranslation": "Bir örnek.",
                    "difficulty": 1
                },
                "state": "CORRECT"
            },
            {
                "letter": "E",
                "card": {
                    "id": "a1-egg",
                    "level": "A1",
                    "category": "food-drink",
                    "answer": "EGG",
                    "letter": "E",
                    "rule": "STARTS",
                    "partOfSpeech": "NOUN",
                    "clue": "ipucu",
                    "example": "An example with EGG.",
                    "exampleTranslation": "Bir örnek.",
                    "difficulty": 1
                },
                "state": "CORRECT"
            },
            {
                "letter": "F",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "G",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "H",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "I",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "J",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "K",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "L",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "M",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "N",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "O",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "P",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "Q",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "R",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "S",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "T",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "U",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "V",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "W",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "X",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "Y",
                "card": null,
                "state": "SKIPPED"
            },
            {
                "letter": "Z",
                "card": null,
                "state": "SKIPPED"
            }
        ]
    },
    "jokers": {
        "counts": {
            "REVEAL_LETTER": 1,
            "REMOVE_DISTRACTORS": 0,
            "EXAMPLE": 2,
            "LISTEN": 1
        }
    },
    "status": "FINISHED",
    "startedAtMs": 1000,
    "lap": 3,
    "inputs": {
        "0": {
            "keys": [
                {
                    "letter": "P",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "R",
                    "isDistractor": true
                },
                {
                    "letter": "O",
                    "isDistractor": true
                },
                {
                    "letter": "V",
                    "isDistractor": true
                },
                {
                    "letter": "D",
                    "isDistractor": true
                },
                {
                    "letter": "E",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "A",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "B",
                    "isDistractor": true
                },
                {
                    "letter": "P",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "L",
                    "isDistractor": false,
                    "state": "USED"
                }
            ],
            "cells": [
                {
                    "letter": "A",
                    "keyIndex": 6,
                    "revealed": false
                },
                {
                    "letter": "P",
                    "keyIndex": 0,
                    "revealed": false
                },
                {
                    "letter": "P",
                    "keyIndex": 8,
                    "revealed": true
                },
                {
                    "letter": "L",
                    "keyIndex": 9,
                    "revealed": false
                },
                {
                    "letter": "E",
                    "keyIndex": 5,
                    "revealed": false
                }
            ],
            "placementOrder": [
                0,
                1,
                3,
                4
            ],
            "jokersUsed": [
                "REVEAL_LETTER"
            ]
        },
        "1": {
            "keys": [
                {
                    "letter": "A",
                    "isDistractor": false
                },
                {
                    "letter": "U",
                    "isDistractor": true,
                    "state": "DISABLED"
                },
                {
                    "letter": "B",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "L",
                    "isDistractor": true,
                    "state": "DISABLED"
                },
                {
                    "letter": "M",
                    "isDistractor": true,
                    "state": "DISABLED"
                },
                {
                    "letter": "D",
                    "isDistractor": false
                },
                {
                    "letter": "R",
                    "isDistractor": false
                },
                {
                    "letter": "E",
                    "isDistractor": false
                },
                {
                    "letter": "T",
                    "isDistractor": true,
                    "state": "DISABLED"
                },
                {
                    "letter": "N",
                    "isDistractor": true,
                    "state": "DISABLED"
                }
            ],
            "cells": [
                {
                    "letter": "B",
                    "keyIndex": 2,
                    "revealed": false
                },
                null,
                null,
                null,
                null
            ],
            "placementOrder": [
                0
            ],
            "jokersUsed": [
                "REMOVE_DISTRACTORS"
            ],
            "wasPassed": true
        },
        "2": {
            "keys": [
                {
                    "letter": "A",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "K",
                    "isDistractor": true
                },
                {
                    "letter": "C",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "O",
                    "isDistractor": true
                },
                {
                    "letter": "E",
                    "isDistractor": true
                },
                {
                    "letter": "N",
                    "isDistractor": true
                },
                {
                    "letter": "Y",
                    "isDistractor": true
                },
                {
                    "letter": "T",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "R",
                    "isDistractor": true
                },
                {
                    "letter": "P",
                    "isDistractor": true
                }
            ],
            "cells": [
                {
                    "letter": "A",
                    "keyIndex": 0,
                    "revealed": false
                },
                {
                    "letter": "T",
                    "keyIndex": 7,
                    "revealed": false
                },
                {
                    "letter": "C",
                    "keyIndex": 2,
                    "revealed": false
                }
            ],
            "placementOrder": [
                0,
                1,
                2
            ]
        },
        "3": {
            "keys": [
                {
                    "letter": "F",
                    "isDistractor": true
                },
                {
                    "letter": "U",
                    "isDistractor": true
                },
                {
                    "letter": "O",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "E",
                    "isDistractor": true
                },
                {
                    "letter": "W",
                    "isDistractor": true
                },
                {
                    "letter": "P",
                    "isDistractor": true
                },
                {
                    "letter": "G",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "D",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "H",
                    "isDistractor": true
                },
                {
                    "letter": "A",
                    "isDistractor": true
                }
            ],
            "cells": [
                {
                    "letter": "D",
                    "keyIndex": 7,
                    "revealed": false
                },
                {
                    "letter": "O",
                    "keyIndex": 2,
                    "revealed": false
                },
                {
                    "letter": "G",
                    "keyIndex": 6,
                    "revealed": false
                }
            ],
            "placementOrder": [
                0,
                1,
                2
            ],
            "jokersUsed": [
                "EXAMPLE"
            ]
        },
        "4": {
            "keys": [
                {
                    "letter": "A",
                    "isDistractor": true
                },
                {
                    "letter": "W",
                    "isDistractor": true
                },
                {
                    "letter": "O",
                    "isDistractor": true
                },
                {
                    "letter": "U",
                    "isDistractor": true
                },
                {
                    "letter": "G",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "E",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "I",
                    "isDistractor": true
                },
                {
                    "letter": "S",
                    "isDistractor": true
                },
                {
                    "letter": "G",
                    "isDistractor": false,
                    "state": "USED"
                },
                {
                    "letter": "Y",
                    "isDistractor": true
                }
            ],
            "cells": [
                {
                    "letter": "E",
                    "keyIndex": 5,
                    "revealed": false
                },
                {
                    "letter": "G",
                    "keyIndex": 4,
                    "revealed": false
                },
                {
                    "letter": "G",
                    "keyIndex": 8,
                    "revealed": false
                }
            ],
            "placementOrder": [
                0,
                1,
                2
            ]
        }
    },
    "remainingMs": 0,
    "score": 27,
    "longestStreak": 2,
    "summary": {
        "reason": "TIME_UP",
        "score": 27,
        "timeBonus": 0,
        "completed": false,
        "correct": 3,
        "wrong": 1,
        "passed": 1,
        "skipped": 21,
        "unanswered": 0,
        "longestStreak": 2,
        "remainingMs": 0,
        "correctCards": [
            "a1-apple",
            "a1-dog",
            "a1-egg"
        ],
        "missedCards": [
            "a1-bread",
            "a1-cat"
        ]
    }
}"""

/** Final state of `GoldenTest.DUEL_EVENTS` (seed 77). */
internal const val GOLDEN_DUEL: String = """{
    "seed": 77,
    "players": [
        {
            "seed": -3449238610718420921,
            "mode": "DUEL",
            "level": "A1",
            "wheel": {
                "slots": [
                    {
                        "letter": "A",
                        "card": {
                            "id": "a1-arm",
                            "level": "A1",
                            "category": "food-drink",
                            "answer": "ARM",
                            "letter": "A",
                            "rule": "STARTS",
                            "partOfSpeech": "NOUN",
                            "clue": "ipucu",
                            "example": "An example with ARM.",
                            "exampleTranslation": "Bir örnek.",
                            "difficulty": 1
                        },
                        "state": "CORRECT"
                    },
                    {
                        "letter": "B",
                        "card": {
                            "id": "a1-bread",
                            "level": "A1",
                            "category": "food-drink",
                            "answer": "BREAD",
                            "letter": "B",
                            "rule": "STARTS",
                            "partOfSpeech": "NOUN",
                            "clue": "ipucu",
                            "example": "An example with BREAD.",
                            "exampleTranslation": "Bir örnek.",
                            "difficulty": 1
                        },
                        "state": "WRONG"
                    },
                    {
                        "letter": "C",
                        "card": {
                            "id": "a1-chair",
                            "level": "A1",
                            "category": "food-drink",
                            "answer": "CHAIR",
                            "letter": "C",
                            "rule": "STARTS",
                            "partOfSpeech": "NOUN",
                            "clue": "ipucu",
                            "example": "An example with CHAIR.",
                            "exampleTranslation": "Bir örnek.",
                            "difficulty": 1
                        },
                        "state": "CORRECT"
                    },
                    {
                        "letter": "D",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "E",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "F",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "G",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "H",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "I",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "J",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "K",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "L",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "M",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "N",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "O",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "P",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "Q",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "R",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "S",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "T",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "U",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "V",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "W",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "X",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "Y",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "Z",
                        "card": null,
                        "state": "SKIPPED"
                    }
                ]
            },
            "jokers": {},
            "status": "FINISHED",
            "startedAtMs": 1000,
            "inputs": {
                "0": {
                    "keys": [
                        {
                            "letter": "L",
                            "isDistractor": true
                        },
                        {
                            "letter": "R",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "H",
                            "isDistractor": true
                        },
                        {
                            "letter": "P",
                            "isDistractor": true
                        },
                        {
                            "letter": "T",
                            "isDistractor": true
                        },
                        {
                            "letter": "A",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "M",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "E",
                            "isDistractor": true
                        },
                        {
                            "letter": "I",
                            "isDistractor": true
                        },
                        {
                            "letter": "N",
                            "isDistractor": true
                        }
                    ],
                    "cells": [
                        {
                            "letter": "A",
                            "keyIndex": 5,
                            "revealed": false
                        },
                        {
                            "letter": "R",
                            "keyIndex": 1,
                            "revealed": false
                        },
                        {
                            "letter": "M",
                            "keyIndex": 6,
                            "revealed": false
                        }
                    ],
                    "placementOrder": [
                        0,
                        1,
                        2
                    ]
                },
                "1": {
                    "keys": [
                        {
                            "letter": "Y",
                            "isDistractor": true
                        },
                        {
                            "letter": "A",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "U",
                            "isDistractor": true
                        },
                        {
                            "letter": "R",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "T",
                            "isDistractor": true
                        },
                        {
                            "letter": "D",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "S",
                            "isDistractor": true
                        },
                        {
                            "letter": "I",
                            "isDistractor": true
                        },
                        {
                            "letter": "E",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "B",
                            "isDistractor": false,
                            "state": "USED"
                        }
                    ],
                    "cells": [
                        {
                            "letter": "R",
                            "keyIndex": 3,
                            "revealed": false
                        },
                        {
                            "letter": "B",
                            "keyIndex": 9,
                            "revealed": false
                        },
                        {
                            "letter": "E",
                            "keyIndex": 8,
                            "revealed": false
                        },
                        {
                            "letter": "A",
                            "keyIndex": 1,
                            "revealed": false
                        },
                        {
                            "letter": "D",
                            "keyIndex": 5,
                            "revealed": false
                        }
                    ],
                    "placementOrder": [
                        0,
                        1,
                        2,
                        3,
                        4
                    ]
                },
                "2": {
                    "keys": [
                        {
                            "letter": "N",
                            "isDistractor": true
                        },
                        {
                            "letter": "A",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "S",
                            "isDistractor": true
                        },
                        {
                            "letter": "C",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "H",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "T",
                            "isDistractor": true
                        },
                        {
                            "letter": "I",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "O",
                            "isDistractor": true
                        },
                        {
                            "letter": "E",
                            "isDistractor": true
                        },
                        {
                            "letter": "R",
                            "isDistractor": false,
                            "state": "USED"
                        }
                    ],
                    "cells": [
                        {
                            "letter": "C",
                            "keyIndex": 3,
                            "revealed": false
                        },
                        {
                            "letter": "H",
                            "keyIndex": 4,
                            "revealed": false
                        },
                        {
                            "letter": "A",
                            "keyIndex": 1,
                            "revealed": false
                        },
                        {
                            "letter": "I",
                            "keyIndex": 6,
                            "revealed": false
                        },
                        {
                            "letter": "R",
                            "keyIndex": 9,
                            "revealed": false
                        }
                    ],
                    "placementOrder": [
                        0,
                        1,
                        2,
                        3,
                        4
                    ]
                }
            },
            "remainingMs": 150000,
            "score": 20,
            "streak": 1,
            "longestStreak": 1,
            "summary": {
                "reason": "ALL_ANSWERED",
                "score": 170,
                "timeBonus": 150,
                "completed": true,
                "correct": 2,
                "wrong": 1,
                "passed": 0,
                "skipped": 23,
                "unanswered": 0,
                "longestStreak": 1,
                "remainingMs": 150000,
                "correctCards": [
                    "a1-arm",
                    "a1-chair"
                ],
                "missedCards": [
                    "a1-bread"
                ]
            }
        },
        {
            "seed": 8393769216844398008,
            "mode": "DUEL",
            "level": "A1",
            "wheel": {
                "slots": [
                    {
                        "letter": "A",
                        "card": {
                            "id": "a1-apple",
                            "level": "A1",
                            "category": "food-drink",
                            "answer": "APPLE",
                            "letter": "A",
                            "rule": "STARTS",
                            "partOfSpeech": "NOUN",
                            "clue": "ipucu",
                            "example": "An example with APPLE.",
                            "exampleTranslation": "Bir örnek.",
                            "difficulty": 1
                        },
                        "state": "CORRECT"
                    },
                    {
                        "letter": "B",
                        "card": {
                            "id": "a1-bread",
                            "level": "A1",
                            "category": "food-drink",
                            "answer": "BREAD",
                            "letter": "B",
                            "rule": "STARTS",
                            "partOfSpeech": "NOUN",
                            "clue": "ipucu",
                            "example": "An example with BREAD.",
                            "exampleTranslation": "Bir örnek.",
                            "difficulty": 1
                        },
                        "state": "WRONG"
                    },
                    {
                        "letter": "C",
                        "card": {
                            "id": "a1-chair",
                            "level": "A1",
                            "category": "food-drink",
                            "answer": "CHAIR",
                            "letter": "C",
                            "rule": "STARTS",
                            "partOfSpeech": "NOUN",
                            "clue": "ipucu",
                            "example": "An example with CHAIR.",
                            "exampleTranslation": "Bir örnek.",
                            "difficulty": 1
                        },
                        "state": "CORRECT"
                    },
                    {
                        "letter": "D",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "E",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "F",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "G",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "H",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "I",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "J",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "K",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "L",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "M",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "N",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "O",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "P",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "Q",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "R",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "S",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "T",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "U",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "V",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "W",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "X",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "Y",
                        "card": null,
                        "state": "SKIPPED"
                    },
                    {
                        "letter": "Z",
                        "card": null,
                        "state": "SKIPPED"
                    }
                ]
            },
            "jokers": {},
            "status": "FINISHED",
            "startedAtMs": 2000,
            "lap": 2,
            "inputs": {
                "0": {
                    "keys": [
                        {
                            "letter": "I",
                            "isDistractor": true
                        },
                        {
                            "letter": "E",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "A",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "H",
                            "isDistractor": true
                        },
                        {
                            "letter": "O",
                            "isDistractor": true
                        },
                        {
                            "letter": "P",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "C",
                            "isDistractor": true
                        },
                        {
                            "letter": "P",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "L",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "N",
                            "isDistractor": true
                        }
                    ],
                    "cells": [
                        {
                            "letter": "A",
                            "keyIndex": 2,
                            "revealed": false
                        },
                        {
                            "letter": "P",
                            "keyIndex": 5,
                            "revealed": false
                        },
                        {
                            "letter": "P",
                            "keyIndex": 7,
                            "revealed": false
                        },
                        {
                            "letter": "L",
                            "keyIndex": 8,
                            "revealed": false
                        },
                        {
                            "letter": "E",
                            "keyIndex": 1,
                            "revealed": false
                        }
                    ],
                    "placementOrder": [
                        0,
                        1,
                        2,
                        3,
                        4
                    ]
                },
                "1": {
                    "keys": [
                        {
                            "letter": "I",
                            "isDistractor": true
                        },
                        {
                            "letter": "E",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "O",
                            "isDistractor": true
                        },
                        {
                            "letter": "B",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "A",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "F",
                            "isDistractor": true
                        },
                        {
                            "letter": "N",
                            "isDistractor": true
                        },
                        {
                            "letter": "R",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "D",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "T",
                            "isDistractor": true
                        }
                    ],
                    "cells": [
                        {
                            "letter": "R",
                            "keyIndex": 7,
                            "revealed": false
                        },
                        {
                            "letter": "B",
                            "keyIndex": 3,
                            "revealed": false
                        },
                        {
                            "letter": "E",
                            "keyIndex": 1,
                            "revealed": false
                        },
                        {
                            "letter": "A",
                            "keyIndex": 4,
                            "revealed": false
                        },
                        {
                            "letter": "D",
                            "keyIndex": 8,
                            "revealed": false
                        }
                    ],
                    "placementOrder": [
                        0,
                        1,
                        2,
                        3,
                        4
                    ],
                    "wasPassed": true
                },
                "2": {
                    "keys": [
                        {
                            "letter": "O",
                            "isDistractor": true
                        },
                        {
                            "letter": "C",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "P",
                            "isDistractor": true
                        },
                        {
                            "letter": "R",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "N",
                            "isDistractor": true
                        },
                        {
                            "letter": "Y",
                            "isDistractor": true
                        },
                        {
                            "letter": "A",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "S",
                            "isDistractor": true
                        },
                        {
                            "letter": "H",
                            "isDistractor": false,
                            "state": "USED"
                        },
                        {
                            "letter": "I",
                            "isDistractor": false,
                            "state": "USED"
                        }
                    ],
                    "cells": [
                        {
                            "letter": "C",
                            "keyIndex": 1,
                            "revealed": false
                        },
                        {
                            "letter": "H",
                            "keyIndex": 8,
                            "revealed": false
                        },
                        {
                            "letter": "A",
                            "keyIndex": 6,
                            "revealed": false
                        },
                        {
                            "letter": "I",
                            "keyIndex": 9,
                            "revealed": false
                        },
                        {
                            "letter": "R",
                            "keyIndex": 3,
                            "revealed": false
                        }
                    ],
                    "placementOrder": [
                        0,
                        1,
                        2,
                        3,
                        4
                    ]
                }
            },
            "remainingMs": 147000,
            "score": 20,
            "longestStreak": 1,
            "summary": {
                "reason": "ALL_ANSWERED",
                "score": 167,
                "timeBonus": 147,
                "completed": true,
                "correct": 2,
                "wrong": 1,
                "passed": 0,
                "skipped": 23,
                "unanswered": 0,
                "longestStreak": 1,
                "remainingMs": 147000,
                "correctCards": [
                    "a1-apple",
                    "a1-chair"
                ],
                "missedCards": [
                    "a1-bread"
                ]
            }
        }
    ],
    "activePlayer": 1,
    "phase": "FINISHED",
    "result": {
        "reason": "ALL_FINISHED",
        "winner": 0
    }
}"""
