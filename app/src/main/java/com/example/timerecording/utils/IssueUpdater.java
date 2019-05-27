package com.example.timerecording.utils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.WebView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import android.net.Uri;
import android.os.AsyncTask;

import com.example.timerecording.models.BitbucketIssue;
import com.example.timerecording.models.BitbucketResponse;

public class IssueUpdater extends AsyncTask<Void, Void, BitbucketIssue[]> {
    // Basis-Zugriffspunkt
    private static final String _ISSUE_BASE_URI =
            "https://api.bitbucket.org/2.0/repositories/webducerbooks/androidbook-changes/issues?q=";

    // Filter
    private static final String _ISSUE_FILTER =
            "(state = \"new\" OR state = \"on hold\" OR state = \"open\")"
                    + " AND updated_on > \"2017-02-01T00:00+02:00\""
                    + " AND component != \"395592\"";
    private static final String _FILTERED_ISSUE_URI =
            _ISSUE_BASE_URI
                    + Uri.encode(_ISSUE_FILTER);

    // Standardrückgabewert
    private final static BitbucketIssue[] _DEFAULT_RETURN_VALUE = new BitbucketIssue[0];
    private WebView _webContent;

    public IssueUpdater(WebView webContent){
        _webContent = webContent;
    }

    // Methode, um nach dem Abschluss der Arbeit Daten zu verarbeiten
    @Override
    protected void onPostExecute(BitbucketIssue[] bitbucketIssues) {
        super.onPostExecute(bitbucketIssues);
        // Konvertieren in HTML
        String html = convertToHtml(bitbucketIssues);

        // HTML in WebView ausgeben
        _webContent.loadData(html, "text/html; charset=utf-8", "utf-8");
    }

    private String convertToHtml(BitbucketIssue[] bitbucketIssues) {
        StringBuilder html = new StringBuilder();
        // Header der HTML Datei
        html.append("<!DOCTYPE html>")
                .append("<html lang=\"en\">")
                .append("<head>")
                .append("<meta charset=\"utf-8\">")
                .append("<title>Fehlerliste</title>")
                .append("</head>");

        // Inhaltsbereich
        html.append("<body>");

        // Fehler ausgeben
        for (BitbucketIssue issue : bitbucketIssues) {
            // Überschrift
            html.append("<h1>")
                    .append("#")
                    .append(issue.id)
                    .append(" - ")
                    .append(issue.title)
                    .append("</h1>");

            // Status
            html.append("<div>")
                    .append("Status: ")
                    .append(issue.state)
                    .append("</div>");

            // Priorität
            html.append("<div>")
                    .append("Priorität: ")
                    .append(issue.priority)
                    .append("</div>");

            // Inhalt
            html.append("<section>")
                    .append(issue.content.html)
                    .append("</section>");
        }

        // Datei finalisieren
        html.append("</body>")
                .append("</html>");

        return html.toString();
    }

    @Override
    protected BitbucketIssue[] doInBackground(Void... voids) {
        // Client initialisieren
        try {
            // Parsen der Uri
            URL url = new URL(_FILTERED_ISSUE_URI);

            // Erstellen des Clients
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            // Defintion der Timeouts
            connection.setReadTimeout(30000); // 30 Sekunden
            connection.setConnectTimeout(60000); // 60 Sekunden

            // Anfrage Header definieren
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            // Verbinden
            connection.connect();

            // Status der Anfrage prüfen
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                // Fehler bei der Abfrage der Daten
                return _DEFAULT_RETURN_VALUE;
            }

            // Laden der Daten
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            // Lesen der geladenen Daten
            StringBuilder content = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            // Schließen der Resourcen
            reader.close();
            is.close();
            connection.disconnect();

            //parseIssueList(content.toString());
            return parseWithGSon(content.toString());

        } catch (MalformedURLException e) {
            // URL konnte nicht interpretiert werden
            e.printStackTrace();
        } catch (IOException e) {
            // Fehler beim Zugriff auf das Internet
            e.printStackTrace();
        }

        return _DEFAULT_RETURN_VALUE;
    }

    private String[] parseIssueList(String content) {
        // Prüfen des Inhaltes
        if (content == null || content.isEmpty()) {
            // Rückgabe einer leeren Liste
            return new String[0];
        }

        // String in JSON-Object umwandeln
        try {
            JSONObject response = new JSONObject(content);

            // Auslesen der Werte als Array
            JSONArray issueList = response.getJSONArray("values");

            if (issueList == null || issueList.length() == 0) {
                // Liste ist leer
                return new String[0];
            }

            // Titel aus der Liste der Issues auslesen
            List<String> issueTitles = new ArrayList<>();
            for (int index = 0; index < issueList.length(); index++) {
                JSONObject issue = issueList.getJSONObject(index);
                String title = issue.getString("title");
                Log.i("ISSUE-TITLE: ", title);
                issueTitles.add(title);
            }

            return issueTitles.toArray(new String[]{});

        } catch (JSONException e) {
            // String ist kein JSON-Objekt, oder Eigenschaft kann nicht ausgelesen werden
            e.printStackTrace();
            return new String[0];
        }
    }

    private BitbucketIssue[] parseWithGSon(String content) {
        // Prüfen des Inhaltes
        if (content == null || content.isEmpty()) {
            // Rückgabe einer leeren Liste
            return _DEFAULT_RETURN_VALUE;
        }

        // Initialisieren der Bibliothek
        Gson g = new Gson();
        // Deserialisieren von JSON
        BitbucketResponse response = g.fromJson(content, BitbucketResponse.class);

        return response.values;
    }
}