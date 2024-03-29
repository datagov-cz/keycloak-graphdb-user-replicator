package cz.cvut.kbss.keycloak.provider;

import cz.cvut.kbss.keycloak.provider.model.KodiUserAccount;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserProvider;

import java.util.Objects;

public class KeycloakAdapter {

    private final UserProvider userProvider;
    private final RealmProvider realmProvider;

    private final Configuration configuration;

    public KeycloakAdapter(UserProvider userProvider, RealmProvider realmProvider, Configuration configuration) {
        this.userProvider = userProvider;
        this.realmProvider = realmProvider;
        this.configuration = configuration;
    }

    public boolean isDifferentRealm(String realmId) {
        return !Objects.equals(configuration.getRealmId(), realmId);
    }

    public KodiUserAccount getUser(String userId, String realmId) {
        final UserModel userModel = userProvider.getUserById(userId, realmProvider.getRealm(realmId));
        return userModel != null ? new KodiUserAccount(userModel) : null;
    }
}
