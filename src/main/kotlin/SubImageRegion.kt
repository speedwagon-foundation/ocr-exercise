package hofwimmer.lukas

import org.opencv.core.Mat

data class SubImageRegion(
    val startX: Int,
    val startY: Int,
    val width: Int,
    val height: Int,
    val imgFragment: Mat
)