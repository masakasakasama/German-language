package com.masakasakasama.reisedeutsch.navigation

data class AppDestination(val page: String, val courseUnitId: String? = null, val conversationId: String? = null)

/** Keeps the previous screen and its content, rather than letting system Back finish the activity. */
class NavigationHistory {
    private val entries = mutableListOf(AppDestination("home"))
    val current: AppDestination get() = entries.last()

    fun navigate(destination: AppDestination) {
        if (destination.page == "home") {
            entries.clear()
            entries.add(AppDestination("home"))
            return
        }
        val existing = entries.indexOfLast { it == destination }
        if (existing >= 0) {
            while (entries.lastIndex > existing) entries.removeAt(entries.lastIndex)
        } else entries.add(destination)
    }

    fun replaceCurrent(destination: AppDestination) {
        if (destination.page == "home") navigate(destination)
        else if (entries.size == 1) entries.add(destination)
        else entries[entries.lastIndex] = destination
    }

    /** null means Home: the UI should ask before exiting. */
    fun back(): AppDestination? {
        if (entries.size == 1) return null
        entries.removeAt(entries.lastIndex)
        return current
    }
}
