package si.f5.wata777.sacraeconomyv2.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LanguageManager {
    
    private final JavaPlugin plugin;
    private final DatabaseManager databaseManager;
    private final Map<String, Map<String, String>> languages = new HashMap<>();
    private String defaultLanguage;

    public LanguageManager(JavaPlugin plugin, DatabaseManager databaseManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.defaultLanguage = configManager.getConfig().getString("language.default", "ja");
        
        loadLanguages();
    }

    private void loadLanguages() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            // Copy default language files from resources
            plugin.saveResource("lang/ja.yml", false);
            plugin.saveResource("lang/en.yml", false);
        }

        // Load all YAML files in lang folder
        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File langFile : langFiles) {
                String langCode = langFile.getName().replace(".yml", "");
                YamlConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
                Map<String, String> langMap = flattenYaml(langConfig, "");
                languages.put(langCode, langMap);
                plugin.getLogger().info("Loaded language: " + langCode);
            }
        }
    }

    private Map<String, String> flattenYaml(YamlConfiguration config, String prefix) {
        Map<String, String> flat = new HashMap<>();
        for (String key : config.getKeys(true)) {
            if (!config.isConfigurationSection(key)) {
                String value = config.get(key).toString();
                String fullKey = prefix.isEmpty() ? key : prefix + "." + key;
                flat.put(fullKey, value);
            }
        }
        return flat;
    }

    public void setPlayerLanguage(UUID playerUuid, String languageCode) {
        if (!languages.containsKey(languageCode)) {
            return;
        }

        try {
            databaseManager.executeUpdate(
                "UPDATE " + databaseManager.getTableName("player_accounts") + 
                " SET language = ? WHERE uuid = ?",
                languageCode, playerUuid.toString()
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set player language: " + e.getMessage());
        }
    }

    public String getPlayerLanguage(UUID playerUuid) {
        try {
            ResultSet rs = databaseManager.executeQuery(
                "SELECT language FROM " + databaseManager.getTableName("player_accounts") + 
                " WHERE uuid = ?",
                playerUuid.toString()
            );
            if (rs.next()) {
                String lang = rs.getString("language");
                if (lang != null && !lang.isEmpty()) {
                    return languages.containsKey(lang) ? lang : defaultLanguage;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get player language: " + e.getMessage());
        }
        return defaultLanguage;
    }

    public String translate(UUID playerUuid, String key, Map<String, String> placeholders) {
        String language = getPlayerLanguage(playerUuid);
        Map<String, String> langMap = languages.get(language);
        
        if (langMap == null || !langMap.containsKey(key)) {
            langMap = languages.get(defaultLanguage);
            if (langMap == null || !langMap.containsKey(key)) {
                return "§c[MISSING: " + key + "]";
            }
        }

        String message = langMap.get(key);
        
        // Replace placeholders
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }

        return message;
    }

    public String translate(UUID playerUuid, String key) {
        return translate(playerUuid, key, null);
    }

    public String translate(Player player, String key, Map<String, String> placeholders) {
        return translate(player.getUniqueId(), key, placeholders);
    }

    public String translate(Player player, String key) {
        return translate(player.getUniqueId(), key, null);
    }

    public List<String> getAvailableLanguages() {
        return new ArrayList<>(languages.keySet());
    }

    public boolean isLanguageAvailable(String languageCode) {
        return languages.containsKey(languageCode);
    }

    public void reloadLanguages() {
        languages.clear();
        loadLanguages();
        plugin.getLogger().info("Languages reloaded");
    }
}
