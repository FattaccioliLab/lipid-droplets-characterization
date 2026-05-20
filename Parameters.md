# Parameters description

## Preprocessing

| Parameter | Role | Recommended values |
|------------|------|-------------------|
| Enhance contrast (%) | Improves visual contrast without altering pixel values | 0.1–0.5% (visual adjustment only, no effect on analysis) |
| Median filter (px) | Reduces noise using a local median smoothing | 2–3 (higher values for noisier images) |

## Thresholding

| Parameter | Role | Recommended values |
|------------|------|-------------------|
| Threshold method | Sets predefined intensity cutoff pixel values | User preference among: Otsu / Moments / Triangle / Yen / Li / Manual |
| Manual threshold | Sets user selected intensity cutoff pixel values | Depends on image histogram |
| Dark background | Adapts thresholding for inverted intensity images | **On** for images with dark background, otherwise **Off** |

## Binary mask operations

| Parameter | Role | Recommended values |
|------------|------|-------------------|
| Erosion | Shrinks foreground objects | **Off** (use only if particles appear artificially enlarged) |
| Dilation | Expands foreground objects | **Off** (use only if particles appear fragmented) |
| Opening | Removes small objects/noise | **On** (recommended to remove noise after thresholding) |
| Closing | Fills small holes in objects | **On** (recommended to fill small internal gaps) |
| Watershed | Separates touching particles | **On**, when particles touch or overlap |

## Particle analysis

### Calibration

| Parameter | Role | Recommended values |
|------------|------|-------------------|
| Calibrate the image | Calibrates the image using selected settings. If Off it is 1px = 1µm | **On** |
| Calibration unit | Defines measurement unit displayed in generated results | µm |
| Pixel size ratio (unit/px) | Converts pixels to physical scale | **Don't change anything**, by default got from image metadata |

### Particle settings

| Parameter | Role | Recommended values |
|------------|------|-------------------|
| Min size | Filters out small particles | 0.5–2 µm² (adjust based on expected particle size) |
| Max size | Filters out large particles | 50–200 µm² (set to infinity if no upper limit needed) |
| Circularity range | Filters irregular shapes | 0.3–0.8 (lower for irregular particles, higher for spheres) |
| Exclude edges | Removes border-touching particles | **On** (recommended for unbiased measurements) |
| Include holes | Keeps internal voids in objects | **Off** (unless holes are biologically relevant) |
| Isolation threshold | Defines isolation criterion based on circularity | 0.7–0.95 (higher values = stricter isolation criterion) |

### Measurements

| Parameter | Role | Recommended values |
|------------|------|-------------------|
| Area | Measures particle surface | **On** |
| Diameter | Estimates equivalent diameter | **On** (useful for size distribution) |
| Mean intensity | Average pixel intensity | **On** |
| Median intensity | Median pixel intensity | **On** (less sensitive to outliers than mean) |
| Integrated density | Total intensity per particle | **On** (useful for quantifying total signal) |
| Circularity | Shape compactness metric | **On** if isolation threshold criterion is actively useful, otherwise **Off** |
