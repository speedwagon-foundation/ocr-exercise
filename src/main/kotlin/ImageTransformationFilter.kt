package hofwimmer.lukas

object ImageTransformationFilter {
    /**
     * apply scalar transformation
     *
     * @param inImg
     * @param width
     * @param height
     * @param transferFunction
     * @return
     */
    fun getTransformedImage(
        inImg: Array<IntArray?>?,
        width: Int,
        height: Int,
        transferFunction: IntArray?
    ): Array<IntArray> {
        val returnImg = Array(width) { IntArray(height) }


        //TODO implementation required
        return returnImg
    }

    /**
     * get transfer function for contrast inversion
     *
     * @param maxVal
     * @return
     */
    fun getInversionTF(maxVal: Int): IntArray {
        val transferFunction = IntArray(maxVal + 1)


        //TODO implementation required
        return transferFunction
    }
}