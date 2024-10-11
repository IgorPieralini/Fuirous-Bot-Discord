package aceita;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class ConfigManager {

    private Map<String, Object> configYML;
    private String FilePath;

    public ConfigManager(String filePath) {
        loadConfig(filePath);
        FilePath = filePath;
    }

    private void loadConfig(String filePath) {
        Yaml yaml = new Yaml();
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            configYML = yaml.load(inputStream);
        } catch (IOException e) {
            System.out.println("Error");
            System.out.println(e.getMessage());
        }
    }

    public String get(String path) {
        String[] keys = path.split("\\.");
        Map<String, Object> currentMap = configYML;
        for (int i = 0; i < keys.length - 1; i++) {
            currentMap = (Map<String, Object>) currentMap.get(keys[i]);
        }
        return (String) currentMap.get(keys[keys.length - 1]);
    }

    public Integer getInt(String path) {
        String[] keys = path.split("\\.");
        Map<String, Object> currentMap = configYML;
        for (int i = 0; i < keys.length - 1; i++) {
            currentMap = (Map<String, Object>) currentMap.get(keys[i]);
        }
        return (Integer) currentMap.get(keys[keys.length - 1]);
    }

    public void set(String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> currentMap = configYML;
        for (int i = 0; i < keys.length - 1; i++) {
            currentMap = (Map<String, Object>) currentMap.get(keys[i]);
        }
        currentMap.put(keys[keys.length - 1], value);
    }

    public void save() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(FilePath)) {
            yaml.dump(configYML, writer);
        } catch (IOException e) {
            System.out.println("Error");
            System.out.println(e.getMessage());
        }
    }

    private static void saveFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) return;
        try (InputStream inputStream = Main.class.getResourceAsStream("/" + filePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Resource file not found: " + filePath);
            }
            Files.copy(inputStream, new File(filePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
