package org.zendesk.client.v2;

import org.apache.http.HttpStatus;
/*import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;*/

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zendesk.client.v2.model.*;
import org.zendesk.client.v2.model.events.Event;
import org.zendesk.client.v2.model.hc.Article;
import org.zendesk.client.v2.model.hc.Category;
import org.zendesk.client.v2.model.hc.Section;
import org.zendesk.client.v2.model.hc.Translation;
import org.zendesk.client.v2.model.targets.Target;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeThat;

/**
 * @author stephenc
 * @since 04/04/2013 13:57
 */
public class RealSmokeTest {

    private static Properties config;

    private Zendesk instance;
    private String xyz;

    @BeforeClass
    public static void loadConfig() {
        config = ZendeskConfig.load();
        assumeThat("We have a configuration", config, notNullValue());
        assertThat("Configuration has an url", config.getProperty("url"), notNullValue());
    }

    @Test
    public void test() {
        System.out.println("Abhilash");
    }

    public void assumeHaveToken() {
        assumeThat("We have a username", config.getProperty("username"), notNullValue());
        assumeThat("We have a token", config.getProperty("token"), notNullValue());
    }

    public void assumeHavePassword() {
        assumeThat("We have a username", config.getProperty("username"), notNullValue());
        assumeThat("We have a password", config.getProperty("password"), notNullValue());
    }

    public void assumeHaveTokenOrPassword() {
        assumeThat("We have a username", config.getProperty("username"), notNullValue());
        assumeThat("We have a token or password", config.getProperty("token") != null || config.getProperty("password") != null, is(
                true));
    }

    @AfterClass
    public void closeClient() {
        if (instance != null) {
            instance.close();
        }
        instance = null;
    }

    @Test
    public void createClientWithToken() throws Exception {
        assumeHaveToken();
        instance = new Zendesk.Builder(config.getProperty("url"))
                .setUsername(config.getProperty("username"))
                .setToken(config.getProperty("token"))
                .build();
    }

    @Test
    public void createClientWithTokenOrPassword() throws Exception {
        System.out.println("Running Before class");
        assumeHaveTokenOrPassword();
        final Zendesk.Builder builder = new Zendesk.Builder(config.getProperty("url"))
                .setUsername(config.getProperty("username"));
        /*if (config.getProperty("token") != null) {
            builder.setToken(config.getProperty("token"));
        } else*/
        if (config.getProperty("password") != null) {
            builder.setPassword(config.getProperty("password"));
        }
        instance = builder.build();
    }

    @Test //Fine
    public void getTicket() throws Exception {
        createClientWithTokenOrPassword();
        Ticket ticket = instance.getTicket(55);
        System.out.println(ticket.getUrl());
        assertThat(ticket, notNullValue());
    }

    @Test
    public void getTicketForm() throws Exception {
        createClientWithTokenOrPassword();
        TicketForm ticketForm = instance.getTicketForm(27562);
        assertThat(ticketForm, notNullValue());
        assertTrue(ticketForm.isEndUserVisible());
    }

    @Test
    public void getTicketForms() throws Exception {
        createClientWithTokenOrPassword();
        Iterable<TicketForm> ticketForms = instance.getTicketForms();
        assertTrue(ticketForms.iterator().hasNext());
        for (TicketForm ticketForm : ticketForms) {
            assertThat(ticketForm, notNullValue());
        }
    }

    @Test
    public void getTicketFieldsOnForm() throws Exception {
        createClientWithTokenOrPassword();
        TicketForm ticketForm = instance.getTicketForm(27562);
        for (Long id : ticketForm.getTicketFieldIds()) {
            Field f = instance.getTicketField(id);
            assertNotNull(f);
        }
        assertThat(ticketForm, notNullValue());
        assertTrue(ticketForm.isEndUserVisible());
    }

    @Test
    public void getTargets() throws Exception {
        createClientWithTokenOrPassword();
        Long firstTargetId = null;
        for (Target target : instance.getTargets()) {
            assertNotNull(target);
            if (firstTargetId != null) {
                assertNotEquals(firstTargetId, target.getId()); // check for infinite loop
            } else {
                firstTargetId = target.getId();
            }
        }
    }

