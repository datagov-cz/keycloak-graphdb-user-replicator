package cz.cvut.kbss.keycloak.provider.model;

import org.keycloak.models.UserModel;

import java.net.URI;
import java.util.Objects;

public class KodiUserAccount {

    private static String namespace = "http://onto.fel.cvut.cz/ontologies/uzivatel/";

    private static String context = null;

    private URI uri;

    private String firstName;

    private String lastName;

    private String username;

    public KodiUserAccount() {
    }

    public KodiUserAccount(UserModel userModel) {
        Objects.requireNonNull(userModel);
        this.uri = URI.create(namespace + userModel.getId());
        this.firstName = userModel.getFirstName();
        this.lastName = userModel.getLastName();
        this.username = userModel.getUsername();
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static void setNamespace(String namespace) {
        KodiUserAccount.namespace = namespace;
    }

    public static void setContext(String context) {
        KodiUserAccount.context = context;
    }

    public static String getContext() {
        return context;
    }

    @Override
    public String toString() {
        return "KodiUserAccount{" +
                "<" + uri + "> " +
                firstName + " " + lastName +
                ", username='" + username + '\'' +
                '}';
    }
}
