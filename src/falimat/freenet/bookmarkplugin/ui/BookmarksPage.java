/**
 * 
 */
package falimat.freenet.bookmarkplugin.ui;

import falimat.freenet.bookmarkplugin.components.BookmarkList;
import falimat.freenet.bookmarkplugin.components.BookmarkSearchForm;
import falimat.freenet.bookmarkplugin.storage.Store;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;

public class BookmarksPage extends HtmlPage {

    
    BookmarkSearchForm searchForm;
    
    BookmarkList list;

    public BookmarksPage(String name) {
        super(name);
    }

    @Override
    public void constructComponents() {
        
        this.searchForm = new BookmarkSearchForm();
        this.addComponent(this.searchForm);
        
        this.list = new BookmarkList();
        this.addComponent(this.list);
        
        this.searchForm.setResultList(this.list);
        
        this.list.setBookmarks(Store.instance().getAllBookmarks());            
    }

    @Override
    public void renderHtml(HtmlWriter out) {
        super.renderHtml(out);
    }
}