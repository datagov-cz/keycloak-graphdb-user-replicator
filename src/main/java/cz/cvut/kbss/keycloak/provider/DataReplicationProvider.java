package cz.cvut.kbss.keycloak.provider;

import cz.cvut.kbss.keycloak.provider.model.KodiUserAccount;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Replicates user metadata on relevant events into a GraphDB instance.
 * <p>
 * This replication involves:
 * <ul>
 *     <li>Adding basic user metadata (first name, last name) into the configured repository</li>
 *     <li>Creating a user account in the GraphDB user database with write access to the configured repository</li>
 * </ul>
 * <p>
 * Note that due to the nature of event representation in Keycloak, on update, user metadata are updated, but the since it is not possible
 * to resolve original username, a new user account is created in GraphDB user database without removing the old one. Since this change involves updating
 * the username, it is not a security risk, since the user account becomes inaccessible. The same holds for user removal - it will
 * not be removed from the GraphDB user database, because the corresponding event does not contain info about which user account
 * was removed.
 */
public class DataReplicationProvider implements EventListenerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DataReplicationProvider.class);

    private final KeycloakAdapter keycloakAdapter;

    private final UserAccountDao userAccountDao;

    private final GraphDBUserDao graphDBUserDao;

    public DataReplicationProvider(KeycloakAdapter keycloakAdapter, UserAccountDao userAccountDao,
                                   GraphDBUserDao graphDBUserDao) {
        this.keycloakAdapter = keycloakAdapter;
        this.userAccountDao = userAccountDao;
        this.graphDBUserDao = graphDBUserDao;
    }

    @Override
    public void onEvent(Event event) {
        if (keycloakAdapter.isDifferentRealm(event.getRealmId())) {
            return;
        }
        switch (event.getType()) {
            case UPDATE_PROFILE:
                updateUser(resolveUser(event));
                break;
            case UPDATE_EMAIL:
                // Create new GraphDB user account, the old one will remain, but will be inaccessible
                addGraphDBUser(resolveUser(event));
                break;
            case REGISTER:
                // This is in case user self-registration is supported
                newUser(resolveUser(event));
            default:
                break;
        }
    }

    private void newUser(KodiUserAccount userAccount) {
        LOG.info("Generating new user metadata into triple store for user {}", userAccount);
        userAccountDao.transactional(() -> userAccountDao.persist(userAccount));
        addGraphDBUser(userAccount);
    }

    private void addGraphDBUser(KodiUserAccount userAccount) {
        LOG.info("Adding user account to GraphDB use database.");
        graphDBUserDao.addUser(userAccount);
    }

    private void updateUser(KodiUserAccount userAccount) {
        LOG.info("Updating metadata of user {} in triple store", userAccount);
        userAccountDao.transactional(() -> userAccountDao.update(userAccount));
        addGraphDBUser(userAccount);
    }

    private KodiUserAccount resolveUser(Event event) {
        return getUser(event.getUserId(), event.getRealmId());
    }

    private KodiUserAccount getUser(String userId, String realmId) {
        return keycloakAdapter.getUser(userId, realmId);
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        if (keycloakAdapter.isDifferentRealm(event.getRealmId())) {
            return;
        }
        switch (event.getOperationType()) {
            case CREATE:
                newUser(resolveUser(event));
                break;
            case UPDATE:
                updateUser(resolveUser(event));
                break;
            default:
                break;
        }
    }

    private KodiUserAccount resolveUser(AdminEvent event) {
        final String resourceUri = event.getResourcePath();
        final String userId = resourceUri.substring(resourceUri.lastIndexOf('/') + 1);
        return getUser(userId, event.getRealmId());
    }

    @Override
    public void close() {
        userAccountDao.close();
    }
}
