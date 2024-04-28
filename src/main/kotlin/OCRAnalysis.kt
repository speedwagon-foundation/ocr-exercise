@file:JvmName("OCRAnalysisKt")

package hofwimmer.lukas

import ImageFeatureBase
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.highgui.HighGui
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt


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
    private val F_MaxDistY: Int = 0,
) {
    private val refCharIndex = 2

    fun run(imagePath: String) {
        val image = loadImage(imagePath)
        //HighGui.imshow("test window", image)

        val gray = Mat()
        Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY)

        // Create an output image
        val output = Mat(gray.rows(), gray.cols(), CvType.CV_8UC1)

        // Perform binary thresholding
        val thresholdValue = 127.0
        val maxValue = 255.0
        Imgproc.threshold(gray, output, thresholdValue, maxValue, Imgproc.THRESH_BINARY)

        // Save the output image
        //HighGui.imshow("bnw", output)

        val lines = getLines(output)
        val allSubregions = mutableListOf<SubImageRegion>()
        lines.forEach { line ->
            val letters = getLetters(line.second)
            val subregions = letters.map {
                val res = convertToSubimageRegion(it.second, it.first, line.first)
                res
            }
            allSubregions.addAll(subregions)
        }

        val features = listOf(
            ImageFeatureF_FGCount(),
            ImageFeatureF_MaxDistX(),
            ImageFeatureF_MaxDistY()
        )

        allSubregions.forEachIndexed { i, region ->
            val fgCount = features[0].calcFeatureVal(region, 0)
            val maxDistX = features[1].calcFeatureVal(region, 0)
            val maxDistY = features[2].calcFeatureVal(region, 0)
//            println("region $i: fgCount: $fgCount, maxDistX: $maxDistX, maxDistY: $maxDistY")
        }

        // first: convert image to binary - where are black pixels present
        // second: find lines. - a line starts and ends if there is a black pixel in the row
        // third: separate individual characters. fire through vertical. for every column if there is a black pixel, it is a character - bounding box contains character
        // fourth: get features: summe von foreground pixel, height width,
        // fifth: normalize features - z normalization
        // sixth: use reference character set to compare the characters

        // use reference character set to compare the characters - normalize features
        // z normalization

        val normArr = calculateNormArr(allSubregions, 0, features)
        val stdDevs = calculateStandardDeviation(allSubregions, 0, features)
        println("normArr: ${normArr.joinToString(", ")}")

        // lower case a
        val referenceCharacter = allSubregions[refCharIndex]
        val referenceFeatures = calcFeatureArr(referenceCharacter, 0.0, features)
        val normalizedRefFeatures = normalize(referenceFeatures, normArr, stdDevs)
        println("referenceFeatures: ${referenceFeatures.joinToString(", ")}")

        val highlighted = image.clone()

        var matchCount = 0
        // iterate over all subregions, check if a character is matching the reference character
        allSubregions.forEach { currentLetter ->
            val featuresArr = calcFeatureArr(currentLetter, 0.0, features)
            val normalizedFeatures = normalize(featuresArr, normArr, stdDevs)

            if (isMatchingChar(normalizedFeatures, normalizedRefFeatures)) {
                println("------------------- start ----------------------")
                println("region features:        ${featuresArr.joinToString(", ")}")
                println("reference features:     ${referenceFeatures.joinToString(", ")}")
                println("mean of the population: ${normArr.joinToString(", ")}")
                println("stddev:                 ${stdDevs.joinToString(", ")}")
                println("normalized region:      ${normalizedFeatures.joinToString(", ")}")
                println("corr coeff:             ${correlationCoefficient(normalizedFeatures, normalizedRefFeatures)}")
                println("------------------- end ----------------------")
                highlightLetter(highlighted, currentLetter, 0.0)
                matchCount += 1
            }
        }

        HighGui.imshow("highlighted", highlighted)
        Imgcodecs.imwrite("output/highlighted.png", highlighted)
        println("match count: $matchCount")
    }

    private fun normalize(features: DoubleArray, normArr: DoubleArray, stdDev: DoubleArray): DoubleArray {
        return features.mapIndexed { i, feature ->
            (feature - normArr[i]) / stdDev[i]
        }.toDoubleArray()
    }

    private val CORR_COEFFICIENT_LIMIT = 0.99999999999
    private fun isMatchingChar(currFeatureArray: DoubleArray, referenceFeatureArray: DoubleArray): Boolean {
        val correlationCoefficient = correlationCoefficient(currFeatureArray, referenceFeatureArray)
        return correlationCoefficient > CORR_COEFFICIENT_LIMIT
    }

    private fun calculateStandardDeviation(
        allSubregions: List<SubImageRegion>,
        fgVal: Int,
        features: List<ImageFeatureBase>
    ): DoubleArray {
        return features.map { imageFeatureBase ->
            standardDeviation(allSubregions.map { imageFeatureBase.calcFeatureVal(it, fgVal) }.toDoubleArray())
        }.toDoubleArray()
    }

    private fun calculateNormArr(
        allSubregions: List<SubImageRegion>,
        fgVal: Int,
        features: List<ImageFeatureBase>
    ): DoubleArray {
        return features.map { imageFeatureBase ->
            val featureVal = allSubregions.sumOf { imageFeatureBase.calcFeatureVal(it, fgVal) }
            featureVal / allSubregions.size
        }.toDoubleArray()
    }

    private fun correlationCoefficient(xs: DoubleArray, ys: DoubleArray): Double {
        var sx = 0.0
        var sy = 0.0
        var sxx = 0.0
        var syy = 0.0
        var sxy = 0.0

        val n = xs.size

        for (i in 0 until n) {
            val x = xs[i]
            val y = ys[i]

            sx += x
            sy += y
            sxx += x * x
            syy += y * y
            sxy += x * y
        }

        // covariation
        val cov = sxy / n - sx * sy / n / n
        // standard error of x
        val sigmax = sqrt(sxx / n - sx * sx / n / n)
        // standard error of y
        val sigmay = sqrt(syy / n - sy * sy / n / n)

        // correlation is just a normalized covariation
        return cov / sigmax / sigmay
    }

    private fun highlightLetter(
        image: Mat,
        letter: SubImageRegion,
        fgVal: Double
    ) {
        val x = letter.startX
        val y = letter.startY
        val width = letter.width
        val height = letter.height

        for (i in 0 until width) {
            for (j in 0 until height) {
                if (letter.imgFragment[j, i][0] == fgVal) {
                    image.put(y + j, x + i, 0.0, 0.0, 255.0)
                }
            }
        }
    }

    fun calcFeatureArr(region: SubImageRegion, FGVal: Double, featuresToUse: List<ImageFeatureBase>): DoubleArray {
        return featuresToUse.map { feature ->
            feature.calcFeatureVal(region, FGVal.roundToInt())
        }.toDoubleArray()
    }

    fun standardDeviation(numbers: DoubleArray): Double {
        val mean = numbers.average()
        val variance = numbers.map { (it - mean).pow(2) }.average()
        return sqrt(variance)
    }

    fun convertToSubimageRegion(letter: Mat, widthUntilNow: Int, heightUntilNow: Int): SubImageRegion {
        val letterWidth = letter.cols()
        val letterHeight = letter.rows()

        return SubImageRegion(widthUntilNow, heightUntilNow, letterWidth, letterHeight, letter)
    }

    fun getLines(image: Mat): List<Pair<Int,Mat>> {
        // split by rows
        val lines = mutableListOf<Pair<Int,Mat>>()
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
                lines.add(Pair(startRow,subImage))
            } else {
                currRow++
            }
        }
        return lines.toList()
    }

    fun getLetters(line: Mat): List<Pair<Int, Mat>> {
        fun getCol(colIndex: Int): BooleanArray {
            val array = BooleanArray(line.cols())
            for (i in 0 until line.rows()) {
                array[i] = line.get(i, colIndex)[0] < 0.5
            }
            return array
        }

        val letters = mutableListOf<Pair<Int, Mat>>()
        var currCol = 0
        val converted = line.to2DBoolArray()
        var startCol = 0
        var endCol = 0
        while (currCol < line.cols()) {
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
                letters.add(Pair(startCol, subImage))
            }
        }
        return letters.toList()
    }

    private fun BooleanArray.isFalse(): Boolean = this.none { it }

    private fun loadImage(imagePath: String): Mat {
        return Imgcodecs.imread(imagePath)
    }
}