    @Test
    public void getTicketsPagesRequests() throws Exception {
        createClientWithTokenOrPassword();
        int count = 0;
        for (Ticket t : instance.getTickets()) {
            assertThat(t.getSubject(), notNullValue());
            if (++count > 150) {
                break;
            }
        }
        assertThat(count, is(151));
    }

    @Test
    public void getRecentTickets() throws Exception {
        createClientWithTokenOrPassword();
        int count = 0;
        for (Ticket t : instance.getRecentTickets()) {
            assertThat(t.getSubject(), notNullValue());
            if (++count > 150) {
                break;
            }
        }
        assertThat(count, is(151));
    }

    @Test
    public void getTicketsById() throws Exception {
        createClientWithTokenOrPassword();
        long count = 1;
        for (Ticket t : instance.getTickets(1, 6, 11)) {
            assertThat(t.getSubject(), notNullValue());
            assertThat(t.getId(), is(count));
            count += 5;
        }
        assertThat(count, is(16L));
    }

    @Test
    public void getTicketsIncrementally() throws Exception {
        createClientWithTokenOrPassword();
        int count = 0;
        for (Ticket t : instance.getTicketsIncrementally(new Date(0L))) {
            assertThat(t.getId(), notNullValue());
            if (++count > 10) {
                break;
            }
        }
    }

    @Test
    public void getTicketAudits() throws Exception {
        createClientWithTokenOrPassword();
        for (Audit a : instance.getTicketAudits(1L)) {
            assertThat(a, notNullValue());
            assertThat(a.getEvents(), not(Collections.<Event>emptyList()));
        }
    }

    @Test
    public void getTicketFields() throws Exception {
        createClientWithTokenOrPassword();
        int count = 0;
        for (Field f : instance.getTicketFields()) {
            System.out.println(f.getUrl());
            assertThat(f, notNullValue());
            assertThat(f.getId(), notNullValue());
            assertThat(f.getType(), notNullValue());
            if (++count > 10) {
                break;
            }
        }
    }

    @Test
    public void createClientWithPassword() throws Exception {
        assumeHavePassword();
        instance = new Zendesk.Builder(config.getProperty("url"))
                .setUsername(config.getProperty("username"))
                .setPassword(config.getProperty("password"))
                .build();
        Ticket t = instance.getTicket(1);
        assertThat(t, notNullValue());
        System.out.println(t);
    }

    @Test
    public void createAnonymousClient() {
        instance = new Zendesk.Builder(config.getProperty("url"))
                .build();
    }

    @Test
    public void createDeleteTicket() throws Exception {
        createClientWithTokenOrPassword();
        assumeThat("Must have a requester email", config.getProperty("requester.email"), notNullValue());
        Ticket t = new Ticket(
                new Ticket.Requester(config.getProperty("requester.name"), config.getProperty("requester.email")),
                "This is a test", new Comment("Please ignore this ticket"));
        t.setCollaborators(Arrays.asList(new Collaborator("Bob Example", "bob@example.org"), new Collaborator("Alice Example", "alice@example.org")));
        Ticket ticket = instance.createTicket(t);
        System.out.println(ticket.getId() + " -> " + ticket.getUrl());
        assertThat(ticket.getId(), notNullValue());
        try {
            Ticket t2 = instance.getTicket(ticket.getId());
            assertThat(t2, notNullValue());
            assertThat(t2.getId(), is(ticket.getId()));

            List<User> ticketCollaborators = instance.getTicketCollaborators(ticket.getId());
            assertThat("Collaborators", ticketCollaborators.size(), is(2));
            assertThat("First Collaborator", ticketCollaborators.get(0).getEmail(), anyOf(is("alice@example.org"), is("bob@example.org")));
        } finally {
            //  instance.deleteTicket(ticket.getId());
        }
        assertThat(ticket.getSubject(), is(t.getSubject()));
        assertThat(ticket.getRequester(), nullValue());
        assertThat(ticket.getRequesterId(), notNullValue());
        assertThat(ticket.getDescription(), is(t.getComment().getBody()));
        assertThat("Collaborators", ticket.getCollaboratorIds().size(), is(2));
//        assertThat(instance.getTicket(ticket.getId()), nullValue());
    }

