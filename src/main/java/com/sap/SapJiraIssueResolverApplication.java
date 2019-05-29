package com.sap;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This application reads a csv/excel file and copies one field to another and
 * updates in Jira.
 * 
 * Please provide fill up all the constants present in this file before running
 * the application.
 * 
 * @author Adityaraj.Mishra
 */
@SpringBootApplication
public class SapJiraIssueResolverApplication implements CommandLineRunner {

    private static final String USER_NAME = "";

    private static final String PASSWORD = "";

    private static final String JIRA_BASE_URL = "";

    private static final String PLANNED_EFFORT_FIELD_NAME = "customfield_10005";

    private static final String EFFORT_IN_FIELD_NAME = "customfield_20440";

    public static void main(String[] args) {
        SpringApplication.run(SapJiraIssueResolverApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        try {

            String issueKey = "EX44-1";

            JSONObject response = getIssueDetails(issueKey);

            JSONObject fields = response.getJSONObject("fields");

            Double plannedEffort = null;

            Double effortInPD = null;

            if (fields.get(PLANNED_EFFORT_FIELD_NAME) != null
                    && !fields.get(PLANNED_EFFORT_FIELD_NAME).toString().equalsIgnoreCase("null")) {
                plannedEffort = (Double) fields.get(PLANNED_EFFORT_FIELD_NAME);
            }

            if (fields.get(EFFORT_IN_FIELD_NAME) != null
                    && !fields.get(EFFORT_IN_FIELD_NAME).toString().equalsIgnoreCase("null")) {
                effortInPD = (Double) fields.get(EFFORT_IN_FIELD_NAME);
            }

            if (plannedEffort != null && effortInPD == null) {
                Double newEffort = calculateNewPlannedEffort(plannedEffort);
                update(issueKey, newEffort);
                System.out.println("Issue updated: " + issueKey);
            } else {
                System.out.println("Issue didn't get update: " + issueKey);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Double calculateNewPlannedEffort(Double plannedEffort) {
        // TODO Auto-generated method stub
        return plannedEffort;
    }

    public static void update(String issueKey, Double newEffort) throws ClientProtocolException, IOException {

        String url = JIRA_BASE_URL + "/rest/api/2/issue/" + issueKey;
        String userPassword = USER_NAME + ":" + PASSWORD;
        String encodeBase64 = Base64.getEncoder().encodeToString(userPassword.getBytes("utf-8"));
        HttpPut httpPut = new HttpPut(url);
        String authHeader = "Basic " + encodeBase64;
        httpPut.setHeader(HttpHeaders.AUTHORIZATION, authHeader);

        StringEntity input = new StringEntity("{\"fields\":{\"customfield_10005\":null,\"customfield_20440\":24}}");
        input.setContentType("application/json");

        httpPut.setEntity(input);

        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(httpPut);

        System.out.println(response.getStatusLine().getStatusCode());

    }

    private static JSONObject getIssueDetails(String issueKey)
            throws UnsupportedEncodingException, IOException, ClientProtocolException, JSONException {
        String url = JIRA_BASE_URL + "/rest/api/latest/issue/" + issueKey;

        String userPassword = USER_NAME + ":" + PASSWORD;

        String encodeBase64 = Base64.getEncoder().encodeToString(userPassword.getBytes("utf-8"));

        HttpGet request = new HttpGet(url);

        String authHeader = "Basic " + encodeBase64;
        request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(request);

        HttpEntity entity = response.getEntity();

        StringWriter writer = new StringWriter();
        IOUtils.copy(entity.getContent(), writer);

        JSONObject jsonObject = new JSONObject(writer.toString());

        System.out.println(response.getStatusLine().getStatusCode());

        return jsonObject;
    }

}
