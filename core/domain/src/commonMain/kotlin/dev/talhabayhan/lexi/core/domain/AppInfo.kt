package dev.talhabayhan.lexi.core.domain

/**
 * App-wide constants. The brand name lives here and in the string resources; renaming the app means updating:
 * this constant, `app_name` in `core/designsystem/src/commonMain/composeResources/values*`, `app_name` in
 * `androidApp/src/main/res/values/strings.xml` and `PRODUCT_NAME` in `iosApp/Configuration/Config.xcconfig`.
 */
public object AppInfo {
    public const val NAME: String = "Lexi"
}
