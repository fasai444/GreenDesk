package org.example.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    // Stockage des notifications en mémoire (pour l'interface)
    private final List<Notification> notifications = new ArrayList<>();

    @Value("${notifications.console.enabled:true}")
    private boolean consoleEnabled;

    @Value("${notifications.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${notifications.email.to:admin@greendesk.local}")
    private String emailTo;

    // Configuration email (à compléter avec une vraie config SMTP si besoin)
    // @Value("${spring.mail.host:}")
    // private String mailHost;

    /**
     * Envoie une notification
     * @param type Type de notification (WEATHER_ALERT, PLANT_CRITICAL, PREDICTION_RISK)
     * @param title Titre de la notification
     * @param message Message détaillé
     * @param plantId ID de la plante concernée (peut être null)
     * @param forestId ID de la forêt concernée (peut être null)
     */
    public void sendNotification(String type, String title, String message, String plantId, String forestId) {
        Notification notification = new Notification(
            java.util.UUID.randomUUID().toString(),
            type,
            title,
            message,
            plantId,
            forestId,
            LocalDateTime.now(),
            false
        );

        // 1. Stocker en mémoire
        notifications.add(0, notification); // Ajouter en tête
        if (notifications.size() > 100) {
            notifications.remove(notifications.size() - 1); // Garder max 100
        }

        // 2. Afficher dans la console (toujours)
        if (consoleEnabled) {
            logToConsole(notification);
        }

        // 3. Envoyer par email (si configuré)
        if (emailEnabled) {
            sendEmail(notification);
        }

        // 4. Log
        logger.info("Notification envoyée: [{}] {} - {}", type, title, message);
    }

    /**
     * Envoie une notification simple
     */
    public void sendNotification(String type, String title, String message) {
        sendNotification(type, title, message, null, null);
    }

    /**
     * Récupère toutes les notifications non lues
     */
    public List<Notification> getUnreadNotifications() {
        return notifications.stream()
                .filter(n -> !n.isRead())
                .toList();
    }

    /**
     * Récupère toutes les notifications
     */
    public List<Notification> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    /**
     * Marque une notification comme lue
     */
    public void markAsRead(String notificationId) {
        notifications.stream()
                .filter(n -> n.getId().equals(notificationId))
                .findFirst()
                .ifPresent(n -> n.setRead(true));
    }

    /**
     * Marque toutes les notifications comme lues
     */
    public void markAllAsRead() {
        notifications.forEach(n -> n.setRead(true));
    }

    private void logToConsole(Notification notification) {
        String emoji = switch (notification.getType()) {
            case "WEATHER_ALERT" -> "🌦️";
            case "PLANT_CRITICAL" -> "⚠️";
            case "PREDICTION_RISK" -> "🔮";
            default -> "📢";
        };
        System.out.println("\n" + emoji + " [NOTIFICATION] " + notification.getTitle());
        System.out.println("   " + notification.getMessage());
        System.out.println("   " + notification.getTimestamp() + "\n");
    }

    private void sendEmail(Notification notification) {
        // À implémenter avec JavaMailSender si besoin
        // Exemple avec simulation
        logger.info("Email envoyé à {}: {} - {}", emailTo, notification.getTitle(), notification.getMessage());
    }

    // ==================== CLASSE INTERNE NOTIFICATION ====================

    public static class Notification {
        private String id;
        private String type;
        private String title;
        private String message;
        private String plantId;
        private String forestId;
        private LocalDateTime timestamp;
        private boolean read;

        public Notification(String id, String type, String title, String message, 
                           String plantId, String forestId, LocalDateTime timestamp, boolean read) {
            this.id = id;
            this.type = type;
            this.title = title;
            this.message = message;
            this.plantId = plantId;
            this.forestId = forestId;
            this.timestamp = timestamp;
            this.read = read;
        }

        // Getters et Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getPlantId() { return plantId; }
        public void setPlantId(String plantId) { this.plantId = plantId; }
        public String getForestId() { return forestId; }
        public void setForestId(String forestId) { this.forestId = forestId; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
    }
}
