package hofwimmer.lukas

abstract class ImageFeatureBase {
    var description: String = ""
    abstract fun CalcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double
}