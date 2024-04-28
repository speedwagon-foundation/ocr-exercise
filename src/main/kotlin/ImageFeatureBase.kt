import hofwimmer.lukas.SubImageRegion


abstract class ImageFeatureBase(
    private val description: String
) {
    abstract fun calcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double
}