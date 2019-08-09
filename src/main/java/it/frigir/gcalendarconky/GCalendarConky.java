package it.frigir.gcalendarconky;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Events;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class GCalendarConky {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/calendar.readonly");

    private static Credential getCredentials(String credentialFilepath, NetHttpTransport HTTP_TRANSPORT) throws IOException {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(credentialFilepath));
        GoogleAuthorizationCodeFlow flow = (new Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)).setDataStoreFactory(new FileDataStoreFactory(new File("tokens"))).setAccessType(
                "offline").build();
        return (new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {

        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service =
                (new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(args[0], HTTP_TRANSPORT))).setApplicationName("My gCalendar events").build();

        DateTime now = new DateTime(System.currentTimeMillis());
        SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy");

        Events events = (Events) service.events()
                .list("primary")
                .setMaxResults(10)
                .setTimeMin(now)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        events.getItems().forEach((event) -> {
            Date dStart = new Date((event.getStart().getDateTime() == null ? event.getStart().getDate() : event.getStart().getDateTime()).getValue());
            Date dEnd = new Date((event.getEnd().getDateTime() == null ? event.getEnd().getDate() : event.getEnd().getDateTime()).getValue());
            String start = dt.format(dStart);
            String end = dt.format(dEnd);
            String date = start.equals(end) ? start : "Da " + start + " a " + end;
            System.out.printf("%s (%s)\n", event.getSummary(), date);
        });
    }
}