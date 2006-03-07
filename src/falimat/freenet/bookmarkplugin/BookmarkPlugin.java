package falimat.freenet.bookmarkplugin;

import java.net.MalformedURLException;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xomat.util.ParamException;
import xomat.util.ParamRuntimeException;
import xomat.util.StopWatch;
import falimat.freenet.bookmarkplugin.components.LoginMessage;
import falimat.freenet.bookmarkplugin.components.SideBar;
import falimat.freenet.bookmarkplugin.model.AbstractSendable;
import falimat.freenet.bookmarkplugin.model.Bookmark;
import falimat.freenet.bookmarkplugin.model.Channel;
import falimat.freenet.bookmarkplugin.model.Ping;
import falimat.freenet.bookmarkplugin.model.Pong;
import falimat.freenet.bookmarkplugin.model.Slot;
import falimat.freenet.bookmarkplugin.model.User;
import falimat.freenet.bookmarkplugin.storage.Store;
import falimat.freenet.bookmarkplugin.ui.AddBookmarkPage;
import falimat.freenet.bookmarkplugin.ui.BookmarksPage;
import falimat.freenet.bookmarkplugin.ui.ChannelsPage;
import falimat.freenet.bookmarkplugin.ui.HomePage;
import falimat.freenet.network.SlotWriter;
import falimat.freenet.network.XmlSlotReader;
import falimat.freenet.network.XmlSlotWriter;
import falimat.freenet.webplugin.WebinterfacePlugin;
import falimat.freenet.webplugin.components.NavigationMenu;
import freenet.client.ClientMetadata;
import freenet.client.FetchException;
import freenet.client.FetchResult;
import freenet.client.HighLevelSimpleClient;
import freenet.client.InsertBlock;
import freenet.client.InserterException;
import freenet.keys.FreenetURI;
import freenet.pluginmanager.PluginRespirator;
import freenet.support.ArrayBucket;
import freenet.support.Logger;

public class BookmarkPlugin extends WebinterfacePlugin {

    private final static Log log = LogFactory.getLog(BookmarkPlugin.class);

    HighLevelSimpleClient client;

    private boolean terminationRequested = false;

    private HomePage homePage;

    private ChannelsPage channelsPage;

    private AddBookmarkPage addBookmarkPage;

    private BookmarksPage bookmarksPage;

    private LoginMessage loginMessage;

    private SideBar sidebar;

    @Override
    public void runPlugin(PluginRespirator pr) {
        Logger.normal(BookmarkPlugin.class, this + " was started.");

        this.constructInterface();

        this.client = pr.getHLSimpleClient();
        while (!terminationRequested) {
            try {
                Thread.sleep(1 * DateUtils.MILLIS_PER_MINUTE);
                this.endlessLoop();
            } catch (InterruptedException e) {
                e.printStackTrace();
                this.terminationRequested = true;
            }
        }
    }

    private void constructInterface() {
        this.homePage = new HomePage("Bookmark Plugin");
        super.setRootPage(this.homePage);

        this.channelsPage = new ChannelsPage("Channels");
        super.getRootPage().addSubpage(this.channelsPage, "channels");

        this.addBookmarkPage = new AddBookmarkPage(this, "Add Bookmark");
        super.getRootPage().addSubpage(this.addBookmarkPage, "addBookmark");

        this.bookmarksPage = new BookmarksPage("Find Bookmarks");
        super.getRootPage().addSubpage(this.bookmarksPage, "bookmarks");

        this.sidebar = new SideBar();
        NavigationMenu menu = new NavigationMenu(super.getRootPage());
        this.sidebar.setNavigation(menu);
        super.getRootPage().addToAll(this.sidebar);

        this.loginMessage = new LoginMessage(this.addBookmarkPage);
        super.getRootPage().addToAll(this.loginMessage);

        this.homePage.constructComponents();
        this.channelsPage.constructComponents();
        this.addBookmarkPage.constructComponents();
        this.bookmarksPage.constructComponents();

    }

