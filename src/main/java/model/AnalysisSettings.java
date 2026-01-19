package model;

/**
 * Class that attribute are the setting chosen by the user for the image processing.
 */
public class AnalysisSettings {

	private boolean calibrate = false;
	private double calibration; // μm per pixel
	
	// pre-treatment 
	private boolean enhanceContraste = false;
	private boolean medianFilter = false;
	private boolean gausianFilter = false;
	private boolean backgroundSubstraction = false;
	private boolean darkBackgroud = false;
	
	// thresholding
	// manual
	private boolean manualThresholding = false;
	private boolean threasholdValue = false; // set by user if manualThresholding is true
	
	// automatic
	private boolean otsuThreshold = false;
	private boolean momentsThreshold = false;
	private boolean triangleThreshold = false;
	private boolean yenLiThreshold = false;
	
	// slice vs global
	private boolean globalThreshold = false;
	
	// binary mask
	// morphological operations
	private boolean erosion = false;
	private boolean dilation = false;
	private boolean opening = false;
	private boolean closing = false;
	
	private boolean watershed = false; // bonus
	
	// measures
	private boolean area = false;
	private boolean equivalentDiameter = false;
	private boolean feret = false;
	private boolean circularity = false;
	private boolean mean = false;
	private boolean stdDev = false;
	private boolean integratedDensity;
	private boolean median = false;
	private boolean min = false;
	private boolean max = false;
	