    // Used for creating multiple zendesk tickets
    @Test()
    public void createSolveTickets() throws Exception {
        createClientWithTokenOrPassword();
        assumeThat("Must have a requester email", config.getProperty("requester.email"), notNullValue());
        Ticket ticket;
        long firstId = Long.MAX_VALUE;
        do {
            Ticket t = new Ticket(
                    new Ticket.Requester(config.getProperty("requester.name"), config.getProperty("requester.email")),
                    "This is a test " + UUID.randomUUID().toString(), new Comment("please ignore this ticket"));
            ticket = instance.createTicket(t);
            System.out.println(ticket.getId() + " -> " + ticket.getUrl());
            assertThat(ticket.getId(), notNullValue());
            Ticket t2 = instance.getTicket(ticket.getId());
            assertThat(t2, notNullValue());
            assertThat(t2.getId(), is(ticket.getId()));
            t2.setAssigneeId(instance.getCurrentUser().getId());
            t2.setStatus(Status.OPEN);
            t2.setPriority(Priority.HIGH);
            t2.setType(Type.TASK);
            instance.updateTicket(t2);
            assertThat(ticket.getSubject(), is(t.getSubject()));
            assertThat(ticket.getRequester(), nullValue());
            assertThat(ticket.getRequesterId(), notNullValue());
            assertThat(ticket.getDescription(), is(t.getComment().getBody()));
            assertThat(instance.getTicket(ticket.getId()), notNullValue());
            firstId = Math.min(ticket.getId(), firstId);
        }
        while (ticket.getId() < firstId + 5L); // seed enough data for the paging tests
    }

    @Test
    public void lookupUserByEmail() throws Exception {
        createClientWithTokenOrPassword();
        String requesterEmail = config.getProperty("requester.email");
        System.out.println(requesterEmail);
        assumeThat("Must have a requester email", requesterEmail, notNullValue());
        for (User user : instance.lookupUserByEmail(requesterEmail)) {
            assertThat(user.getEmail(), is(requesterEmail));
        }
    }

    @Test
    public void searchUserByEmail() throws Exception {
        createClientWithTokenOrPassword();
        String requesterEmail = config.getProperty("requester.email");
        assumeThat("Must have a requester email", requesterEmail, notNullValue());
        for (User user : instance.getSearchResults(User.class, "requester:" + requesterEmail)) {
            assertThat(user.getEmail(), is(requesterEmail));
        }
    }

    @Test
    public void lookupUserIdentities() throws Exception {
        createClientWithTokenOrPassword();
        User user = instance.getCurrentUser();
        for (Identity i : instance.getUserIdentities(user)) {
            assertThat(i.getId(), notNullValue());
            Identity j = instance.getUserIdentity(user, i);
            assertThat(j.getId(), is(i.getId()));
            assertThat(j.getType(), is(i.getType()));
            assertThat(j.getValue(), is(i.getValue()));
        }
    }

    @Test
    public void getUserRequests() throws Exception {
        createClientWithTokenOrPassword();
        User user = instance.getCurrentUser();
        int count = 5;
        for (Request r : instance.getUserRequests(user)) {
            assertThat(r.getId(), notNullValue());
            System.out.println(r.getSubject());
            for (Comment c : instance.getRequestComments(r)) {
                assertThat(c.getId(), notNullValue());
                System.out.println("  " + c.getBody());
            }
            if (--count < 0) {
                break;
            }
        }
    }

    @Test
    public void getUsers() throws Exception {
        createClientWithTokenOrPassword();
        int count = 0;
        for (User u : instance.getUsers()) {
            System.out.println(u.getName());
            assertThat(u.getName(), notNullValue());
            if (++count > 10) {
                break;
            }
        }
    }

    @Test
    public void getUsersIncrementally() throws Exception {
        createClientWithTokenOrPassword();
        int count = 0;
        for (User u : instance.getUsersIncrementally(new Date(0L))) {
            assertThat(u.getName(), notNullValue());
            if (++count > 10) {
                break;
            }
        }
    }

    @Test
    public void getSuspendedTickets() throws Exception {
        createClientWithTokenOrPassword();
        int count = 0;
        for (SuspendedTicket ticket : instance.getSuspendedTickets()) {
            assertThat(ticket.getId(), notNullValue());
            if (++count > 10) {
                break;
            }
        }
    }

