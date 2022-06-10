package game.navigation

import kotlinx.coroutines.flow.MutableStateFlow

class NavController(initial: NavDestination) {

    private val deque = ArrayDeque<NavDestination>()

    val currentDestination: MutableStateFlow<NavDestination> by lazy { MutableStateFlow(deque.last()) }

    init {
        deque.add(initial)
    }

    fun navigate(destination: NavDestination) {
        deque.add(destination)
        currentDestination.value = destination
    }

    fun popBackStack() {
        if (deque.size > 1) {
            deque.removeLast()
        }
        currentDestination.value = deque.last()
    }

}