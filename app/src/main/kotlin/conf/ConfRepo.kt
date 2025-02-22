package conf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class ConfRepo(
    private val queries: ConfQueries,
) {

    private val _conf: MutableStateFlow<Conf> = MutableStateFlow(
        queries.select() ?: default()
    )

    val conf: StateFlow<Conf> = _conf.asStateFlow()

    val current: Conf
        get() = conf.value

    init {
        conf
            .onEach { withContext(Dispatchers.IO) { queries.insertOrReplace(it) } }
            .launchIn(GlobalScope)
    }

    fun update(newConf: (Conf) -> Conf) {
        _conf.update { newConf(conf.value) }
    }

    fun default(): Conf {
        return Conf(
            lastSyncDate = null,
            viewportNorthLat = 12.116667 + 0.04,
            viewportEastLon = -68.933333 + 0.04 + 0.03,
            viewportSouthLat = 12.116667 - 0.04,
            viewportWestLon = -68.933333 - 0.04 + 0.03,
            showAtms = false,
            showSyncSummary = false,
            notifyOfNewElementsNearby = false,
        )
    }
}