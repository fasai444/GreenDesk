package org.example.services.calendar;

import org.example.entities.care.CareTask;

public interface ExternalCalendarService {

    String push(CareTask task);

    void update(String externalId, CareTask task);

    void remove(String externalId);
}