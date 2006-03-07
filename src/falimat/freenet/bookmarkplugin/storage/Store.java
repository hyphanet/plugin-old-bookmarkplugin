package falimat.freenet.bookmarkplugin.storage;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import falimat.beanstore.BeanStore;
import falimat.beanstore.BeanStoreFactory;
import falimat.beanstore.GenericQuery;
import falimat.beanstore.StoreQuery;
import falimat.beanstore.BeanStore.KeyProvider;
import falimat.freenet.bookmarkplugin.BookmarkPlugin;
import falimat.freenet.bookmarkplugin.model.Bookmark;
import falimat.freenet.bookmarkplugin.model.Channel;
import falimat.freenet.bookmarkplugin.model.Ping;
import falimat.freenet.bookmarkplugin.model.Pong;
import falimat.freenet.bookmarkplugin.model.Slot;
import falimat.freenet.bookmarkplugin.model.User;

public class Store {

    private final static Log log = LogFactory.getLog(Store.class);

    private BeanStore<Channel> channelStore;

    private BeanStore<Slot> slotStore;

    private BeanStore<Bookmark> bookmarkStore;

    private BeanStore<User> userStore;

    private BeanStore<Ping> pingStore;

    private BeanStore<Pong> pongStore;

    private static Store instance;

    private BeanStoreFactory beanStoreFactory;

    private final static KeyProvider<Channel> channelKeyProvider = new KeyProvider<Channel>() {
        public String getKey(Channel data) {
            return data.getId();
        }
    };

    private final static KeyProvider<Bookmark> bookmarkKeyProvider = new KeyProvider<Bookmark>() {
        public String getKey(Bookmark data) {
            return data.getId();
        }
    };

    private final static KeyProvider<Ping> pingKeyProvider = new KeyProvider<Ping>() {
        public String getKey(Ping data) {
            return data.getId();
        }
    };

    private final static KeyProvider<Pong> pongKeyProvider = new KeyProvider<Pong>() {
        public String getKey(Pong data) {
            return data.getId();
        }
    };

    private Store() {
        this.beanStoreFactory = new BeanStoreFactory();
        this.beanStoreFactory.setStorageDirectory(new File("./bookmarks"));

        this.channelStore = this.beanStoreFactory.getBeanStore("channels", Channel.class);
        this.channelStore.setPropertiesToIndexAsKeywords("sender", "baseName", "lastInsertTime", "insertInterval",
                "lastSlot", "published");

        this.slotStore = this.beanStoreFactory.getBeanStore("slots", Slot.class);
        this.slotStore.setPropertiesToIndexAsKeywords("channel", "sender", "available", "failureCount", "requestTime",
                "insertTime");

        this.bookmarkStore = this.beanStoreFactory.getBeanStore("bookmarks", Bookmark.class);
        this.bookmarkStore.setPropertiesToIndexAsKeywords("tags", "contentType", "sender", "uri", "rating", "size",
                "time", "published");
        this.bookmarkStore.setPropertiesToIndexAsText("description", "title", "tags");

        this.userStore = this.beanStoreFactory.getBeanStore("users", User.class);
        this.userStore.setPropertiesToIndexAsKeywords("name");

        this.pingStore = this.beanStoreFactory.getBeanStore("pings", Ping.class);
        this.pingStore.setPropertiesToIndexAsKeywords("sender", "insertTime", "published");

        this.pongStore = this.beanStoreFactory.getBeanStore("pongs", Pong.class);
        this.pongStore.setPropertiesToIndexAsKeywords("sender", "pingId", "published");
    }

    public Channel getChannel(String id) {
        return this.channelStore.get(id);
    }

    public boolean isOutdated(Channel channel) {
        Channel existing = this.channelStore.get(channel.getId());
        if (existing != null && existing.getLastModified() > channel.getLastModified()) {
            log.info("not saving channel " + channel.getId()
                    + " because there already is a more recent version in the store");
            return true;
        }
        return false;
    }

    public List<Channel> getUnpublishedChannels(String publicSSK) {
        GenericQuery query = StoreQuery.is("sender", publicSSK).andIs("published", false);
        return this.channelStore.executeQuery(query);
    }

    public void saveChannel(Channel channel) {
        if (!isOutdated(channel)) {
            this.channelStore.put(channel.getId(), channel);
        }
    }

    public void saveBookmarks(List<Bookmark> bookmarks) {
        for (Iterator<Bookmark> it = bookmarks.iterator(); it.hasNext();) {
            Bookmark bookmark = it.next();
            if (isOutdated(bookmark)) {
                it.remove();
            }
        }
        this.bookmarkStore.put(bookmarkKeyProvider, bookmarks);
    }

    public void saveBookmark(Bookmark bookmark) {
        if (!isOutdated(bookmark)) {
            this.bookmarkStore.put(bookmark.getId(), bookmark);
        }
    }

    public boolean isOutdated(Bookmark bookmark) {
        Bookmark existing = this.bookmarkStore.get(bookmark.getId());
        if (existing != null && existing.getLastModified() > bookmark.getLastModified()) {
            log.info("not saving bookmark " + bookmark.getId()
                    + " because there already is a more recent version in the store");
            return true;
        }
        return false;
    }

    public List<Bookmark> getUnpublishedBookmarks(String publicSSK) {
        GenericQuery query = StoreQuery.is("sender", publicSSK).andIs("published", false);
        return this.bookmarkStore.executeQuery(query);
    }

