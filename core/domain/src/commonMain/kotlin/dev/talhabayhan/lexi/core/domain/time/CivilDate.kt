package dev.talhabayhan.lexi.core.domain.time

/** A calendar date (proleptic Gregorian); [month] is 1–12. */
public data class CivilDate(val year: Int, val month: Int, val day: Int)

/**
 * The calendar date of an epoch day (days since 1970-01-01, as `AppClock.epochDay` returns it), e.g. the day and month
 * on the home screen's daily card. Pure arithmetic (Hinnant's `civil_from_days`), valid for any epoch day.
 */
public fun civilDateOfEpochDay(epochDay: Long): CivilDate {
    val z = epochDay + DAYS_FROM_0000_TO_1970
    val era = z.floorDiv(DAYS_PER_ERA)
    val dayOfEra = z - era * DAYS_PER_ERA
    val yearOfEra = (
        dayOfEra - dayOfEra / DAYS_PER_4_YEARS + dayOfEra / DAYS_PER_100_YEARS -
            dayOfEra / (DAYS_PER_ERA - 1)
        ) / DAYS_PER_YEAR
    val dayOfYear = dayOfEra - (DAYS_PER_YEAR * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val monthIndex = (5 * dayOfYear + 2) / DAYS_PER_5_MONTHS
    val day = (dayOfYear - (DAYS_PER_5_MONTHS * monthIndex + 2) / 5 + 1).toInt()
    val month = (if (monthIndex < MARCH_BASED_YEAR_END) monthIndex + 3 else monthIndex - 9).toInt()
    val year = (yearOfEra + era * YEARS_PER_ERA + if (month <= 2) 1 else 0).toInt()
    return CivilDate(year, month, day)
}

private const val DAYS_FROM_0000_TO_1970 = 719_468L
private const val DAYS_PER_ERA = 146_097L
private const val DAYS_PER_4_YEARS = 1_460L
private const val DAYS_PER_100_YEARS = 36_524L
private const val DAYS_PER_YEAR = 365L
private const val DAYS_PER_5_MONTHS = 153L
private const val MARCH_BASED_YEAR_END = 10L
private const val YEARS_PER_ERA = 400L
