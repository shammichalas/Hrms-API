package com.company.hrms.settings.service;

import com.company.hrms.settings.entity.Settings;
import com.company.hrms.settings.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingsService {

    private final SettingsRepository settingsRepository;

    @Transactional(readOnly = true)
    public String getSettingValue(String key, String defaultValue) {
        return settingsRepository.findByKey(key)
                .map(Settings::getValue)
                .orElse(defaultValue);
    }

    @Transactional
    public Settings updateSetting(String key, String value, String description) {
        log.info("Updating setting key: {}", key);
        Settings setting = settingsRepository.findByKey(key)
                .orElse(new Settings());

        if (setting.getId() == null) {
            setting.setKey(key);
        }

        setting.setValue(value);
        if (description != null) {
            setting.setDescription(description);
        }

        Settings saved = settingsRepository.save(setting);
        log.info("Setting updated: {}", key);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Settings> getAllSettings() {
        return settingsRepository.findAll();
    }
}
