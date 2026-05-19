# Lipid Droplets Characterization

An ImageJ/Fiji plugin for the morphological and photometric characterization of spherical microparticles from optical or confocal microscopy images. This plugin offers a graphical interface that allows users to apply a complete image processing pipeline.

## Download and Installation

You can download the latest version of the plugin's `.jar` file from the [releases page](https://github.com/FattaccioliLab/lipid-droplets-characterization/releases).

To install it, simply place the `.jar` file into the **plugins** folder of your Fiji directory. If installed correctly, you will be able to find **Lipid Droplets Characterization** at the bottom of the Plugins menu in Fiji.

## Contributing

If you want to contribute to the project, please consider reading the [developer README](https://github.com/FattaccioliLab/lipid-droplets-characterization/blob/documentation/README_DEV.md).

## Credits

**David VADIMON** \
**Abdullah AL MAMUN** \
**Yahya MUDALLAL** 
 
## License

This project is licensed under the GPL-3.0 license.

## Explanation of the plugin's usage

The following section explains how to use the plugin and describes its features.
This is how the overall UI presents itself.

<img width="1919" height="1020" alt="overall" src="https://github.com/user-attachments/assets/89f5cc81-fa39-4739-be38-b01dee546169" />

### Preprocessing

<img width="575" height="276" alt="preprocessing" src="https://github.com/user-attachments/assets/d08328a7-a4cb-4a57-a598-4449999dff00" />

The preprocessing section of the pipeline allows you to apply preliminary treatments to the images. The available preprocessing steps are the following:
- **Enhance contrast**: This is just a visual tool; it does not affect pixel values.
- **Median filter**: Applies a median filter to the image with the specified radius. The possibility to preview the filter's effect is offered by checking the Preview checkbox. You can also apply the median filter to a specific range of slices in an image stack by defining the range in the format shown in the interface.

### Thresholding

<img width="568" height="374" alt="thresholding" src="https://github.com/user-attachments/assets/9cf1ddfb-66d4-45c9-b565-a9eba07d08bd" />

In the thresholding section, you must create a binary mask of the image by applying a threshold. The threshold can either be set manually or determined automatically using one of the following methods: **Otsu**, **Moments**, **Triangle**, **Yen**, or **Li**. If you choose an automatic method, make sure to check the **Dark Background** option if the background of your image is dark.

### Operations on the binary mask

<img width="575" height="340" alt="binary_mask_operations" src="https://github.com/user-attachments/assets/60e1b9a2-eada-468d-8bea-bce8f05423d7" />

After generating the binary mask, you can refine it by applying some modifications. You can choose one morphological operation among the following:
- **Erosion**: Shrinks the objects.
- **Dilation**: Expands the objects.
- **Opening**: Removes small noise/objects.
- **Closing**: Fills small holes.

You can also apply the **Watershed** algorithm, which separates touching objects. 
These modifications can be previewed before being definitively applied by checking the Preview checkbox.

### Particle analysis

<img width="1919" height="1019" alt="particle_analysis" src="https://github.com/user-attachments/assets/762126d4-6773-4e87-b3e6-bf9c326b74c2" />

This section allows the user to configure the parameters for the particle analysis. The parameters to configure are the following:

**Calibration settings**

In this section, you can calibrate the image by defining the unit and the ratio (unit/px) of the calibration. By default, this is set to the calibration found in the image's metadata, if available.

**Particle settings**

In this section, you can define a minimum and maximum **Size** and **Circularity** for the particles you want to keep in the final results.
You can also choose whether to exclude particles on the edges of the image or to include holes. 
Additionally, you can set the circularity threshold to determine if a particle is isolated: if a particle does not touch the edges and has a circularity above the threshold, it will be considered isolated.

**Measurements**

Here, you can check the box for each property you want to measure from the particles in the final results. The available measurements are:
- **Area**: The area of the particles in px² or unit², depending on whether the image is calibrated.
- **Diameter**: The diameter of the particles in px or unit, depending on whether the image is calibrated.
- **Median**: The median intensity of the particles.
- **Mean**: The mean intensity of the particles.
- **Integrated density**: The integrated density of the particles.
- **Circularity**: The circularity of the particles.

The **Show results with default calibration unit** checkbox can only be selected when **Calibrate the image** is unchecked. If selected, the results will be displayed using the original calibration from the image's metadata, if available.

### Generating results

After completing the processing pipeline, you can proceed to the particle analysis to generate your data using the tools available on the right panel of the UI.

**Preview** 

<img width="657" height="736" alt="outlines_window" src="https://github.com/user-attachments/assets/818dfc8f-60ae-44e7-bc17-d43ec1435d71" />

The **Preview** button opens a new window showing the outlines of the particles detected based on your current analysis parameters.

**Generate results**

<img width="1919" height="1020" alt="data" src="https://github.com/user-attachments/assets/1c86dd52-d21a-4848-9ef5-04e373f45b0f" />

This button runs the full analysis and displays the raw data for each analyzed particle. In this results table, each row represents a single particle. It includes its specific properties such as X and Y coordinates, the slice number it belongs to, and the **is_isolated** indicator (which equals `1.0` if the particle is isolated and `0.0` otherwise), alongside all the measurements selected in the parameters section.

**Histograms**

<img width="950" height="631" alt="historgram" src="https://github.com/user-attachments/assets/a1a2800a-6c71-4f73-8307-9b6ce43efc95" />

After generating the results, you can display frequency distributions for your measured properties by clicking the **Histograms** button. You can double-click on any histogram to open it in a separate window and save it using the standard Fiji menu.

**Statistics**

<img width="752" height="994" alt="statistics" src="https://github.com/user-attachments/assets/27454aac-058b-4e50-b5ce-9ee1281ec68d" />

Clicking the **Statistics** button computes the overall statistical metrics for your data. For each property, it generates the Mean, Median, Standard Deviation (SD), Coefficient of Variation (CV), Minimum, and Maximum values. The table displays these metrics globally for all slices combined, followed by a detailed breakdown for each individual slice.

**Export**

The **Export** button allows you to save the currently displayed table (either the raw results or the statistics) as a CSV file, maintaining the exact format shown in the UI. Therefore, if you want to export the raw particle data, ensure the results table is active by clicking **Generate results** beforehand; likewise, click **Statistics** before exporting if you wish to save the statistical summary.

**Batch mode**

<img width="1919" height="1018" alt="batch_window" src="https://github.com/user-attachments/assets/cb113b1f-87b4-4eb4-a756-bddc203c95a3" />

By clicking the **Batch mode** button, you can process multiple image files simultaneously. A dialog will prompt you to select an input directory containing your images and an output destination for the final global CSV file.

<img width="1118" height="362" alt="batch_mode_csv_format" src="https://github.com/user-attachments/assets/5dbfe872-4556-43e5-8e0d-0fcae4a65359" />

The output CSV file from the batch mode combines global and individual data. The top rows present the aggregated statistics across all processed images, while the subsequent rows list every single detected particle with its metrics and the filename of the image it was extracted from.

### Parameter Export/Import

The **Image Source** section at the top of the interface provides option buttons to import or export your pipeline configurations.

- **Export parameters**: Saves your current configuration into a JSON file. If you haven't completed the entire pipeline, default values will automatically be assigned to the unconfigured steps.
- **Import parameters**: Loads an existing JSON parameters file. Upon importing, a dialog will ask you to select a specific target step in the workflow up to which the parameters should be automatically applied.

<img width="340" height="191" alt="import_parameters" src="https://github.com/user-attachments/assets/f228d5db-d347-438b-8538-b52983a95f82" />
