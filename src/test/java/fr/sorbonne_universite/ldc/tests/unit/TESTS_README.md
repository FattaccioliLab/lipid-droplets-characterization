# Test explanations

To ensure that our plugin pipeline produces the same results as using ImageJ tools through their interface, we created tests to verify the correctness of our plugin results by comparing them to ImageJ's ones.

The idea is simple. For a given test:
- We applied some treatments related to the testing field through the ImageJ interface on an original image.
- We saved the resulting image. These resulting images are kept in the `src/test/resources/expected` sub-folders.

Then, by executing the corresponding JUnit test:
- It uses our Lipid Droplets Characterization API to apply the same treatments on the original image as done through the ImageJ interface.
- It compares the expected resulting image and the image obtained by our API, and verifies that the content is the same.

# Test configurations

All tests have been done in April 2026.  
The following versions are automatically resolved by Maven via the `pom-scijava 42.0.0` parent POM, and have been used for the plugin and its test development:
- pom-scijava 42.0.0
- imagej 2.17.0
- imglib2-ij 2.0.3
- imagej-legacy 2.0.2
- slf4j-simple 1.7.36
- junit-jupiter 5.10.2

We have also used either Java 11 or Java 8. Both are compatible with our plugin development.  
(Once in a production environment (Fiji), there are no compatibility problems with more recent Java versions)

## TestPreprocessing.java

Only the preprocessing part of our plugin is individually tested.  
Original image used before pretreatments : `src/test/resources/TestSample.tif`.  

### test1

Through ImageJ :
- Process > Enhance Contrast : Saturated pixels 0,35%  
-> resulting image : `.../expected/test_preprocessing/test1.tif`

### test2

Through ImageJ :
- Process > Enhance Contrast : Saturated pixels 10%  
-> resulting image : `.../expected/test_preprocessing/test2.tif`  

### test3

Through ImageJ :
- Process > Enhance Contrast : Saturated pixels 40%  
-> resulting image : `.../expected/test_preprocessing/test3.tif`  

### test4

Through ImageJ :
- Process > Filters > Median... : Radius 0 pixels + process all 3 slices  
-> resulting image : `.../expected/test_preprocessing/test4.tif`  

### test5

Through ImageJ :
- Process > Filters > Median... : Radius 2 pixels + process all 3 slices  
-> resulting image : `.../expected/test_preprocessing/test5.tif`  

### test6

Through ImageJ :
- Process > Filters > Median... : Radius 20 pixels + process all 3 slices  
-> resulting image : `.../expected/test_preprocessing/test6.tif`  

### test7

Through ImageJ :
- Process > Enhance Contrast : Saturated pixels 10%  
- Process > Filters > Median... : Radius 5 pixels + process all 3 slices  
-> resulting image : `.../expected/test_preprocessing/test7.tif`  

### test8

Through ImageJ :
- Process > Enhance Contrast : Saturated pixels 10%  
- Process > Filters > Median... : Radius 2.45 pixels + process all 3 slices  
-> resulting image : `.../expected/test_preprocessing/test8.tif`  

## TestSegmentation.java

Only the segmentation/thresholding part of our plugin is individually tested.  
Original image used before segmentation/thresholding : `src/test/resources/TestSample.tif`.  
Here we test binary masks got by the segmentation / thresholding.  

### test1

Through ImageJ :  
- Image > Adjust > Threshold ... : Default, min = 1048, max = 2576, no option selected  
-> resulting image : `.../expected/test_segmentation/test1.tif`  

### test2

Through ImageJ :  
- Image > Adjust > Threshold ... : Default, min = 430, max = 3731, no option selected  
-> resulting image : `.../expected/test_segmentation/test2.tif`  

### test3

Through ImageJ :  
- Process > Binary > Make Binary : method Otsu, Background Light, Black background selected  
-> resulting image : `.../expected/test_segmentation/test3.tif`  

### test4

Through ImageJ :  
- Process > Binary > Make Binary : method Otsu, Background Dark, Black background selected  
-> resulting image : `.../expected/test_segmentation/test4.tif`  

### test5

Through ImageJ :  
- Process > Binary > Make Binary : method Moments, Background Light, Black background selected  
-> resulting image : `.../expected/test_segmentation/test5.tif`  

### test6

Through ImageJ :  
- Process > Binary > Make Binary : method Moments, Background Dark, Black background selected  
-> resulting image : `.../expected/test_segmentation/test6.tif`  

### test7

Through ImageJ :  
- Process > Binary > Make Binary : method Triangle, Background Light, Black background selected  
-> resulting image : `.../expected/test_segmentation/test7.tif`  

### test8

Through ImageJ :  
- Process > Binary > Make Binary : method Triangle, Background Dark, Black background selected  
-> resulting image : `.../expected/test_segmentation/test8.tif`  

### test9

