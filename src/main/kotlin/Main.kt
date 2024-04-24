package hofwimmer.lukas

import nu.pattern.OpenCV
import org.opencv.core.Core
import org.opencv.highgui.HighGui

fun main() {
    OpenCV.loadShared()
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

    val ocr = OCRAnalysis()
    ocr.run("img/altesTestament_ArialBlack.png")
    HighGui.waitKey(0);
}