    @Test
    public void getOrganizations() throws Exception {
        createClientWithTokenOrPassword();
        int count = 0;
        String filename = "./Organization.csv";
        File f = new File(filename);
        System.out.println("cannonical path is " + f.getCanonicalPath());
        FileWriter fw = new FileWriter(filename);
        fw.append("OrganizationName");
        fw.append(',');
        fw.append("OrganizationId");
        fw.append('\n');
        for (Organization t : instance.getOrganizations()) {
            System.out.println(t.getName());
            System.out.println(t.getId());
            fw.append(t.getName());
            fw.append(',');
            fw.append(String.valueOf(t.getId()));
            fw.append('\n');
            assertThat(t.getName(), notNullValue());
            if (++count > 2000) {
                break;
            }
        }
        fw.flush();
        fw.close();
    }

    @Test
    public void getOrganizationsIncrementally() throws Exception {
        createClientWithTokenOrPassword();
        int count = 0;
        for (Organization t : instance.getOrganizationsIncrementally(new Date(1L))) {
            System.out.println(t.getName());
            assertThat(t.getName(), notNullValue());
            if (++count > 10) {
                break;
            }
        }
    }

    @Test
    public void createOrganization() throws Exception {
        createClientWithTokenOrPassword();

        // Clean up to avoid conflicts
        for (Organization t : instance.getOrganizations()) {
            if ("testorg".equals(t.getExternalId())) {
                instance.deleteOrganization(t);
            }
        }

        Organization org = new Organization();
        org.setExternalId("testorg");
        org.setName("Test Organization1");
        Organization result = instance.createOrganization(org);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Organization1", result.getName());
        assertEquals("testorg", result.getExternalId());
        System.out.println("Sucessfully created Organization");
        //    instance.deleteOrganization(result);
    }

    // creates multiple organizations
    @Test()
    public void createOrganizations() throws Exception {
        createClientWithTokenOrPassword();

        // Clean up to avoid conflicts
        for (Organization t : instance.getOrganizations()) {
            if (t.getExternalId() != null && "testorg".contains(t.getExternalId())) {
                instance.deleteOrganization(t);
            }
        }


        for (int i = 1; i <= 500; i++) {
            Organization org1 = new Organization();
            org1.setExternalId("testorg" + i);
            org1.setName("Test Organization " + i);
            org1.setDomainNames(Arrays.asList("Test." + i + ""));
            System.out.println("Creating organization: " + "Test Organization " + i);

            JobStatus<Organization> result = instance.createOrganizations(org1);
            assertNotNull(result);
            assertNotNull(result.getId());
            assertNotNull(result.getStatus());


            System.out.println(given().header("Authorization", "Basic YnVkYXR0dUBnYWluc2lnaHQuY29tOjEyMzQ1NjdqYnI=").header("Content-Type", "application/json").body("{\"user\":{\"name\":\"Abhilash done\",\"email\":\"abhilash@done.com\",\"verified\":true}}").log().ifValidationFails()
                    .when().post("https://gainsight29.zendesk.com/api/v2/users.json").
                            then().log().body().statusCode(HttpStatus.SC_CREATED).extract().response().getBody().jsonPath().get("result").toString());

            while (result.getStatus() != JobStatus.JobStatusEnum.completed) {
                result = instance.getJobStatus(result);
                assertNotNull(result);
                assertNotNull(result.getId());
                assertNotNull(result.getStatus());
            }

            List<Organization> resultOrgs = result.getResults();

            assertEquals(1, resultOrgs.size());
/*        for (Organization org : resultOrgs) {
            assertNotNull(org.getId());
            instance.deleteOrganization(org);
        }*/
        }
        System.out.println("Organizations created successfully");
    }