    private void endlessLoop() {

        long now = System.currentTimeMillis();

        User user = LoginCredentials.instance().getCurrentUser();

        if (user != null) {

            // check if the next slot of a current user's channel should be
            // inserted
            Channel personalChannel = Store.instance().getChannelBySender(user.getPublicSSK());
            if (personalChannel == null) {
                String msg = "No personal channel found in database for user {0}";
                throw new ParamRuntimeException(msg, user.getPublicSSK());
            }
            long nextInsertTime = personalChannel.getLastInsertTime() + personalChannel.getInsertInterval();
            if (now < nextInsertTime) {
                log.debug("There are " + (nextInsertTime - now) / 1000 + " seconds until the next insert.");
            } else {
                this.insertNextSlotOnPersonalChannel(user, personalChannel);
            }
        }

        List<Slot> slotsToBeFetched = Store.instance().getSlotsToBeFetched(user);

        if (!slotsToBeFetched.isEmpty()) {
            Slot slot = slotsToBeFetched.get(0);

            Channel requestedChannel = Store.instance().getChannel(slot.getChannel());
            try {
                FetchResult result = this.fetch(slot);

                String contentType = result.getMimeType();

                XmlSlotReader reader = new XmlSlotReader();
                if (!reader.getContentType().equals(contentType)) {
                    String msg = "Will not try to parse slot data beecause its content type was {0} instead of {1}";
                    throw new ParamException(msg, contentType, reader.getContentType());
                }

                byte[] data = result.asByteArray();
                reader.readMessages(data);

                List<User> users = reader.getUsers();
                Store.instance().saveUsers(users);

                now = System.currentTimeMillis();

                List<Channel> channels = reader.getChannels();
                List<Bookmark> bookmarks = reader.getBookmarks();
                List<Ping> pings = reader.getPings();
                List<Pong> pongs = reader.getPongs();

                for (Channel channel : channels) {
                    Store.instance().saveChannel(channel);

                    Slot lastSlot = new Slot(channel, channel.getLastSlot());
                    if (lastSlot.getUri().equals(slot.getUri())) {
                        lastSlot = slot;
                    }

                    if (!Store.instance().slotIsAvailable(lastSlot.getChannel(), lastSlot.getIndex())) {
                        lastSlot.setExpectedTime(channel.getLastInsertTime());
                        if (now > lastSlot.getExpectedTime()) {
                            lastSlot.setRequestTime(now);
                        } else {
                            lastSlot.setRequestTime(channel.getLastInsertTime());
                        }
                        Store.instance().saveSlot(lastSlot);
                    }

                    Slot nextSlot = new Slot(channel, channel.getLastSlot() + 1);
                    if (!Store.instance().slotIsAvailable(nextSlot.getChannel(), nextSlot.getIndex())) {
                        nextSlot.setExpectedTime(lastSlot.getExpectedTime() + channel.getInsertInterval());
                        if (now > nextSlot.getExpectedTime()) {
                            nextSlot.setRequestTime(now);
                        } else {
                            nextSlot.setRequestTime(nextSlot.getExpectedTime() + 3 * DateUtils.MILLIS_PER_MINUTE);
                        }
                        Store.instance().saveSlot(nextSlot);
                    }
                }
                Store.instance().saveBookmarks(bookmarks);
                Store.instance().savePings(pings);
                Store.instance().savePongs(pongs);

                if (user != null) {
                    for (Ping ping : pings) {
                        if (Store.instance().shouldAnswerPing(ping, user)) {
                            Pong pong = new Pong(ping, user);
                            Store.instance().savePong(pong);
                        }
                    }
                }

                slot.setAvailable(true);
                slot.setRequestTime(System.currentTimeMillis());
                Store.instance().saveSlot(slot);

            } catch (FetchException e) {
                log.warn("Could not fetch slot at " + slot.getUri() + ", will retry later...", e);
                slot.setAvailable(false);
                slot.setFailureCount(slot.getFailureCount() + 1);
                if (slot.getExpectedTime() + requestedChannel.getInsertInterval() > now) {
                    slot.setRequestTime(slot.getRequestTime() + requestedChannel.getInsertInterval());
                } else {
                    slot.setRequestTime(slot.getRequestTime() + slot.getFailureCount()
                            * requestedChannel.getInsertInterval() / 10);
                }
                Store.instance().saveSlot(slot);

                if (slot.getFailureCount() == 5
                        && Store.instance().slotIsAvailable(slot.getChannel(), slot.getIndex() - 2)) {
                    // create the next slot in the database
                    Slot nextSlot = new Slot();
                    nextSlot.setChannel(slot.getChannel());
                    nextSlot.setIndex(slot.getIndex() + 1);
                    if (!Store.instance().slotIsAvailable(nextSlot.getChannel(), nextSlot.getIndex())) {
                        log.error("It's the fifth time I couldn't fetch slot at " + slot.getUri()
                                + ", will try the next slot as well)");
                        nextSlot.setExpectedTime(slot.getExpectedTime() + requestedChannel.getInsertInterval());
                        nextSlot.setRequestTime(now);
                        Store.instance().saveSlot(nextSlot);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to fetch slot at " + slot.getUri() + ", skipping this slot.", e);
                slot.setAvailable(true);
                slot.setFailureCount(slot.getFailureCount() + 1);
                slot.setInvalid(true);
                Store.instance().saveSlot(slot);

                // create the next slot in the database
                Slot nextSlot = new Slot();
                nextSlot.setChannel(slot.getChannel());
                nextSlot.setIndex(slot.getIndex() + 1);
                if (!Store.instance().slotIsAvailable(nextSlot.getChannel(), nextSlot.getIndex())) {
                    nextSlot.setExpectedTime(slot.getExpectedTime() + requestedChannel.getInsertInterval());
                    nextSlot.setRequestTime(now);
                    Store.instance().saveSlot(nextSlot);
                }
            }
        }
    }

    public FetchResult fetch(Slot slot) throws FetchException {
        FreenetURI freenetURI;
        StopWatch.start("fetching slot at " + slot.getUri());
        try {
            freenetURI = new FreenetURI(slot.getUri());
            HighLevelSimpleClient client = this.obtainClient();
            FetchResult result = client.fetch(freenetURI);
            return result;
        } catch (MalformedURLException e) {
            String msg = "Slot {0} has an invalid uri";
            throw new ParamRuntimeException(msg, slot.getUri(), e);
        } finally {
            StopWatch.stop();
        }

    }

    public final static Comparator<Slot> slotsPriorityComparator = new Comparator<Slot>() {

        public int compare(Slot o1, Slot o2) {

            int failureCompare = new Integer(o1.getFailureCount()).compareTo(new Integer(o2.getFailureCount()));
            if (failureCompare != 0) {
                return failureCompare;
            }

            if (!o1.getChannel().equals(o2.getChannel())) {
                int indexCompare = new Integer(o1.getIndex()).compareTo(new Integer(o2.getIndex()));
                if (indexCompare != 0) {
                    return indexCompare;
                }
            }

            long expected1 = o1.getRequestTime();
            long expected2 = o2.getRequestTime();

            return -(new Long(expected1).compareTo(new Long(expected2)));
        }

    };

    private void insertNextSlotOnPersonalChannel(User user, Channel personalChannel) {
        long lastInsertTime = personalChannel.getLastInsertTime();
        int lastSlot = personalChannel.getLastSlot();
        Slot slot = personalChannel.createNextSlot();

        log.info("starting insert of slot " + slot.getIndex() + " of channel " + personalChannel.getBasename());
        Store.instance().saveChannel(personalChannel);
        Store.instance().saveSlot(slot);

        Ping ping = new Ping();
        ping.setInsertTime(System.currentTimeMillis());
        ping.setSender(user.getPublicSSK());
        Store.instance().savePing(ping);

        List<AbstractSendable> messagesToBeSend = new LinkedList<AbstractSendable>();

        messagesToBeSend.addAll(Store.instance().getUnpublishedPings(user.getPublicSSK()));
        messagesToBeSend.addAll(Store.instance().getUnpublishedPongs(user.getPublicSSK()));
        messagesToBeSend.addAll(Store.instance().getUnpublishedBookmarks(user.getPublicSSK()));
        messagesToBeSend.addAll(Store.instance().getUnpublishedChannels(user.getPublicSSK()));

        try {
            log.info("there are " + messagesToBeSend.size() + " messages to be published");

            SlotWriter slotWriter = new XmlSlotWriter();
            slotWriter.setSlot(slot);
            slotWriter.setKeypair(user);

            slotWriter.writeObjects(messagesToBeSend);
            this.insert(slotWriter.getInsertUri(), slotWriter.getContentType(), slotWriter.toByteArray());

            log.info("Succesfully inserted slot at " + slot.getUri());
            slot.setAvailable(true);
            slot.setRequestTime(System.currentTimeMillis());
            Store.instance().saveSlot(slot);

            for (AbstractSendable sendable : messagesToBeSend) {
                sendable.setPublished(true);
                if (sendable instanceof Bookmark) {
                    Store.instance().saveBookmark((Bookmark) sendable);
                } else if (sendable instanceof Ping) {
                    Store.instance().savePing((Ping) sendable);
                } else if (sendable instanceof Pong) {
                    Pong pong = (Pong) sendable;

                    Store.instance().savePong(pong);
                } else if (sendable instanceof Channel) {
                    Store.instance().saveChannel((Channel) sendable);
                }
            }

        } catch (InserterException e) {
            log.warn("Could not insert slot " + slot.getUri() + ", will retry...", e);
            personalChannel.setLastInsertTime(lastInsertTime);
            personalChannel.setLastSlot(lastSlot);
            Store.instance().saveChannel(personalChannel);
        } catch (Exception e) {
            log.error("Failed to insert slot " + slot.getUri(), e);
            personalChannel.setLastInsertTime(lastInsertTime);
            personalChannel.setLastSlot(lastSlot);
            Store.instance().saveChannel(personalChannel);
        }
    }

    private void insert(String uri, String contentType, byte[] slotData) throws InserterException {
        try {
            FreenetURI insertUri = new FreenetURI(uri);
            log.info("Inserting" + slotData.length + " bytes of data with content type " + contentType + " with URI "
                    + uri);

            InsertBlock insertBlock = new InsertBlock(new ArrayBucket(slotData), new ClientMetadata(contentType),
                    insertUri);
            HighLevelSimpleClient client = this.obtainClient();
            client.getFetcherContext().localRequestOnly = true;
            StopWatch.start("Inserting " + slotData.length + " bytes");
            client.insert(insertBlock, false);
            StopWatch.stop();

        } catch (InserterException e) {
            throw e;
        } catch (Exception e) {
            String msg = "Failed to insert {0} bytes of data with content type {1} with URI {2}";
            throw new ParamRuntimeException(msg, slotData.length, contentType, uri, e);
        }
    }

    public HighLevelSimpleClient obtainClient() {
        return this.client;
    }

    static String randomString(int length) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            char c = (char) (Math.random() * 96 + 32);
            buf.append(c);
        }
        return buf.toString();
    }

    @Override
    public void terminate() {
        Logger.normal(BookmarkPlugin.class, this + " was asked to terminate: TODO implenent this");
        this.terminationRequested = true;
        Store.instance().shutDown();
    }

    @Override
    public String toString() {
        return "FredPlugin " + this.getClass().getName();
    }

}