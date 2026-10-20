package dev.talhabayhan.lexi.feature.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.talhabayhan.lexi.core.designsystem.theme.LexiTheme
import dev.talhabayhan.lexi.core.domain.model.Level
import dev.talhabayhan.lexi.feature.game.components.rememberGameMotion
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

/** Type-safe destination of the onboarding level test run (`GAME_RULES.md` §9). */
@Serializable
data object LevelTestRoute

/**
 * Registers the level test run on the game screen. [onFinished] gets the placement (nothing was recorded); [onQuit]
 * runs after a confirmed "Pes et" or "Geri dön" of a test that can't start (back to the level choice, nothing
 * saved); [onOpenSettings] for "Ayarlar" of the pause dialog (the test stays paused underneath).
 */
fun NavGraphBuilder.levelTestScreen(onFinished: (Level) -> Unit, onQuit: () -> Unit, onOpenSettings: () -> Unit) {
    composable<LevelTestRoute> {
        LevelTestRoute(onFinished = onFinished, onQuit = onQuit, onOpenSettings = onOpenSettings)
    }
}

@Composable
internal fun LevelTestRoute(
    onFinished: (Level) -> Unit,
    onQuit: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: LevelTestViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Read only inside lambdas (draw phase / derived state), so ticks never recompose the route.
    val clock = viewModel.clock.collectAsStateWithLifecycle()
    val motion = rememberGameMotion(LexiTheme.motion)
    val currentOnFinished by rememberUpdatedState(onFinished)
    val currentOnQuit by rememberUpdatedState(onQuit)
    val currentOnOpenSettings by rememberUpdatedState(onOpenSettings)
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner, motion) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is LevelTestEffect.Motion -> launch { motion.play(effect.effect) }
                    is LevelTestEffect.Finished -> currentOnFinished(effect.level)
                    LevelTestEffect.Quit -> currentOnQuit()
                    LevelTestEffect.OpenSettings -> currentOnOpenSettings()
                }
            }
        }
    }
    // Same as the game: a running card → paused; while loading, before the start or after a load error back leaves.
    GameBackHandler(
        enabled = !state.isLoading && !state.isStarting && state.loadError == null,
        onBack = { viewModel.onIntent(GameIntent.Back) },
    )
    val readClock = remember(clock) { { clock.value } }
    GameScreen(state = state, clock = readClock, onIntent = viewModel::onIntent, motion = motion)
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LevelTestMidTestPreview() {
    LexiTheme(reducedMotion = false) {
        GameScreen(state = LevelTestSamples.midTest, clock = { LevelTestSamples.midTestClock }, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LevelTestRevealPreview() {
    LexiTheme(reducedMotion = true) {
        GameScreen(state = LevelTestSamples.reveal, clock = { LevelTestSamples.midTestClock }, onIntent = {})
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun LevelTestUnavailablePreview() {
    LexiTheme(reducedMotion = false) {
        GameScreen(state = LevelTestSamples.unavailable, clock = { GameClockUi.Untimed }, onIntent = {})
    }
}
