# Unit test explanations

To ensure that our plugin pipeline produces the same results as using ImageJ tools through their interface, we created unit tests to verify the correctness of our plugin results by comparing them to ImageJ's ones.

The idea is simple. For a given test:
- We applied some treatments related to the testing field through the ImageJ interface on an original image.
- We saved the resulting image. These resulting images are kept in the `src/test/resources/expected` sub-folders.

Then, by executing the corresponding JUnit test:
- It uses our Lipid Droplets Characterization API to apply the same treatments on the original image as done through the ImageJ interface.
- It compares the expected resulting image and the image obtained by our API, and verifies that the content is the same.

# Test configurations

All tests have been done in April 2026.  
The following versions are automatically resolved by Maven via the `pom-scijava 42.0.0` parent POM, and have been used for the plugin and its test development:
- SciJava 42.0.0
- ImageJ 2.17.0
- imglib2-ij 2.0.3
- imagej-legacy 2.0.2
- slf4j-simple 1.7.36
- JUnit 5.10.2

We have also used either Java JDK 11.0.2 or Java JRE 8. Both are compatible with our plugin development.  
(Once in a production environment (Fiji), there are no compatibility problems with more recent Java versions)

## TestPreprocessing.java

Only the preprocessing part of our plugin is individually tested.  
Original image used before treatments : `src/test/resources/TestSample.tif`.  

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
- Process > Filters > Median... : Radius 0 pixels + process all 10 slices  
-> resulting image : `.../expected/test_preprocessing/test4.tif`  

### test5

Through ImageJ :
- Process > Filters > Median... : Radius 2 pixels + process all 10 slices  
-> resulting image : `.../expected/test_preprocessing/test5.tif`  

### test6

Through ImageJ :
- Process > Filters > Median... : Radius 20 pixels + process all 10 slices  
-> resulting image : `.../expected/test_preprocessing/test6.tif`  

### test7

Through ImageJ :
- Process > Filters > Median... : Radius 2 pixels + only slice 1  
-> resulting image : `.../expected/test_preprocessing/test7.tif`

### test8

Through ImageJ :
- Process > Filters > Median... : Radius 4 pixels + only slice 5  
-> resulting image : `.../expected/test_preprocessing/test8.tif`  

### test9

Through ImageJ :
- Process > Enhance Contrast : Saturated pixels 0,35%  
- Process > Filters > Median... : Radius 10 pixels + only slice 3  
-> resulting image : `.../expected/test_preprocessing/test9.tif`  

### test10

Through ImageJ :
- Process > Enhance Contrast : Saturated pixels 10%  
- Process > Filters > Median... : Radius 5 pixels + process all 10 slices  
-> resulting image : `.../expected/test_preprocessing/test10.tif`  