Through ImageJ :  
- Process > Binary > Make Binary : method Yen, Background Light, Black background selected  
-> resulting image : `.../expected/test_segmentation/test9.tif`  

### test10

Through ImageJ :  
- Process > Binary > Make Binary : method Yen, Background Dark, Black background selected  
-> resulting image : `.../expected/test_segmentation/test10.tif`  

### test11

Through ImageJ :  
- Process > Binary > Make Binary : method Li, Background Light, Black background selected  
-> resulting image : `.../expected/test_segmentation/test11.tif`  

### test12

Through ImageJ :  
- Process > Binary > Make Binary : method Li, Background Dark, Black background selected  
-> resulting image : `.../expected/test_segmentation/test12.tif`  

## TestBinaryMaskOperations.java

Only the binary mask operations of our plugin are individually tested.  
Original mask used before those operations : `src/test/resources/TestMask.tif`.  
(Which is in reality `.../expected/test_segmentation/test12.tif`)  

### test1

Through ImageJ :  
- Process > Binary > Erode (on all slices)  
-> resulting image : `.../expected/test_binary_operations/test1.tif`  

### test2

Through ImageJ :  
- Process > Binary > Dilate (on all slices)  
-> resulting image : `.../expected/test_binary_operations/test2.tif`  

### test3

Through ImageJ :  
- Process > Binary > Open (on all slices)  
-> resulting image : `.../expected/test_binary_operations/test3.tif`  

### test4

Through ImageJ :  
- Process > Binary > Close (on all slices)  
-> resulting image : `.../expected/test_binary_operations/test4.tif`  

### test5

Through ImageJ :  
- Process > Binary > Erode (on all slices)  
- Process > Binary > Open (on all slices)  
-> resulting image : `.../expected/test_binary_operations/test5.tif`  

### test6

Through ImageJ :  
- Process > Binary > Dilate (on all slices)  
- Process > Binary > Open (on all slices)  
- Process > Binary > Close (on all slices)  
-> resulting image : `.../expected/test_binary_operations/test6.tif`  

## TestPipeline.java

We successively apply the different previous steps, and verify that the same resulting image, mask and csv results are got with our plugin.  
Original image used before applying the pipeline: `src/test/resources/TestSample.tif`.  

### test1

Through ImageJ :
- Process > Filters > Median... : Radius 2 pixels + process all 3 slices  
- Process > Binary > Make Binary : method Moments, Background Dark, Black background selected + Create new stack  
- Analyze > Set Measurements ... : Only Area, Shape descriptors, Integrated density, Mean gray value, Centroid, Median selected. Redirect to : None, decimal places : 4  
- Analyze > Analyze particles ... : Size : 0 - Infinity (pixel units selected). Circularity : 0 - 1. Show : Nothing. Only Display results, Exclude on edges selected + process all 3 slices   
-> resulting image : `.../expected/test_pipeline/test1_res.tif`  
-> resulting mask : `.../expected/test_pipeline/test1_mask.tif`  
-> resulting csv : `.../expected/test_pipeline/test1_table.csv`  

### test2

Through ImageJ :
- Process > Enhance Contrast : Saturated pixels 0,35%  
- Process > Filters > Median... : Radius 4 pixels + process all 3 slices  
- Process > Binary > Make Binary : method Triangle, Background Dark, Black background selected + Create new stack  
- Analyze > Set Measurements ... : Only Area, Shape descriptors, Integrated density, Mean gray value, Centroid, Median selected. Redirect to : None, decimal places : 4  
- Analyze > Analyze particles ... : Size : 0 - Infinity (pixel units selected). Circularity : 0 - 0.8. Show : Nothing. Only Display results, Exclude on edges selected + process all 3 slices   
-> resulting image : `.../expected/test_pipeline/test2_res.tif`  
-> resulting mask : `.../expected/test_pipeline/test2_mask.tif`  
-> resulting csv : `.../expected/test_pipeline/test2_table.csv`  

### test3

Through ImageJ :
- Process > Enhance Contrast : Saturated pixels 4%  
- Process > Filters > Median... : Radius 2 pixels + process all 3 slices  
- Process > Binary > Make Binary : method Otsu, Background Dark, Black background selected + Create new stack  
- Process > Binary > Erode (on all slices)  
- Analyze > Set Measurements ... : Only Area, Shape descriptors, Integrated density, Mean gray value, Centroid, Median selected. Redirect to : None, decimal places : 4  
- Analyze > Analyze particles ... : Size : 1 - Infinity (pixel units selected). Circularity : 0 - 1. Show : Nothing. Only Display results selected + process all 3 slices   
-> resulting image : `.../expected/test_pipeline/test3_res.tif`  
-> resulting mask : `.../expected/test_pipeline/test3_mask.tif`  
-> resulting csv : `.../expected/test_pipeline/test3_table.csv`  