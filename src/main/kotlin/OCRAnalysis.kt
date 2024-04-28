@file:JvmName("OCRAnalysisKt")

package hofwimmer.lukas

import ImageFeatureBase
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.highgui.HighGui
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

fun Mat.to2DArray(): Array<IntArray> {
    val array = Array(this.rows()) { IntArray(this.cols()) }
    for (i in 0 until this.rows()) {
        for (j in 0 until this.cols()) {
            array[i][j] = this.get(i, j)[0].toInt()
        }
    }
    return array
}

fun Mat.to2DBoolArray(): Array<BooleanArray> {
    val array = Array(this.rows()) { BooleanArray(this.cols()) }
    for (i in 0 until this.rows()) {
        for (j in 0 until this.cols()) {
            array[i][j] = this.get(i, j)[0] < 0.5
        }
    }
    return array
}

class OCRAnalysis(
    private val F_FGcount: Int = 0,
    private val F_MaxDistX: Int = 0,
    private val F_MaxDistY: Int = 0
) {
    fun run(imagePath: String) {
        val image = loadImage(imagePath)
        HighGui.imshow("test window", image)

        val gray = Mat()
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY)

        // Create an output image
        val output = Mat(gray.rows(), gray.cols(), CvType.CV_8UC1)

        // Perform binary thresholding
        val thresholdValue = 127.0
        val maxValue = 255.0
        Imgproc.threshold(gray, output, thresholdValue, maxValue, Imgproc.THRESH_BINARY)

        // Save the output image
        HighGui.imshow("bnw", output)

        val something = output[50, 50]
        println("value: ${output[50, 50]}")

        val lines = getLines(output)
        var heightUntilNow = 0
        val allSubregions = mutableListOf<SubImageRegion>()
        lines.forEach { line ->
            val letters = getLetters(line)
            var widthUntilNow = 0
            val subregions = letters.map {
                val res = convertToSubimageRegion(it, widthUntilNow, heightUntilNow)
                widthUntilNow += it.cols()
                res
            }
            allSubregions.addAll(subregions)
            heightUntilNow+= line.rows()
        }
        var x = 0

        // first: convert image to binary - where are black pixels present
        // second: find lines. - a line starts and ends if there is a black pixel in the row
        // third: separate individual characters. fire through vertical. for every column if there is a black pixel, it is a character - bounding box contains character
        // fourth: get features: summe von foreground pixel, height width,
        // fifth: normalize features - z normalization
        // sixth: use reference character set to compare the characters

        // use reference character set to compare the characters - normalize features
        // z normalization

        
    }

    fun convertToSubimageRegion(letter: Mat, widthUntilNow: Int, heightUntilNow: Int): SubImageRegion {
        val letterWidth = letter.cols()
        val letterHeight = letter.rows()


        return SubImageRegion(widthUntilNow, heightUntilNow, letterWidth, letterHeight, letter)
    }

    fun getLines(image: Mat): List<Mat> {
        // split by rows
        val lines = mutableListOf<Mat>()
        var currRow = 0
        val converted = image.to2DBoolArray()
        var startRow = 0
        var endRow = 0
        while (currRow < image.rows()) {
            var rowArray = converted[currRow]
            if (!rowArray.isFalse()) {
                startRow = currRow
                while (!rowArray.isFalse()) {
                    currRow++
                    rowArray = converted[currRow]
                }
                endRow = currRow

                val subImage = image.submat(startRow, endRow, 0, image.cols())
                Imgcodecs.imwrite("output/subimage_$startRow-$endRow.png", subImage)
                lines.add(subImage)
                // split image into rows / subimages
            } else {
                currRow++
            }
        }
        return lines.toList()
    }

    fun getLetters(line: Mat): List<Mat> {
        fun getCol(colIndex: Int): BooleanArray {
            val array = BooleanArray(line.cols())
            for (i in 0 until line.rows()) {
                array[i] = line.get(i, colIndex)[0] < 0.5
            }
            return array
        }

        val letters = mutableListOf<Mat>()
        var currCol = 0
        val converted = line.to2DBoolArray()
        var startCol = 0
        var endCol = 0
        while (currCol < line.cols()) {
            println("currcol: $currCol | line.cols: ${line.cols()}")
            var colArray = getCol(currCol)

            if (colArray.isFalse()) {
                currCol++
            } else {
                startCol = currCol
                while (!colArray.isFalse()) {
                    currCol++
                    colArray = getCol(currCol)
                }
                endCol = currCol

                val subImage = line.submat(0, line.rows(), startCol, endCol)
                Imgcodecs.imwrite("output/letters/subimage_$startCol-$endCol.png", subImage)
                letters.add(subImage)
            }
        }
        return letters.toList()
    }
    private fun BooleanArray.isFalse(): Boolean = this.none { it }

    private fun loadImage(imagePath: String): Mat {
        return Imgcodecs.imread(imagePath)
    }
}

sealed class ImageFeatureF_FGCount : ImageFeatureBase("F_FGCount") {
    override fun calcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
        return imgRegion?.imgFragment?.to2DBoolArray()?.sumOf { it.count { bool -> bool } }?.toDouble() ?: 0.0
    }
}

sealed class ImageFeatureF_MaxDistX : ImageFeatureBase("F_MaxDistX") {
    override fun calcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
        var earliestX = Int.MAX_VALUE
        var latestX = Int.MIN_VALUE

        for(i in 0 until (imgRegion?.imgFragment?.rows() ?: 0)) {
            for(j in 0 until (imgRegion?.imgFragment?.cols() ?: 0)) {
                if((imgRegion?.imgFragment?.get(i, j)?.get(0) ?: 0.0) < 0.5) {
                    earliestX = minOf(earliestX, j)
                    latestX = maxOf(latestX, j)
                }
            }
        }

        return (latestX - earliestX).toDouble()
    }
}

sealed class ImageFeatureF_MaxDistY : ImageFeatureBase("F_MaxDistY") {
    override fun calcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
        var earliestY = Int.MAX_VALUE
        var latestY = Int.MIN_VALUE

        for(i in 0 until (imgRegion?.imgFragment?.cols() ?: 0)) {
            for(j in 0 until (imgRegion?.imgFragment?.rows() ?: 0)) {
                if((imgRegion?.imgFragment?.get(i, j)?.get(0) ?: 0.0) < 0.5) {
                    earliestY = minOf(earliestY, j)
                    latestY = maxOf(latestY, j)
                }
            }
        }

        return (latestY - earliestY).toDouble()
    }
}