package hofwimmer.lukas

import ij.ImagePlus
import ij.gui.PolygonRoi
import ij.process.ByteProcessor
import ij.process.ColorProcessor
import ij.process.ImageProcessor

object ImageJUtility {
    /**
     *
     * @param pixels 1D byte array from ImageProcessor
     * @param width
     * @param height
     * @return 2D image array
     */
    fun convertFrom1DByteArr(pixels: ByteArray, width: Int, height: Int): Array<IntArray> {
        val inArray2D = Array(width) { IntArray(height) }

        var pixelIdx1D = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                inArray2D[x][y] = pixels[pixelIdx1D].toInt()
                if (inArray2D[x][y] < 0) {
                    inArray2D[x][y] += 256
                } // if

                pixelIdx1D++
            }
        }

        return inArray2D
    }

    /**
     * conversion from int to double image mask for intermediate calculations
     * @param inArr int[][] image array
     * @param width
     * @param height
     * @return double[][] image array
     */
    fun convertToDoubleArr2D(inArr: Array<IntArray>, width: Int, height: Int): Array<DoubleArray> {
        val returnArr = Array(width) { DoubleArray(height) }
        for (x in 0 until width) {
            for (y in 0 until height) {
                returnArr[x][y] = inArr[x][y].toDouble()
            }
        }

        return returnArr
    }

    /**
     * conversion from double to int image mask for visualization / result representation
     * @param inArr double[][] image array
     * @param width
     * @param height
     * @return int[][] image array
     */
    fun convertToIntArr2D(inArr: Array<DoubleArray>, width: Int, height: Int): Array<IntArray> {
        val returnArr = Array(width) { IntArray(height) }
        for (x in 0 until width) {
            for (y in 0 until height) {
                returnArr[x][y] = (inArr[x][y] + 0.5).toInt()
            }
        }

        return returnArr
    }

    /**
     * conversion back to 1D byte array for ImageJ use
     * @param inArr
     * @param width
     * @param height
     * @return
     */
    fun convertFrom2DIntArr(inArr: Array<IntArray>, width: Int, height: Int): ByteArray {
        var pixelIdx1D = 0
        val outArray2D = ByteArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                var resultVal = inArr[x][y]
                if (resultVal > 127) {
                    resultVal -= 256
                }
                outArray2D[pixelIdx1D] = resultVal.toByte()
                pixelIdx1D++
            }
        }

        return outArray2D
    }

    /**
     * opening new window for image visualization
     * @param inArr
     * @param width
     * @param height
     * @param title
     */
    fun showNewImage(inArr: Array<IntArray>, width: Int, height: Int, title: String?) {
        val byteArr = convertFrom2DIntArr(inArr, width, height)
        showNewImage(byteArr, width, height, title)
    }

    /**
     *
     * @param inArr
     * @param width
     * @param height
     * @param title
     * @param roi - ROI visualized in result representation
     */
    fun showNewImage(inArr: Array<IntArray>, width: Int, height: Int, title: String?, roi: PolygonRoi?) {
        val byteArr = convertFrom2DIntArr(inArr, width, height)
        showNewImage(byteArr, width, height, title, roi)
    }

    /**
     *
     * @param inArr in double[][] representation
     * @param width
     * @param height
     * @param title
     */
    fun showNewImage(inArr: Array<DoubleArray>, width: Int, height: Int, title: String?) {
        val intArr = convertToIntArr2D(inArr, width, height)
        val byteArr = convertFrom2DIntArr(intArr, width, height)
        showNewImage(byteArr, width, height, title)
    }


    /**
     *
     * @param inDataArr - 3 channel RGB image mask int[][][] to be visualized as RGB image
     * @param width
     * @param height
     * @param title
     */
    fun showNewImageRGB(inDataArr: Array<Array<IntArray>>, width: Int, height: Int, title: String?) {
        val outImgProc: ImageProcessor = ColorProcessor(width, height)
        val channelArr = IntArray(3)
        for (x in 0 until width) {
            for (y in 0 until height) {
                channelArr[0] = inDataArr[x][y][0]
                channelArr[1] = inDataArr[x][y][1]
                channelArr[2] = inDataArr[x][y][2]
                outImgProc.putPixel(x, y, channelArr)
            }
        }

        val ip = ImagePlus(title, outImgProc)
        ip.show()
    }

    /**
     *
     * @param inByteArr
     * @param width
     * @param height
     * @param title
     */
    fun showNewImage(inByteArr: ByteArray?, width: Int, height: Int, title: String?) {
        val outImgProc: ImageProcessor = ByteProcessor(width, height)
        outImgProc.pixels = inByteArr

        val ip = ImagePlus(title, outImgProc)
        ip.show()
    }

    /**
     *
     * @param inByteArr
     * @param width
     * @param height
     * @param title
     * @param roi
     */
    fun showNewImage(inByteArr: ByteArray?, width: Int, height: Int, title: String?, roi: PolygonRoi?) {
        val outImgProc: ImageProcessor = ByteProcessor(width, height)
        outImgProc.pixels = inByteArr

        val ip = ImagePlus(title, outImgProc)
        ip.roi = roi
        ip.show()
    }


    /**
     * representing 3-channel RGB image as int[][][]
     * @param ip
     * @param width
     * @param height
     * @param numOfChannels
     * @return
     */
    fun getChannelImageFromIP(ip: ImageProcessor, width: Int, height: Int, numOfChannels: Int): Array<Array<IntArray>> {
        val returnMask = Array(width) {
            Array(height) {
                IntArray(numOfChannels)
            }
        }

        val channelArr = IntArray(numOfChannels)

        for (x in 0 until width) {
            for (y in 0 until height) {
                ip.getPixel(x, y, channelArr)
                for (z in 0 until numOfChannels) {
                    returnMask[x][y][z] = channelArr[z]
                }
            }
        }

        return returnMask
    }

    /**
     * extract one color channel as separate image from int[][][] 3-channel structure
     * @param inImg
     * @param width
     * @param height
     * @param channelID
     * @return
     */
    fun getChannel(inImg: Array<Array<IntArray>>, width: Int, height: Int, channelID: Int): Array<IntArray> {
        val returnArr = Array(width) { IntArray(height) }
        for (x in 0 until width) {
            for (y in 0 until height) {
                returnArr[x][y] = inImg[x][y][channelID]
            }
        }
        return returnArr
    }

    /**
     * assign int[][] color layer to int[][][] 3-channel RGB structure
     * @param inImg
     * @param width
     * @param height
     * @param channelID in [0;2]
     * @param channelArr
     * @param numOfChannels == 3
     * @return
     */
    fun assignChannel(
        inImg: Array<Array<IntArray>>,
        width: Int,
        height: Int,
        channelID: Int,
        channelArr: Array<IntArray>,
        numOfChannels: Int
    ): Array<Array<IntArray>> {
        for (x in 0 until width) {
            for (y in 0 until height) {
                inImg[x][y][channelID] = channelArr[x][y]
            }
        }
        return inImg
    }
}