package org.example.services.calendar;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.example.entities.care.CareTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.util.Collections;

@Service
public class GoogleCalendarAdapter implements ExternalCalendarService {

    // 💡 Injecté dynamiquement depuis tes fichiers properties !
    @Value("${google.calendar.id}")
    private String calendarId;

    @Value("${google.api.credentials-path}")
    private String credentialsPath;

    private Calendar calendarService;

    // Le constructeur reste vide, on initialise après l'injection des @Value
    public GoogleCalendarAdapter() {}

    @PostConstruct
    public void init() {
        try {
            // Utilise le chemin configuré dynamiquement
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(credentialsPath))
                    .createScoped(Collections.singleton(
                            "https://www.googleapis.com/auth/calendar"
                    ));

            this.calendarService = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName("GreenDesk").build();

        } catch (java.io.FileNotFoundException e) {
            System.err.println("[WARNING] Fichier introuvable : " + credentialsPath + " (Normal en CI/Test)");
            this.calendarService = null;
        } catch (Exception e) {
            System.err.println("[ERROR] Erreur d'initialisation Google Calendar : " + e.getMessage());
            this.calendarService = null;
        }
    }

    @Override
    public String push(CareTask task) {

        //simuler l'envoi si le service n'est pas initialisé
        if (this.calendarService == null) {
            return "mock-google-" + java.util.UUID.randomUUID().toString();
        }
        try {
            Event event = new Event()
                    .setSummary(task.getDescription())
                    .setDescription("GreenDesk CareTask: " + task.getType());

            EventDateTime start = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(task.getScheduledAt().toString()));

            EventDateTime end = new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(task.getDueAt().toString()));

            event.setStart(start);
            event.setEnd(end);

            // Utilise l'ID dynamique
            Event created = calendarService.events()
                    .insert(calendarId, event)
                    .execute();

            return created.getId();

        } catch (Exception e) {
            throw new RuntimeException("Google Calendar push failed pour l'agenda : " + calendarId, e);
        }
    }

    @Override
    public void update(String externalId, CareTask task) {

        //ignorer la mise à jour si on est en mode test/CI
        if (this.calendarService == null || externalId == null || externalId.startsWith("mock-")) {
            return;
        }
        try {
            Event event = calendarService.events()
                    .get(calendarId, externalId)
                    .execute();

            event.setSummary(task.getDescription());
            event.setDescription("GreenDesk CareTask: " + task.getType());
            event.setStart(new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(task.getScheduledAt().toString())));
            event.setEnd(new EventDateTime()
                    .setDateTime(new com.google.api.client.util.DateTime(task.getDueAt().toString())));

            calendarService.events()
                    .update(calendarId, externalId, event)
                    .execute();

        } catch (Exception e) {
            throw new RuntimeException("Google Calendar update failed", e);
        }
    }

    @Override
    public void remove(String externalId) {

        //ignorer la suppression si on est en mode test/CI
        if (this.calendarService == null || externalId == null || externalId.startsWith("mock-")) {
            return;
        }
        try {
            calendarService.events()
                    .delete(calendarId, externalId)
                    .execute();

        } catch (Exception e) {
            throw new RuntimeException("Google Calendar delete failed", e);
        }
    }
}
