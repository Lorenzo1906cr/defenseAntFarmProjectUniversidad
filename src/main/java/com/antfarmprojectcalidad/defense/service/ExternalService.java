package com.antfarmprojectcalidad.defense.service;

import com.antfarmprojectcalidad.defense.model.Threat;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ExternalService {
    private String lastValue = null;

    public boolean hasChanged() {
        // Example: call external API
        String newValue = callExternalService();

        boolean changed = !Objects.equals(newValue, lastValue);
        lastValue = newValue;
        return changed;
    }

    private String callExternalService() {
        // TODO: use WebClient, HttpURLConnection, or RestTemplate
        return "example";
    }

    public List<Threat> getActiveThreats() {
        return null;
    }
}
