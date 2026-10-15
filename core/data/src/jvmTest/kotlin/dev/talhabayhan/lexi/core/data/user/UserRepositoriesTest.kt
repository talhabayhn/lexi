package dev.talhabayhan.lexi.core.data.user

import app.cash.turbine.test
import dev.talhabayhan.lexi.core.data.testing.ContentFixture.FixtureCard
import dev.talhabayhan.lexi.core.data.testing.TestDatabases
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.JokerInventory
import dev.talhabayhan.lexi.core.domain.game.JokerType
import dev.talhabayhan.lexi.core.domain.joker.JokerWallet
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.session.DailyResult
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.srs.CardProgress
import dev.talhabayhan.lexi.core.domain.stats.SessionRecord
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserRepositoriesTest {
    private val io = UnconfinedTestDispatcher()
    private val db =
        TestDatabases(io, cards = listOf(FixtureCard("a1-apple", "APPLE"), FixtureCard("a1-bread", "BREAD")))

    @AfterTest
    fun close() = db.close()

    private val apple =
        CardProgress(
            CardId("a1-apple"),
            box = 1,
            dueEpochDay = 20_001,
            seen = 2,
            correct = 1,
            wrong = 1,
            lastSeenMs = 1_000,
        )
    private val bread = CardProgress(CardId("a1-bread"), seen = 1, correct = 1, lastSeenMs = 2_000)

    private fun session(
        mode: GameMode = GameMode.CLASSIC,
        day: Long = 20_000,
        completed: Boolean = true,
        correct: Int = 20,
    ) = SessionResult(
        mode = mode,
        level = Level.A1,
        category = CategoryId("food"),
        seed = 42,
        startedAtMs = 1_000,
        endedAtMs = 181_000,
        epochDay = day,
        score = 250,
        correct = correct,
        wrong = 3,
        passed = 3,
        completed = completed,
    )

    // Progress

    @Test
    fun `progress round trips every field and replaces on save`() = runTest(io) {
        val progress = ProgressRepositoryImpl(db.databases, io)
        val mastered = apple.copy(box = 4, dueEpochDay = null, mastered = true)

        progress.save(listOf(apple, bread))
        progress.save(listOf(mastered))

        assertEquals(
            mapOf(mastered.cardId to mastered, bread.cardId to bread),
            progress.get(listOf(apple.cardId, bread.cardId)),
        )
        assertEquals(emptyMap(), progress.get(listOf(CardId("a1-none"))))
    }

    @Test
    fun `observeAll emits on every save`() = runTest(io) {
        val progress = ProgressRepositoryImpl(db.databases, io)

        progress.observeAll().test {
            assertEquals(emptyList(), awaitItem())
            progress.save(listOf(apple))
            assertEquals(listOf(apple), awaitItem())
            progress.save(listOf(bread))
            assertEquals(listOf(apple, bread), awaitItem())
        }
    }

    @Test
    fun `progress of cards that left content is kept but hidden`() = runTest(io) {
        val progress = ProgressRepositoryImpl(db.databases, io)
        val orphan =
            CardProgress(CardId("a1-removed"), box = 0, dueEpochDay = 1, seen = 1, wrong = 1, lastSeenMs = 3_000)

        progress.save(listOf(apple, orphan))

        progress.observeAll().test { assertEquals(listOf(apple), awaitItem()) }
        assertEquals(mapOf(apple.cardId to 1_000L), progress.lastSeenMs())
        assertEquals(orphan, progress.get(listOf(orphan.cardId))[orphan.cardId], "the row itself is not deleted")
    }

    // Sessions and streak

    @Test
    fun `sessions are recorded for stats and completed ones mark the streak day`() = runTest(io) {
        val sessions = SessionRepositoryImpl(db.databases, io, GameConfig())
        val streak = StreakRepositoryImpl(db.databases, io)

        assertTrue(sessions.record(session(day = 20_000, completed = true, correct = 20)))
        assertTrue(sessions.record(session(day = 20_001, completed = false, correct = 7)))
        assertTrue(sessions.record(session(day = 20_000, completed = true, correct = 22)))

        sessions.observeRecords().test {
            assertEquals(
                listOf(
                    SessionRecord(20_000, true, 20),
                    SessionRecord(20_001, false, 7),
                    SessionRecord(20_000, true, 22),
                ),
                awaitItem(),
            )
        }
        streak.observeDays().test { assertEquals(listOf(20_000L), awaitItem()) }
    }

    @Test
    fun `hot-seat duel games are not recorded`() = runTest(io) {
        val sessions = SessionRepositoryImpl(db.databases, io, GameConfig())

        assertFalse(sessions.record(session(mode = GameMode.DUEL)))

        sessions.observeRecords().test { assertEquals(emptyList(), awaitItem()) }
        StreakRepositoryImpl(db.databases, io).observeDays().test { assertEquals(emptyList(), awaitItem()) }
    }

    // Daily

    @Test
    fun `only the first daily attempt of a day is stored`() = runTest(io) {
        val daily = DailyRepositoryImpl(db.databases, io)
        val first = DailyResult(epochDay = 20_000, level = Level.B1, score = 310, timeMs = 95_000, shareCode = null)

        daily.observe(20_000).test {
            assertNull(awaitItem())
            assertTrue(daily.record(first))
            assertEquals(first, awaitItem())
            assertFalse(daily.record(first.copy(score = 999)))
            expectNoEvents()
        }
    }

    // Jokers

    @Test
    fun `start pack is granted once, then saved and added counts persist`() = runTest(io) {
        val jokers = JokerRepositoryImpl(db.databases, io)
        val pack = GameConfig().jokerStartPack

        jokers.observeInventory().test {
            assertEquals(JokerInventory(), awaitItem())

            assertTrue(jokers.grantStartPack(pack))
            assertEquals(pack, awaitItem().withoutZeros())
            assertFalse(jokers.grantStartPack(pack))

            jokers.save(pack.consume(JokerType.LISTEN)!!)
            assertEquals(0, awaitItem().count(JokerType.LISTEN))

            jokers.add(JokerType.LISTEN, 2)
            assertEquals(2, awaitItem().count(JokerType.LISTEN))
        }
        assertFalse(jokers.grantStartPack(pack), "an empty inventory after use is not a first run")
    }

    @Test
    fun `wallet update writes inventory and gift days together and keeps them`() = runTest(io) {
        val jokers = JokerRepositoryImpl(db.databases, io)
        val pack = GameConfig().jokerStartPack
        jokers.grantStartPack(pack)

        val before = jokers.updateWallet { it }
        assertEquals(JokerWallet(pack), before.copy(inventory = before.inventory.withoutZeros()))

        val after = jokers.updateWallet {
            it.copy(
                inventory = it.inventory.plus(JokerType.LISTEN, 1),
                lastDailyGiftDay = 20_000,
                lastStreakBonusDay = 19_998,
            )
        }
        assertEquals(2, after.inventory.count(JokerType.LISTEN))

        // A second repository on the same user.db (next process) reads what was stored.
        val stored = JokerRepositoryImpl(db.databases, io).updateWallet { it }
        assertEquals(after, stored)
        jokers.observeInventory().test { assertEquals(after.inventory, awaitItem()) }
    }

    @Test
    fun `gift days never move back`() = runTest(io) {
        val jokers = JokerRepositoryImpl(db.databases, io)
        jokers.updateWallet { it.copy(lastDailyGiftDay = 20_000, lastStreakBonusDay = 19_990) }

        assertFailsWith<IllegalArgumentException> { jokers.updateWallet { it.copy(lastDailyGiftDay = 19_999) } }
        assertFailsWith<IllegalArgumentException> { jokers.updateWallet { it.copy(lastStreakBonusDay = null) } }

        assertEquals(JokerWallet(JokerInventory(), 20_000, 19_990), jokers.updateWallet { it })
    }

    @Test
    fun `wallet without a stored inventory leaves the start pack to come`() = runTest(io) {
        val jokers = JokerRepositoryImpl(db.databases, io)

        jokers.updateWallet { it.copy(lastDailyGiftDay = 20_000) }

        assertTrue(jokers.grantStartPack(GameConfig().jokerStartPack))
        assertEquals(20_000, jokers.updateWallet { it }.lastDailyGiftDay)
    }

    private fun JokerInventory.withoutZeros() = JokerInventory(counts.filterValues { it > 0 })
}
