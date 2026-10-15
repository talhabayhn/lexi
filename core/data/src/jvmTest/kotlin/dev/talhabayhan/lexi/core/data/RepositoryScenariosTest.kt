package dev.talhabayhan.lexi.core.data

import dev.talhabayhan.lexi.core.data.content.CardRepositoryImpl
import dev.talhabayhan.lexi.core.data.settings.SettingsRepositoryImpl
import dev.talhabayhan.lexi.core.data.settings.createSettingsDataStore
import dev.talhabayhan.lexi.core.data.testing.ContentFixture.FixtureCard
import dev.talhabayhan.lexi.core.data.testing.TestDatabases
import dev.talhabayhan.lexi.core.data.user.ProgressRepositoryImpl
import dev.talhabayhan.lexi.core.data.user.SessionRepositoryImpl
import dev.talhabayhan.lexi.core.data.user.StreakRepositoryImpl
import dev.talhabayhan.lexi.core.domain.game.GameConfig
import dev.talhabayhan.lexi.core.domain.game.GameMode
import dev.talhabayhan.lexi.core.domain.game.SlotState
import dev.talhabayhan.lexi.core.domain.game.WheelGenerator
import dev.talhabayhan.lexi.core.domain.model.CardId
import dev.talhabayhan.lexi.core.domain.model.CategoryId
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.core.domain.random.SeededRandom
import dev.talhabayhan.lexi.core.domain.session.SessionResult
import dev.talhabayhan.lexi.core.domain.settings.Settings
import dev.talhabayhan.lexi.core.domain.srs.CardOutcome
import dev.talhabayhan.lexi.core.domain.srs.Srs
import dev.talhabayhan.lexi.core.domain.stats.Stats
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** End-to-end flows over the in-memory databases: what the game, notebook and stats screens will do. */
class RepositoryScenariosTest {
    private val io = UnconfinedTestDispatcher()
    private val config = GameConfig()
    private val day = 86_400_000L

    /** Two A1 cards per letter (`<letter>ONE`, `<letter>TWO`) plus one exam-pack-only card per letter. */
    private val fullA1 = ('A'..'Z').flatMap { c ->
        listOf(
            FixtureCard("a1-${c.lowercaseChar()}one", "${c}ONE"),
            FixtureCard("a1-${c.lowercaseChar()}two", "${c}TWO", category = "things"),
            FixtureCard("a1-${c.lowercaseChar()}pack", "${c}PACK", packOnly = true),
        )
    }

    @Test
    fun `a wheel from the content DB has 26 playable slots and never an exam-pack card`() = runTest(io) {
        val db = TestDatabases(io, cards = fullA1)
        val pool = CardRepositoryImpl(db.databases, io).wheelPool(Level.A1)

        val wheel = WheelGenerator.generate(
            pool = pool,
            level = Level.A1,
            category = CategoryId.MIXED,
            mode = GameMode.CLASSIC,
            random = SeededRandom(7).derive(WheelGenerator.SEED_PURPOSE),
        )

        assertEquals(('A'..'Z').toList(), wheel.slots.map { it.letter })
        assertTrue(wheel.slots.all { it.state == SlotState.TODO })
        assertTrue(wheel.slots.none { it.card!!.id.value.endsWith("pack") })
        db.close()
    }

    @Test
    fun `cards seen in the last 7 days are avoided using stored lastSeenMs`() = runTest(io) {
        val db = TestDatabases(io, cards = fullA1)
        val progress = ProgressRepositoryImpl(db.databases, io)
        val now = 100 * day
        // Every "ONE" card was seen yesterday; "TWO" cards of A-M were seen 8 days ago (outside the window).
        progress.save(
            ('A'..'Z').map { c ->
                Srs.apply(null, CardId("a1-${c.lowercaseChar()}one"), CardOutcome.CORRECT, 99, now - day)
            } + ('A'..'M').map { c ->
                Srs.apply(null, CardId("a1-${c.lowercaseChar()}two"), CardOutcome.CORRECT, 92, now - 8 * day)
            },
        )

        val recentlySeen = WheelGenerator.recentlySeen(progress.lastSeenMs(), now, config)
        val wheel = WheelGenerator.generate(
            pool = CardRepositoryImpl(db.databases, io).wheelPool(Level.A1),
            level = Level.A1,
            category = CategoryId.MIXED,
            mode = GameMode.CLASSIC,
            random = SeededRandom(7).derive(WheelGenerator.SEED_PURPOSE),
            recentlySeen = recentlySeen,
        )

        assertEquals(26, recentlySeen.size)
        assertTrue(wheel.slots.all { it.card!!.id.value.endsWith("two") }, wheel.slots.map { it.card!!.id }.toString())
        db.close()
    }

