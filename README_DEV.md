# Lipid Droplets Characterization - Developer Documentation

This plugin is fully developed in Java as a Maven project, utilizing the SciJava framework. The development and runtime environments are constrained to **Java 8** and **Java 11** due to specific ImageJ dependency requirements.

## Build and Dependencies

The project uses `SciJava` version `42.0.0` as its **Maven** parent, and thus can be builded using the `mvn clean install` command.  
The core dependencies are configured as follows:
- `imagej` (2.17.0)
- `imglib2-ij` (2.0.3)
- `imagej-legacy` (2.0.2)
- `slf4j-simple` (1.7.36)
- `junit-jupiter` (5.10.2) - *For unit and integration testing*
- `gson` (2.10.1) - *For JSON parameter serialization*

Except for JUnit, all dependency versions are managed automatically by the SciJava parent POM.

## Tests and Continuous Integration

The project integrates Continuous Integration via **GitHub Actions**, which automatically runs the full JUnit test suite on every push or pull request across both Java 8 and Java 11 environments.

For a detailed breakdown of our testing methodology (including unit, integration, and JSON test cases), please refer to the dedicated [Tests README](https://github.com/FattaccioliLab/lipid-droplets-characterization/blob/main/src/test/java/fr/sorbonne_universite/ldc/tests/unit/TESTS_README.md).

## Development Workflow

To avoid building and packaging the `.jar` file manually after every minor code modification, you can execute the `TestPlugin.java` class located in the `fr.sorbonne_universite.ldc.tests` package. Running this class as a standard Java application will immediately launch a local ImageJ instance preloaded with your modified plugin, allowing for rapid testing and debugging.

## Code Architecture and Design

Below is the UML class diagram representing the overall structural design of the project. For readability purposes, only the most critical relationships and classes are depicted.

<img width="1949" height="761" alt="UML" src="https://github.com/user-attachments/assets/8b830861-2020-49d7-9ba2-faf0250b3448" />

The project structure is split into two secondary packages and two core packages to ensure a strict separation of concerns:

### Secondary Packages
- `commands`: Contains the single command class (`PluginCommandLDC`) that binds the plugin to the ImageJ/Fiji menu hierarchy.
- `utils`: Hosts utility classes (`InputUtils`, `PanelUtils`) shared across various UI components.

### Core Packages
- `model`: Acts as the backend of the application. It encapsulates all image processing logic, data structures, and state management.
- `ui`: Handles the entire graphical user interface, built primarily on top of Java Swing components (`JPanel`).

---

## Service Part (Model Architecture)

The core architectural choice of the plugin is the implementation of a dedicated **SciJava service**. The service lifecycle and accessibility are governed by the `LDCService` interface, which extends `SciJavaService`, and its implementation class `LDCServiceImpl`.

### 1. State Persistence and Delegation
Unlike standard volatile execution logic, the `LDCServiceImpl` instance is globally managed by the SciJava framework and acts as a central stateful container:
- **Global State Management**: The service maintains the persistent configuration state of the entire image processing pipeline via an internal `AnalysisSettings` attribute. Every user interaction that adjusts a parameter immediately updates this central state object.
- **Strict Delegation**: The service serves as an API gateway. It does not perform calculation logic itself; it systematically delegates data access to `AnalysisSettings`, and dispatches image processing routines to specialized managers inside the `model.leftpanel` or `model.rightpanel` sub-packages (e.g., `PreprocessingManager`, `ThresholdingManager`, `MeasurementsManager`, `JSONManager`).

### 2. Asynchronous Execution Framework (`model.workers`)
To keep the graphical user interface responsive during heavy computational workloads, all intensive image processing operations are decoupled from the Event Dispatch Thread (EDT):
- Any computation-heavy request dispatched to the service prompts the corresponding manager to instantiate a dedicated worker class extending `SwingWorker` (e.g., `PreprocessingApplyMedianWorker`, `MeasuresProcessingWorker`).
- This worker is returned back to the UI tier, which triggers its execution in the background. This ensures safe, asynchronous pixel calculations and seamless updates to the UI upon thread completion without locking the interface.
- Ultimately, these workers execute low-level routines by directly invoking core, unexposed APIs from the native ImageJ library.

---

## UI Part (Graphical Interface Architecture)

The user interface layer is built entirely on the Java Swing framework and is designed around a strict decoupled pattern where no image processing arithmetic or state mutation happens natively inside the view components.

### 1. Synchronization and Monolithic Image Handling
- **Service Dependency**: UI panels are strictly consumer components. They interact exclusively with the backend by calling the exposed methods of the `LDCService` gateway.
- **Single-Image Workflow Constraint**: To maintain strict coherence between the UI controls and the asynchronous execution threads, the pipeline is strictly restricted to manipulating a maximum of **one active image** at a time inside `MainGUI_LDC`. Processing a different file requires replacing or explicitly closing the current image instance.
- **Sequential Pipeline Wizard**: The user is forced to progress through the image processing workflow step-by-step. This constraint avoids complex state rollbacks that would occur if preceding preprocessing parameters were altered after generating down-stream binary masks.

### 2. Panel Component Interfaces
To enforce behavioral uniformity across the various steps of the Left Panel pipeline, two core layout interfaces are implemented:
- `LeftPanelSubPanel`: Forces sub-panels to implement a standard method to safely enable or disable all their internal input fields (text boxes, sliders, checkboxes) depending on the context of the active step or background worker activity.
- `PipelineSubPanel`: Extends the previous interface capabilities by forcing pipeline-participating panels to implement input reset mechanisms, as well as hooks for parameter loading. This allows the panel to automatically synchronize its visual components whenever a new preset JSON configuration file is imported into the service.

### 3. Dynamic Image Polling Mechanism
A major technical challenge addressed in the UI tier is handling instances where the user closes or switches the active image window natively inside Fiji, completely bypassing our custom plugin buttons:
- Since ImageJ does not naturally expose a window lifecycle event listener for third-party plug-ins, a **polling approach via a background timer** is implemented inside `ImageSourcePanel`.
- This timer periodically interrogates the runtime environment of ImageJ. If it detects that the target image window has been manually closed by the user, it immediately triggers a cascading user interface reset. This propagates a complete input clear command to all left-hand sub-panels, which in turn wipes the state clean within the persistent backend `LDCService`.
