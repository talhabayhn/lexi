# ADR 0016: Navigation with JetBrains navigation-compose 2.10 and type-safe routes

- Status: Accepted
- Date: 2026-10-14

## Context
ARCHITECTURE.md §8 plans Navigation Compose with `@Serializable` routes on both platforms. JetBrains publishes the
multiplatform artifact `org.jetbrains.androidx.navigation:navigation-compose`. Its only stable release, 2.9.2, depends
on lifecycle 2.9 / savedstate 1.3 / Compose 1.8–1.9 and clashes with the Compose Multiplatform 1.12.1 set (lifecycle
2.11.0, savedstate 1.4.0). 2.10.0-rc01 is aligned with exactly that set.

## Decision
- Use `navigation-compose` **2.10.0-rc01**, pinned in the version catalog. This is a recorded pre-release exception
  (ADR 0013). **Upgrade condition:** move to 2.10.0 as soon as it is published as stable (and keep following stable
  2.10.x afterwards).
- Each feature exposes a `@Serializable` destination (`data object HomeRoute`, `data class GameRoute(...)`) and a
  `NavGraphBuilder.xxxScreen()` extension; the graph (`LexiNavHost`) lives in `shared`, the only module that knows all
  features. Cross-feature navigation goes through lambdas passed from `shared`, never feature-to-feature imports.
- ViewModels are obtained with Koin's `koinViewModel()` (koin-compose-viewmodel 4.2.2). We do not add
  `koin-compose-viewmodel-navigation`, which still pulls navigation 2.9.x.
- Koin starts in `App()` with `KoinApplication(configuration = koinConfiguration { modules(appModules) })`;
  `appModules` is checked by a koin-test `verify()` host test in `shared`.

## Alternatives considered
- Navigation 2.9.2 (stable): reintroduces savedstate/lifecycle version conflicts on iOS.
- Navigation 3 (stable 1.1.x): different back-stack model; would change ARCHITECTURE §8 and the route conventions.
  Re-evaluate if 2.10 stalls.
- Decompose / Voyager: extra paradigm and dependency without a concrete need.

## Consequences
- An rc dependency in production code until 2.10.0 ships; the surface we use (NavHost, `composable<T>`, type-safe
  routes) is stable API in androidx.
- Bumping Compose Multiplatform requires checking the matching navigation version.
