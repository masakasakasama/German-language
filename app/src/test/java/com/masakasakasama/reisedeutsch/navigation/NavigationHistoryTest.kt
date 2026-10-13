package com.masakasakasama.reisedeutsch.navigation

import org.junit.Assert.*
import org.junit.Test

class NavigationHistoryTest {
    @Test fun everyNonHomeScreenCanReturnWithoutRequestingAppExit() {
        listOf("lessons", "map", "conversation", "conversation-detail", "collection", "profile",
            "settings", "backup", "course", "course-unit", "course-complete", "study", "complete").forEach { page ->
            val history = NavigationHistory()
            history.navigate(AppDestination(page))
            assertEquals("home", history.back()?.page)
            assertNull(history.back())
        }
    }

    @Test fun settingsAndBackupReturnToTheirEntryScreen() {
        val history = NavigationHistory()
        history.navigate(AppDestination("profile"))
        history.navigate(AppDestination("settings"))
        assertEquals("profile", history.back()?.page)
        history.navigate(AppDestination("backup"))
        assertEquals("profile", history.back()?.page)
        assertEquals("home", history.back()?.page)
    }

    @Test fun studyReturnsToTheExactCourseUnit() {
        val history = NavigationHistory()
        val unit = AppDestination("course-unit", "foundation-06")
        history.navigate(AppDestination("course"))
        history.navigate(unit)
        history.navigate(AppDestination("study", "foundation-06"))
        assertEquals(unit, history.back())
        assertEquals("course", history.back()?.page)
    }

    @Test fun previousUnitContentIsRestoredAfterOpeningNextUnit() {
        val history = NavigationHistory()
        val completed = AppDestination("course-complete", "foundation-01")
        history.navigate(completed)
        history.navigate(AppDestination("course-unit", "foundation-02"))
        assertEquals(completed, history.back())
    }

    @Test fun completedStudyIsRemovedFromTheBackPath() {
        val history = NavigationHistory()
        history.navigate(AppDestination("lessons"))
        history.navigate(AppDestination("study"))
        history.replaceCurrent(AppDestination("complete"))
        assertEquals("lessons", history.back()?.page)
    }

    @Test fun revisitingAnExistingPageDoesNotCreateBackLoops() {
        val history = NavigationHistory()
        history.navigate(AppDestination("course"))
        history.navigate(AppDestination("course-unit", "foundation-01"))
        history.navigate(AppDestination("study", "foundation-01"))
        history.navigate(AppDestination("course-unit", "foundation-01"))
        assertEquals("course", history.back()?.page)
        history.navigate(AppDestination("profile"))
        history.navigate(AppDestination("home"))
        assertNull(history.back())
    }
}
