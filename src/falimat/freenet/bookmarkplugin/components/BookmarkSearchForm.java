package falimat.freenet.bookmarkplugin.components;

import java.util.List;
import java.util.Set;

import falimat.freenet.bookmarkplugin.model.Bookmark;
import falimat.freenet.bookmarkplugin.storage.Store;
import falimat.freenet.network.RegExes;
import falimat.freenet.webplugin.FormAction;
import falimat.freenet.webplugin.components.Form;
import falimat.freenet.webplugin.components.Heading;
import falimat.freenet.webplugin.components.LabeledDropdown;
import falimat.freenet.webplugin.components.LabeledTextField;
import falimat.freenet.webplugin.components.SubmitButton;
import freenet.pluginmanager.HTTPRequest;

public class BookmarkSearchForm extends Form {

    private Heading heading = new Heading(2, "Search for bookmarks...");

    private LabeledTextField textQueryField = new LabeledTextField("textQuery", "...that contain one of these words");

    private LabeledTextField tagQueryField = new LabeledTextField("tagQuery", "...that have one of these tags");

    private LabeledDropdown contentTypeDropdown = new LabeledDropdown("contentTypeQuery",
            "...that have a specific content type", true, "text/plain", "text/html", "image/jpeg");

    private SubmitButton searchButton = new SubmitButton("Search");

    private BookmarkList resultList = new BookmarkList();
    
    public BookmarkSearchForm() {
        this.constructComponents();
    }

    protected void constructComponents() {
        super.addComponent(this.heading);
        this.addComponent(this.textQueryField);
        this.addComponent(this.tagQueryField);
        this.addComponent(this.contentTypeDropdown);
        this.addComponent(this.searchButton);

        this.tagQueryField.setValidation("Tags must be lower case and a-z only.", RegExes.TAGS);
        
        this.searchButton.setAction(new FormAction(this) {

            @Override
            protected void onSubmit(HTTPRequest request) {
                String text = textQueryField.getValue();
                Set<String> tags = BookmarkEditor.tagsAsSet(tagQueryField.getValue());
                String contentType = contentTypeDropdown.getValue();
                List<Bookmark> results = Store.instance().queryBookmarks(text, tags, contentType);
                if (resultList!=null) {
                    resultList.setBookmarks(results);
                }
            }

            @Override
            protected void onInvalidSubmission(HTTPRequest request) {
                // TODO Auto-generated method stub

            }

        });
    }

    public void setResultList(BookmarkList list) {
       this.resultList = list;
    }
}