class ImageFeatureF_FGCount : ImageFeatureBase("F_FGCount") {
    override fun calcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
        return imgRegion?.imgFragment?.to2DBoolArray()?.sumOf { it.count { bool -> bool } }?.toDouble() ?: 0.0
    }
}

class ImageFeatureF_MaxDistX : ImageFeatureBase("F_MaxDistX") {
    override fun calcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
        var earliestX = Int.MAX_VALUE
        var latestX = Int.MIN_VALUE

        for (i in 0 until (imgRegion?.imgFragment?.rows() ?: 0)) {
            for (j in 0 until (imgRegion?.imgFragment?.cols() ?: 0)) {
                if ((imgRegion?.imgFragment?.get(i, j)?.get(0) ?: 0.0) < 0.5) {
                    earliestX = minOf(earliestX, j)
                    latestX = maxOf(latestX, j)
                }
            }
        }
        return (latestX - earliestX).toDouble()
    }
}

class ImageFeatureF_MaxDistY : ImageFeatureBase("F_MaxDistY") {
    override fun calcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
        var earliestY = Int.MAX_VALUE
        var latestY = Int.MIN_VALUE

        for (i in 0 until (imgRegion?.imgFragment?.cols() ?: 0)) {
            for (j in 0 until (imgRegion?.imgFragment?.rows() ?: 0)) {
                if ((imgRegion?.imgFragment?.get(i, j)?.get(0) ?: 0.0) < 0.5) {
                    earliestY = minOf(earliestY, j)
                    latestY = maxOf(latestY, j)
                }
            }
        }

        return (latestY - earliestY).toDouble()
    }
}