    @Test(timeOut = 10000)
    public void bulkCreateMultipleJobs() throws Exception {
        createClientWithTokenOrPassword();

        List<Organization> orgs = new ArrayList<Organization>(4);
        for (int i = 1; i <= 5; i++) {
            Organization org = new Organization();
            org.setExternalId("testorg" + i);
            org.setName("Test Organization " + i);
            orgs.add(org);
        }

        // Clean up to avoid conflicts
        for (Organization t : instance.getOrganizations()) {
            for (Organization org : orgs) {
                if (org.getExternalId().equals(t.getExternalId())) {
                    instance.deleteOrganization(t);
                }
            }
        }


        JobStatus result1 = instance.createOrganizations(orgs.subList(0, 2));
        JobStatus result2 = instance.createOrganizations(orgs.subList(2, 5));

        while (result1.getStatus() != JobStatus.JobStatusEnum.completed || result2.getStatus() != JobStatus.JobStatusEnum.completed) {
            List<JobStatus<HashMap<String, Object>>> results = instance.getJobStatuses(Arrays.asList(result1, result2));
            result1 = results.get(0);
            result2 = results.get(1);
            assertNotNull(result1);
            assertNotNull(result1.getId());
            assertNotNull(result2);
            assertNotNull(result2.getId());
        }

        List<HashMap> resultOrgs1 = result1.getResults();
        assertEquals(2, resultOrgs1.size());
        List<HashMap> resultOrgs2 = result2.getResults();
        assertEquals(3, resultOrgs2.size());

        for (HashMap org : resultOrgs1) {
            assertNotNull(org.get("id"));
            instance.deleteOrganization(((Number) org.get("id")).longValue());
        }

        for (HashMap org : resultOrgs2) {
            assertNotNull(org.get("id"));
            instance.deleteOrganization(((Number) org.get("id")).longValue());
        }
    }

    @Test
    public void getGroups() throws Exception {
        createClientWithTokenOrPassword();
        int count = 0;
        for (Group t : instance.getGroups()) {
            System.out.println(t.getName());
            assertThat(t.getName(), notNullValue());
            if (++count > 10) {
                break;
            }
        }
    }

    @Test
    public void getArticles() throws Exception {
        createClientWithTokenOrPassword();
        int count = 0;
        for (Article t : instance.getArticles()) {
            System.out.println(t.getLabelNames());
            assertThat(t.getTitle(), notNullValue());
            if (++count > 40) {  // Check enough to pull 2 result pages
                break;
            }
        }
    }

    @Test
    public void getArticleTranslations() throws Exception {
        createClientWithTokenOrPassword();
        int articleCount = 0;
        int translationCount = 0;  // Count total translations checked, not per-article
        for (Article art : instance.getArticles()) {
            assertNotNull(art.getId());
            if (++articleCount > 10) {
                break; // Do not overwhelm the getArticles API
            }
            for (Translation t : instance.getArticleTranslations(art.getId())) {
                assertNotNull(t.getId());
                assertNotNull(t.getTitle());
                assertNotNull(t.getBody());
                if (++translationCount > 3) {
                    return;
                }
            }
        }
    }

    @Test
    public void getSectionTranslations() throws Exception {
        createClientWithTokenOrPassword();
        int sectionCount = 0;
        int translationCount = 0;
        for (Section sect : instance.getSections()) {
            assertNotNull(sect.getId());
            if (++sectionCount > 10) {
                break;
            }
            for (Translation t : instance.getSectionTranslations(sect.getId())) {
                assertNotNull(t.getId());
                assertNotNull(t.getTitle());
                assertNotNull(t.getBody());
                if (++translationCount > 3) {
                    return;
                }
            }
        }
    }

    @Test
    public void getCategoryTranslations() throws Exception {
        createClientWithTokenOrPassword();
        int categoryCount = 0;
        int translationCount = 0;
        for (Category cat : instance.getCategories()) {
            assertNotNull(cat.getId());
            if (++categoryCount > 10) {
                break;
            }
            for (Translation t : instance.getCategoryTranslations(cat.getId())) {
                assertNotNull(t.getId());
                assertNotNull(t.getTitle());
                assertNotNull(t.getBody());
                if (++translationCount > 3) {
                    return;
                }
            }
        }
    }

    @Test
    public void getArticlesIncrementally() throws Exception {
        createClientWithTokenOrPassword();
        final long ONE_WEEK = 7 * 24 * 60 * 60 * 1000;
        int count = 0;
        try {
            for (Article t : instance.getArticlesIncrementally(new Date(new Date().getTime() - ONE_WEEK))) {
                assertThat(t.getTitle(), notNullValue());
                if (++count > 10) {
                    break;
                }
            }
        } catch (ZendeskResponseException zre) {
            if (zre.getStatusCode() == 502) {
                // Ignore, this is an API limitation
                // A "Bad Gateway" response is returned if HelpCenter was not active at the given time
            } else {
                throw zre;
            }
        }
    }

    @Test
    public void testCreateUsers() throws Exception {


    }
}
