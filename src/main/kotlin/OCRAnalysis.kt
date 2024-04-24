@file:JvmName("OCRAnalysisKt")

package hofwimmer.lukas

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
        lines.forEach { line ->
            val letters = getLetters(line)
        }
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

        // TODO convert letters to SubImageRegion
    }

    private fun BooleanArray.isFalse(): Boolean = this.none { it }


    private fun loadImage(imagePath: String): Mat {
        return Imgcodecs.imread(imagePath)
    }
}