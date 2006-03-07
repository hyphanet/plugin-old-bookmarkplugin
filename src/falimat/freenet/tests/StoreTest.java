package falimat.freenet.tests;

import java.util.List;

import falimat.beanstore.BeanStore;
import falimat.beanstore.GenericQuery;
import falimat.beanstore.StoreQuery;
import falimat.beanstore.test.BeanStoreTestBase;
import falimat.freenet.bookmarkplugin.model.Bookmark;
import falimat.freenet.bookmarkplugin.model.User;
import freenet.crypt.Yarrow;

public class StoreTest extends BeanStoreTestBase {
    public void testQueryUnpublished() {

        User user = new User("alice", new Yarrow());

        BeanStore<Bookmark> beanStore = this.factory.getBeanStore("bookmarks", Bookmark.class);

        Bookmark b1 = new Bookmark();
        b1.setPublished(true);
        b1.setUri("KSK@test.txt");
        b1.setSender(user.getPublicSSK());

        Bookmark b2 = new Bookmark();
        b2.setPublished(false);
        b2.setUri("KSK@gpl.txt");
        b2.setSender(user.getPublicSSK());

        beanStore.put(b1.getId(), b1);
        beanStore.put(b2.getId(), b2);

        GenericQuery query = StoreQuery.is("sender", user.getPublicSSK());
        List<Bookmark> results = beanStore.executeQuery(query);
        List<Bookmark> expected = beanStore.arrayToList(b1, b2);
        assertEquals(expected.size(), results.size());

        query = StoreQuery.is("sender", user.getPublicSSK()).andIs("published", true);
        results = beanStore.executeQuery(query);
        expected = beanStore.arrayToList(b1);
        assertEquals(expected.size(), results.size());
        assertEquals(b1.getId(), expected.get(0).getId());
        
        query = StoreQuery.is("sender", user.getPublicSSK()).andIs("published", false);
        results = beanStore.executeQuery(query);
        expected = beanStore.arrayToList(b2);
        assertEquals(expected.size(), results.size());
        assertEquals(b2.getId(), expected.get(0).getId());        
    }
}