    public List<Bookmark> queryBookmarks(String text, Set<String> tags, String contentType) {
        GenericQuery combinedQuery = null;
        if (text != null && text.length() > 0) {
            combinedQuery = StoreQuery.contains(text);
        }

        if (tags.size() > 0) {
            GenericQuery tagSubQuery = null;
            for (String tag : tags) {
                if (tagSubQuery == null) {
                    tagSubQuery = StoreQuery.is("tags", tag);
                } else {
                    tagSubQuery = tagSubQuery.orIs("tags", tag);
                }
            }
            if (combinedQuery == null) {
                combinedQuery = tagSubQuery;
            } else {
                combinedQuery = combinedQuery.andMatches(tagSubQuery);
            }
        }

        if (contentType != null && contentType.length() > 0) {
            GenericQuery typeSubQuery = StoreQuery.is("contentType", contentType);
            if (combinedQuery == null) {
                combinedQuery = typeSubQuery;
            } else {
                combinedQuery = combinedQuery.andMatches(typeSubQuery);
            }
        }

        if (combinedQuery != null) {
            return this.bookmarkStore.executeQuery(combinedQuery);
        } else {
            return this.getAllBookmarks();
        }

    }

    public boolean containsBookmark(Bookmark bookmark) {
        return this.bookmarkStore.get(bookmark.getId()) != null;
    }

    public List<Bookmark> getAllBookmarks() {
        return this.bookmarkStore.listValues();
    }

    public List<Channel> getAllChannels() {
        return this.channelStore.listValues();
    }

    public Channel getChannelBySender(String publicSSK) {
        return this.channelStore.queryUniqueKeyword("sender", publicSSK);
    }

    public final static synchronized Store instance() {
        if (instance == null) {
            instance = new Store();
        }
        return instance;
    }

    public void shutDown() {
        this.channelStore.shutDown();
    }

    public String getNick(String publicKey) {
        if (publicKey == null) {
            return "null";
        }
        User user = getUser(publicKey);
        if (user != null) {
            return user.getName();
        }
        return "unknown user";
    }

    public User getUserByName(String name) {
        return this.userStore.queryUniqueKeyword("name", name);
    }

    public Bookmark loadBookmark(Bookmark bookmark) {
        return this.bookmarkStore.get(bookmark.getId());
    }

    public User getUser(String publicSSK) {
        return this.userStore.get(publicSSK);
    }

    public void saveUsers(List<User> users) {
        for (User u : users) {
            this.saveUser(u);
        }
    }

    public boolean isOutdated(User user) {
        User existing = this.userStore.get(user.getPublicSSK());
        if (existing != null && existing.getLastModified() > user.getLastModified()) {
            log.info("not saving user " + user.getPublicSSK()
                    + " because there already is a more recent version in the store");
            return true;
        }
        return false;
    }

    public void saveUser(User user) {
        if (!isOutdated(user)) {
            this.userStore.put(user.getPublicSSK(), user);
        }
    }

    public void saveSlot(Slot slot) {
        this.slotStore.put(slot.getUri(), slot);
    }

    public List<Slot> getSlotsToBeFetched(User user) {
        GenericQuery query = StoreQuery.is("available", false);
        List<Slot> slotsToBeFetched = this.slotStore.executeQuery(query);
        // TODO: Implement range queries so we don't have to sort out here
        long now = System.currentTimeMillis();
        for (Iterator<Slot> it = slotsToBeFetched.iterator(); it.hasNext();) {
            Slot nextSlot = it.next();
            if (nextSlot.getRequestTime() > now) {
                it.remove();
            }
        }
        Collections.sort(slotsToBeFetched, BookmarkPlugin.slotsPriorityComparator);
        return slotsToBeFetched;
    }

    public void savePing(Ping ping) {
        this.pingStore.put(ping.getId(), ping);
    }

    public void savePong(Pong pong) {
        this.pongStore.put(pong.getId(), pong);
    }

    public List<Ping> getUnpublishedPings(String publicSSK) {
        GenericQuery query = StoreQuery.is("sender", publicSSK).andIs("published", false);
        return this.pingStore.executeQuery(query);
    }

    public List<Pong> getUnpublishedPongs(String publicSSK) {
        GenericQuery query = StoreQuery.is("sender", publicSSK).andIs("published", false);
        return this.pongStore.executeQuery(query);
    }

    public void savePings(List<Ping> pings) {
        this.pingStore.put(pingKeyProvider, pings);
    }

    public void savePongs(List<Pong> pongs) {
        this.pongStore.put(pongKeyProvider, pongs);
    }

    public List<Slot> getAllSlots() {
        return this.slotStore.listValues();
    }

    public Object getSlot(String uri) {
        return this.slotStore.get(uri);
    }

    public boolean slotIsAvailable(String channel, int i) {
        Slot slot = new Slot();
        slot.setChannel(channel);
        slot.setIndex(i);
        Slot existing = this.slotStore.get(slot.getUri());
        if (existing != null && existing.isAvailable()) {
            return true;
        }
        return false;
    }

    public boolean shouldAnswerPing(Ping ping, User user) {
        GenericQuery query = StoreQuery.is("pingId", ping.getId()).andIs("sender", user.getPublicSSK());
        List<Pong> pongs = this.pongStore.executeQuery(query);
        return pongs.size()==0;
    }

}
