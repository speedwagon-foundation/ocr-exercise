//package hofwimmer.lukas
//
//import ij.*
//import ij.plugin.filter.PlugInFilter
//import ij.plugin.filter.PlugInFilter.*
//import ij.process.*
//import java.util.*
//
//class OCRAnalysis : PlugInFilter {
//    override fun setup(arg: String, imp: ImagePlus?): Int {
//        if (arg == "about") {
//            showAbout()
//            return DONE
//        }
//
//
//        return DOES_8G + DOES_STACKS + SUPPORTS_MASKING
//    } //setup
//
//    //----------------------------------------------
//    override fun run(ip: ImageProcessor) {
//        val featureVect = Vector<ImageFeatureBase>()
//        featureVect.add(ImageFeatureF_FGcount())
//
//
//        //TODO: build up feature vector
//        val pixels = ip.getPixels() as ByteArray
//        val width: Int = ip.getWidth()
//        val height: Int = ip.getHeight()
//        val inDataArrInt: Array<IntArray> = ImageJUtility.convertFrom1DByteArr(pixels, width, height)
//
//
//        //(1) at first do some binarization
//        val FG_VAL = 0
//        val BG_VAL = 255
//        val MARKER_VAL = 127
//        val thresholdVal = -1 //?;
//
//        val binaryThreshTF: IntArray = ImageTransformationFilter.GetBinaryThresholdTF(255, thresholdVal, FG_VAL, BG_VAL)
//        val binaryImgArr: Array<IntArray?> =
//            ImageTransformationFilter.GetTransformedImage(inDataArrInt, width, height, binaryThreshTF)
//
//        ImageJUtility.showNewImage(binaryImgArr, width, height, "binary image at threh = $thresholdVal")
//
//
//        //(2) split the image according to fire-trough or multiple region growing
//        val splittedCharacters: Vector<Vector<SubImageRegion>> =
//            splitCharacters(binaryImgArr, width, height, BG_VAL, FG_VAL)
//
//
//        //TODO: for reasons of testing, visualize some of the split characters
//
//        //TODO: let the user specify the target character
//        val tgtCharRow = 0
//        val tgtCharCol = 4
//        val charROI: SubImageRegion = splittedCharacters[tgtCharRow][tgtCharCol]
//        ImageJUtility.showNewImage(
//            charROI.subImgArr, charROI.width, charROI.height,
//            "char at pos $tgtCharRow / $tgtCharCol"
//        )
//
//
//        //calculate features of reference character
//        val featureResArr = calcFeatureArr(charROI, BG_VAL, featureVect)
//        printoutFeatureRes(featureResArr, featureVect)
//
//
//        //TODO calculate mean values for all features based on all characters
//        //==> required for normalization
//        val normArr = calculateNormArr(splittedCharacters, BG_VAL, featureVect)
//        printoutFeatureRes(normArr, featureVect)
//
//        val hitCount = 0 //count the number of detected characters
//
//
//        //TODO: now detect all matching characters
//        //forall SubImageRegion sir in splittedCharacters
//        //if isMatchingChar(..,sir,..) then markRegionInImage(..,sir,..)
//        IJ.log("# of letters detected = $hitCount")
//
//        ImageJUtility.showNewImage(binaryImgArr, width, height, "result image with marked letters")
//    } //run
//
//    fun markRegionInImage(
//        inImgArr: Array<IntArray>,
//        imgRegion: SubImageRegion?,
//        colorToReplace: Int,
//        tgtColor: Int
//    ): Array<IntArray> {
//        //TODO: implementation required
//        return inImgArr
//    }
//
//    fun isMatchingChar(
//        currFeatureArr: DoubleArray?,
//        refFeatureArr: DoubleArray?,
//        normFeatureArr: DoubleArray?
//    ): Boolean {
//        val CORR_COEFFICIENT_LIMIT = -1.0 //?;
//
//
//        //TODO: implementation required
//        return false
//    }
//
//
//    fun printoutFeatureRes(featureResArr: DoubleArray, featuresToUse: Vector<ImageFeatureBase>) {
//        IJ.log("========== features =========")
//        for (i in featuresToUse.indices) {
//            IJ.log(("res of F " + i + ", " + featuresToUse[i].description).toString() + " is " + featureResArr[i])
//        }
//    }
//
//
//    fun calcFeatureArr(region: SubImageRegion?, FGval: Int, featuresToUse: Vector<ImageFeatureBase>): DoubleArray {
//        //TODO implementation required
//        val featureResArr = DoubleArray(featuresToUse.size)
//
//        //foreach feature f in featuresToUse resultVal = f.CalcFeatureVal(...)
//        return featureResArr
//    }
//
//    fun calculateNormArr(
//        inputRegions: Vector<Vector<SubImageRegion>>?,
//        FGval: Int,
//        featuresToUse: Vector<ImageFeatureBase>
//    ): DoubleArray {
//        //calculate the average per feature to allow for normalization
//        val returnArr = DoubleArray(featuresToUse.size)
//
//        //TODO implementation required
//        return returnArr
//    }
//
//    //outer Vector ==> lines, inner vector characters per line, i.e. columns
//    fun splitCharacters(
//        inImg: Array<IntArray?>?,
//        width: Int,
//        height: Int,
//        BG_val: Int,
//        FG_val: Int
//    ): Vector<Vector<SubImageRegion>> {
//        val returnCharMatrix: Vector<Vector<SubImageRegion>> = Vector<Vector<SubImageRegion>>()
//
//
//        //TODO: implementation required
//        return returnCharMatrix
//    }
//
//    fun splitCharactersVertically(
//        rowImage: SubImageRegion?,
//        BG_val: Int,
//        FG_val: Int,
//        origImg: Array<IntArray?>?
//    ): Vector<SubImageRegion> {
//        val returnCharArr: Vector<SubImageRegion> = Vector<SubImageRegion>()
//
//
//        //TODO: implementation required
//        return returnCharArr
//    }
//
//    //probably useful helper method
//    fun isEmptyRow(inImg: Array<IntArray>, width: Int, rowIdx: Int, BG_val: Int): Boolean {
//        for (x in 0 until width) {
//            if (inImg[x][rowIdx] != BG_val) {
//                return false
//            }
//        }
//        return true
//    }
//
//    //probably useful helper method
//    fun isEmptyColumn(inImg: Array<IntArray>, height: Int, colIdx: Int, BG_val: Int): Boolean {
//        for (y in 0 until height) {
//            if (inImg[colIdx][y] != BG_val) {
//                return false
//            }
//        }
//        return true
//    }
//
//
//    fun showAbout() {
//        IJ.showMessage(
//            "About Template_...",
//            "this is a RegionGrowing_ template\n"
//        )
//    } //showAbout
//
//
//    //the features to implement
//    internal inner class ImageFeatureF_FGcount : ImageFeatureBase() {
//        init {
//            this.description = "Pixelanzahl"
//        }
//
//        override fun CalcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
//            return (-1).toDouble() //TODO implementation required
//        }
//    }
//
//    internal inner class ImageFeatureF_MaxDistX : ImageFeatureBase() {
//        init {
//            this.description = "maximale Ausdehnung in X-Richtung"
//        }
//
//        override fun CalcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
//            return (-1).toDouble() //TODO implementation required
//        }
//    }
//
//    internal inner class ImageFeatureF_MaxDistY : ImageFeatureBase() {
//        init {
//            this.description = "maximale Ausdehnung in Y-Richtung"
//        }
//
//        override fun CalcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
//            return (-1).toDouble() //TODO implementation required
//        }
//    }
//
//    internal inner class ImageFeatureF_AvgDistanceCentroide : ImageFeatureBase() {
//        init {
//            this.description = "mittlere Distanz zum Centroide"
//        }
//
//        override fun CalcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
//            return (-1).toDouble() //TODO implementation required
//        }
//    }
//
//    internal inner class ImageFeatureF_MaxDistanceCentroide : ImageFeatureBase() {
//        init {
//            this.description = "maximale Distanz zum Centroide"
//        }
//
//        override fun CalcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
//            return (-1).toDouble() //TODO implementation required
//        }
//    }
//
//    internal inner class ImageFeatureF_MinDistanceCentroide : ImageFeatureBase() {
//        init {
//            this.description = "minimale Distanz zum Centroide"
//        }
//
//        override fun CalcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
//            return (-1).toDouble() //TODO implementation required
//        }
//    }
//
//    internal inner class ImageFeatureF_Circularity : ImageFeatureBase() {
//        init {
//            this.description = "Circularit�t"
//        }
//
//        override fun CalcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
//            return (-1).toDouble() //TODO implementation required
//        }
//    }
//
//    internal inner class ImageFeatureF_CentroideRelPosX : ImageFeatureBase() {
//        init {
//            this.description = "relative x-Position des Centroide"
//        }
//
//        override fun CalcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
//            return (-1).toDouble() //TODO implementation required
//        }
//    }
//
//    internal inner class ImageFeatureF_CentroideRelPosY : ImageFeatureBase() {
//        init {
//            this.description = "relative y-Position des Centroide"
//        }
//
//        override fun CalcFeatureVal(imgRegion: SubImageRegion?, FG_val: Int): Double {
//            return (-1).toDouble() //TODO implementation required
//        }
//    }
//
//    companion object {
//        //-------- the defined features ----------------
//        var F_FGcount: Int = 0
//        var F_MaxDistX: Int = 1
//        var F_MaxDistY: Int = 2
//        var F_AvgDistanceCentroide: Int = 3
//        var F_MaxDistanceCentroide: Int = 4
//        var F_MinDistanceCentroide: Int = 5
//        var F_Circularity: Int = 6
//        var F_CentroideRelPosX: Int = 7
//        var F_CentroideRelPosY: Int = 8
//    }
//} //class OCRAnalysis