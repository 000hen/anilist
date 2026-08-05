package one.muisnowdevs.apps.anilist.source

import java.time.DayOfWeek

/**
 * Which section of the schedule a title belongs under.
 *
 * A weekday alone cannot say "not announced yet", and keying the schedule on `DayOfWeek?` would
 * leave every consumer remembering what a null day was supposed to mean. Naming the case instead
 * makes the eighth bucket an ordinary one: it groups, orders and renders down the same path as the
 * seven real days, so the only place that treats it specially is the one that decides where it sits.
 */
sealed interface ScheduleDay {
    data class Of(val week: DayOfWeek) : ScheduleDay

    data object Undetermined : ScheduleDay

    companion object {
        /**
         * The section a title with [week] belongs in — [Undetermined] when it has no weekday.
         *
         * Absence is the input on purpose: a title with no weekday carries no [WeekTime] at all, so
         * grouping reads the same nullable the rest of the stack does rather than a flag beside it.
         */
        fun of(week: DayOfWeek?): ScheduleDay = week?.let(::Of) ?: Undetermined
    }
}
