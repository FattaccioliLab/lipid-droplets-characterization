package fr.sorbonne_universite.ldc.model.leftpanel;
 
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
 
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
 
import fr.sorbonne_universite.ldc.model.AnalysisSettings;

/**
 * Handles {@link AnalysisSettings} export and import as JSON.
 */
public class JSONManager {

    /**
     * Save {@code settings} into a file (.json) specified by the {@code outputPath}.
     * @param outputPath 					The path where {@code settings} needs to be saved.
     * @param settings						The LDC plug-in settings to save.
     * @throws IllegalArgumentException		If the given {@code outputPath} does not end with a '.json'.
     * @throws IOException					If an error occurs while attempting to save the file.
     */
	public void saveAnalysis(String outputPath, AnalysisSettings settings) throws IOException {
		if (!outputPath.endsWith(".json")) {
			throw new IllegalArgumentException("Output path must end with '.json'");
		}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String json = gson.toJson(settings);
		
		try (FileWriter writer = new FileWriter(outputPath)) {
			writer.write(json);
		}
	}
	
    /**
     * Load a file (.json) specified by the {@code inputPath}, and returns the {@link AnalysisSettings} represented by it.
     * @param inputPath 					The path from where the new {@link AnalysisSettings} needs to be loaded.
     * @return 								The loaded {@link AnalysisSettings}.
     * @throws IllegalArgumentException		If the given {@code inputPath} does not end with a '.json'.
     * @throws IOException					If an error occurs while attempting to load the file.
     */
	public AnalysisSettings loadAnalysis(String inputPath) throws IOException {
		if (!inputPath.endsWith(".json")) {
			throw new IllegalArgumentException("Input path must end with '.json'");
		}
		
		Gson gson = new Gson();
		
		try (FileReader reader = new FileReader(inputPath)) {
			return gson.fromJson(reader, AnalysisSettings.class);
		}
	}
	
}
