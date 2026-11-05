package io.github.isaquefontinele.lido.capture

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Starts fetching and extracting a saved item without blocking the share flow. */
fun interface ExtractionScheduler {
    fun schedule(itemId: String)
}

/** Runs the pipeline right away in an app-wide scope. */
class ImmediateExtractionScheduler(
    private val scope: CoroutineScope,
    private val pipeline: ArticlePipeline,
) : ExtractionScheduler {
    override fun schedule(itemId: String) {
        scope.launch { pipeline.process(itemId) }
    }
}