    @Test
    fun `SRS progress survives the round trip through user db across days`() = runTest(io) {
        val db = TestDatabases(io, cards = fullA1)
        val progress = ProgressRepositoryImpl(db.databases, io)
        val apple = CardId("a1-aone")

        // Day 10: missed → notebook box 0, due today.
        progress.save(listOf(Srs.apply(null, apple, CardOutcome.WRONG, 10, 10 * day)))
        assertEquals(1, Srs.dueCount(progress.observeAll().first(), 10))

        // Day 10 review: correct → box 1, due day 11.
        val stored = progress.get(listOf(apple))
        progress.save(listOf(Srs.apply(stored[apple], apple, CardOutcome.CORRECT, 10, 10 * day + 1)))
        val after = progress.get(listOf(apple)).getValue(apple)

        assertEquals(1, after.box)
        assertEquals(11L, after.dueEpochDay)
        assertEquals(2, after.seen)
        assertEquals(listOf(apple), Srs.dueCards(progress.observeAll().first(), 11))
        assertEquals(1, Stats.learnedWords(progress.observeAll().first()))
        db.close()
    }

    @Test
    fun `a content update hides progress of removed cards and shows it again when they return`() = runTest(io) {
        val v1 = TestDatabases(io, cards = fullA1, contentVersion = 1)
        val removed = CardId("a1-zone")
        ProgressRepositoryImpl(v1.databases, io).save(listOf(Srs.apply(null, removed, CardOutcome.WRONG, 1, day)))

        val v2 = TestDatabases(io, cards = fullA1.filterNot { it.id == removed.value }, userDriver = v1.userDriver)
        assertTrue(ProgressRepositoryImpl(v2.databases, io).observeAll().first().isEmpty())

        val v3 = TestDatabases(io, cards = fullA1, userDriver = v1.userDriver)
        assertEquals(listOf(removed), ProgressRepositoryImpl(v3.databases, io).observeAll().first().map { it.cardId })
        v2.contentDriver.close()
        v3.contentDriver.close()
        v1.close()
    }

    @Test
    fun `sessions feed stats and the streak, duel games feed neither`() = runTest(io) {
        val db = TestDatabases(io)
        val sessions = SessionRepositoryImpl(db.databases, io, config)
        fun session(dayNo: Long, mode: GameMode = GameMode.CLASSIC, completed: Boolean = true) = SessionResult(
            mode = mode,
            level = Level.A1,
            category = null,
            seed = dayNo,
            startedAtMs = dayNo * day,
            endedAtMs = dayNo * day + 1,
            epochDay = dayNo,
            score = 100,
            correct = 10,
            wrong = 2,
            passed = 0,
            completed = completed,
        )

        listOf(session(5), session(6), session(7, completed = false), session(8), session(9), session(10))
            .forEach { sessions.record(it) }
        sessions.record(session(7, mode = GameMode.DUEL))

        val records = sessions.observeRecords().first()
        assertEquals(3, Stats.longestStreak(records))
        assertEquals(5, Stats.completedWheels(records))
        assertEquals(listOf(5L, 6L, 8L, 9L, 10L), StreakRepositoryImpl(db.databases, io).observeDays().first())
        db.close()
    }

    @Test
    fun `settings persist across DataStore instances on the same file`() = runTest(io) {
        val dir: File = createTempDirectory("lexi-settings-").toFile()
        val path = File(dir, "datastore/settings.preferences_pb").path
        val firstJob = Job()
        SettingsRepositoryImpl(createSettingsDataStore(path, CoroutineScope(firstJob + io)), config)
            .update { it.copy(classicTimeLimitSeconds = 300, haptics = false) }
        firstJob.cancelAndJoin()

        val reopened = SettingsRepositoryImpl(createSettingsDataStore(path, backgroundScope), config)

        assertEquals(Settings(classicTimeLimitSeconds = 300, haptics = false), reopened.settings.first())
        dir.deleteRecursively()
    }
}