	/**
	 * @return the calibrate
	 */
	public boolean isCalibrate() {
		return calibrate;
	}
	/**
	 * @param calibrate the calibrate to set
	 */
	public void setCalibrate(boolean calibrate) {
		this.calibrate = calibrate;
	}
	/**
	 * @return the calibration
	 */
	public double getCalibration() {
		return calibration;
	}
	/**
	 * @param calibration the calibration to set
	 */
	public void setCalibration(double calibration) {
		this.calibration = calibration;
	}
	/**
	 * @return the enhanceContraste
	 */
	public boolean isEnhanceContraste() {
		return enhanceContraste;
	}
	/**
	 * @param enhanceContraste the enhanceContraste to set
	 */
	public void setEnhanceContraste(boolean enhanceContraste) {
		this.enhanceContraste = enhanceContraste;
	}
	/**
	 * @return the medianFilter
	 */
	public boolean isMedianFilter() {
		return medianFilter;
	}
	/**
	 * @param medianFilter the medianFilter to set
	 */
	public void setMedianFilter(boolean medianFilter) {
		this.medianFilter = medianFilter;
	}
	/**
	 * @return the gausianFilter
	 */
	public boolean isGausianFilter() {
		return gausianFilter;
	}
	/**
	 * @param gausianFilter the gausianFilter to set
	 */
	public void setGausianFilter(boolean gausianFilter) {
		this.gausianFilter = gausianFilter;
	}
	/**
	 * @return the backgroundSubstraction
	 */
	public boolean isBackgroundSubstraction() {
		return backgroundSubstraction;
	}
	/**
	 * @param backgroundSubstraction the backgroundSubstraction to set
	 */
	public void setBackgroundSubstraction(boolean backgroundSubstraction) {
		this.backgroundSubstraction = backgroundSubstraction;
	}
	/**
	 * @return the darkBackgroud
	 */
	public boolean isDarkBackgroud() {
		return darkBackgroud;
	}
	/**
	 * @param darkBackgroud the darkBackgroud to set
	 */
	public void setDarkBackgroud(boolean darkBackgroud) {
		this.darkBackgroud = darkBackgroud;
	}
	/**
	 * @return the threasholdValue
	 */
	public boolean isThreasholdValue() {
		return threasholdValue;
	}
	/**
	 * @param threasholdValue the threasholdValue to set
	 */
	public void setThreasholdValue(boolean threasholdValue) {
		this.threasholdValue = threasholdValue;
	}
	/**
	 * @return the otsuThreshold
	 */
	public boolean isOtsuThreshold() {
		return otsuThreshold;
	}
	/**
	 * @param otsuThreshold the otsuThreshold to set
	 */
	public void setOtsuThreshold(boolean otsuThreshold) {
		this.otsuThreshold = otsuThreshold;
	}
	/**
	 * @return the momentsThreshold
	 */
	public boolean isMomentsThreshold() {
		return momentsThreshold;
	}
	/**
	 * @param momentsThreshold the momentsThreshold to set
	 */
	public void setMomentsThreshold(boolean momentsThreshold) {
		this.momentsThreshold = momentsThreshold;
	}
	/**
	 * @return the triangleThreshold
	 */
	public boolean isTriangleThreshold() {
		return triangleThreshold;
	}
	/**
	 * @param triangleThreshold the triangleThreshold to set
	 */
	public void setTriangleThreshold(boolean triangleThreshold) {
		this.triangleThreshold = triangleThreshold;
	}
	/**
	 * @return the yenLiThreshold
	 */
	public boolean isYenLiThreshold() {
		return yenLiThreshold;
	}
	/**
	 * @param yenLiThreshold the yenLiThreshold to set
	 */
	public void setYenLiThreshold(boolean yenLiThreshold) {
		this.yenLiThreshold = yenLiThreshold;
	}
	/**
	 * @return the globalThreshold
	 */
	public boolean isGlobalThreshold() {
		return globalThreshold;
	}
	/**
	 * @param globalThreshold the globalThreshold to set
	 */
	public void setGlobalThreshold(boolean globalThreshold) {
		this.globalThreshold = globalThreshold;
	}
	/**
	 * @return the erosion
	 */
	public boolean isErosion() {
		return erosion;
	}
	/**
	 * @param erosion the erosion to set
	 */
	public void setErosion(boolean erosion) {
		this.erosion = erosion;
	}
	/**
	 * @return the dilation
	 */
	public boolean isDilation() {
		return dilation;
	}
	/**
	 * @param dilation the dilation to set
	 */
	public void setDilation(boolean dilation) {
		this.dilation = dilation;
	}
	/**
	 * @return the opening
	 */
	public boolean isOpening() {
		return opening;
	}
	/**
	 * @param opening the opening to set
	 */
	public void setOpening(boolean opening) {
		this.opening = opening;
	}
	/**
	 * @return the closing
	 */
	public boolean isClosing() {
		return closing;
	}
	/**
	 * @param closing the closing to set
	 */
	public void setClosing(boolean closing) {
		this.closing = closing;
	}
	/**
	 * @return the watershed
	 */
	public boolean isWatershed() {
		return watershed;
	}
	/**
	 * @param watershed the watershed to set
	 */
	public void setWatershed(boolean watershed) {
		this.watershed = watershed;
	}
	/**
	 * @return the area
	 */
	public boolean isArea() {
		return area;
	}
	/**
	 * @param area the area to set
	 */
	public void setArea(boolean area) {
		this.area = area;
	}
	/**
	 * @return the equivalentDiameter
	 */
	public boolean isEquivalentDiameter() {
		return equivalentDiameter;
	}
	/**
	 * @param equivalentDiameter the equivalentDiameter to set
	 */
	public void setEquivalentDiameter(boolean equivalentDiameter) {
		this.equivalentDiameter = equivalentDiameter;
	}
	/**
	 * @return the feret
	 */
	public boolean isFeret() {
		return feret;
	}
	/**
	 * @param feret the feret to set
	 */
	public void setFeret(boolean feret) {
		this.feret = feret;
	}
	/**
	 * @return the circularity
	 */
	public boolean isCircularity() {
		return circularity;
	}
	/**
	 * @param circularity the circularity to set
	 */
	public void setCircularity(boolean circularity) {
		this.circularity = circularity;
	}
	/**
	 * @return the manualThresholding
	 */
	public boolean isManualThresholding() {
		return manualThresholding;
	}
	/**
	 * @param manualThresholding the manualThresholding to set
	 */
	public void setManualThresholding(boolean manualThresholding) {
		this.manualThresholding = manualThresholding;
	}
	/**
	 * @return the mean
	 */
	public boolean isMean() {
		return mean;
	}
	/**
	 * @param mean the mean to set
	 */
	public void setMean(boolean mean) {
		this.mean = mean;
	}
	/**
	 * @return the stdDev
	 */
	public boolean isStdDev() {
		return stdDev;
	}
	/**
	 * @param stdDev the stdDev to set
	 */
	public void setStdDev(boolean stdDev) {
		this.stdDev = stdDev;
	}
	/**
	 * @return the integratedDensity
	 */
	public boolean isIntegratedDensity() {
		return integratedDensity;
	}
	/**
	 * @param integratedDensity the integratedDensity to set
	 */
	public void setIntegratedDensity(boolean integratedDensity) {
		this.integratedDensity = integratedDensity;
	}
	/**
	 * @return the median
	 */
	public boolean isMedian() {
		return median;
	}
	/**
	 * @param median the median to set
	 */
	public void setMedian(boolean median) {
		this.median = median;
	}
	/**
	 * @return the min
	 */
	public boolean isMin() {
		return min;
	}
	/**
	 * @param min the min to set
	 */
	public void setMin(boolean min) {
		this.min = min;
	}
	/**
	 * @return the max
	 */
	public boolean isMax() {
		return max;
	}
	/**
	 * @param max the max to set
	 */
	public void setMax(boolean max) {
		this.max = max;
	}
}
