package hofwimmer.lukas

import org.opencv.core.Mat

data class SubImageRegion(
    private val startX: Int,
    private val startY: Int,
    private val width: Int,
    private val height: Int,
    private val imgFragment: Mat
)