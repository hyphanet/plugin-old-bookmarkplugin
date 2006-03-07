package falimat.freenet.bookmarkplugin.components;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import falimat.freenet.bookmarkplugin.model.Bookmark;
import falimat.freenet.bookmarkplugin.storage.Store;
import falimat.freenet.network.RegExes;
import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.Action;
import falimat.freenet.webplugin.ActionComponent;
import falimat.freenet.webplugin.FormAction;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;
import falimat.freenet.webplugin.components.DefinitionList;
import falimat.freenet.webplugin.components.Form;
import falimat.freenet.webplugin.components.Heading;
import falimat.freenet.webplugin.components.LabeledDropdown;
import falimat.freenet.webplugin.components.LabeledTextArea;
import falimat.freenet.webplugin.components.LabeledTextField;
import falimat.freenet.webplugin.components.SubmitButton;
import freenet.pluginmanager.PluginHTTPRequest;

public class BookmarkEditor extends AbstractHtmlComponent implements ActionComponent {

    private Bookmark bookmark;

    private Form form;

    private DefinitionList readOnlyProperties = new DefinitionList();

    private LabeledTextField titleField;

    private LabeledTextField tagsField;

    private LabeledDropdown ratingDropdown;

    private LabeledTextArea descriptionArea;

    private SubmitButton saveButton;

    public BookmarkEditor() {
    }

    public void setBookmark(Bookmark b) {
        this.bookmark = b;
        this.form = new Form();

        if (this.bookmark == null) {
            return;
        }

        this.form.addComponent(new Heading(2, "Edit Bookmark"));
        
        this.readOnlyProperties = new DefinitionList();
        this.readOnlyProperties.addEntry("URI", bookmark.getUri());
        this.readOnlyProperties.addEntry("Time", DateFormat.getDateTimeInstance().format(new Date(bookmark.getLastModified())));
        this.readOnlyProperties.addEntry("Content-Type", bookmark.getContentType());
        this.readOnlyProperties
                .addEntry("Size", NumberFormat.getNumberInstance().format(bookmark.getSize()) + " bytes");
        this.form.addComponent(this.readOnlyProperties);

        this.titleField = new LabeledTextField("title", "Title");
        this.titleField.setRequired(true);
        this.titleField.setValue(this.bookmark.getTitle());
        this.form.addComponent(this.titleField);

        this.tagsField = new LabeledTextField("tags", "Tags");
        this.tagsField.setRequired(true);
        this.tagsField.setValidation("Tags must be lower-case and seperated by comma or whitespace", RegExes.TAGS);
        this.tagsField.setValue(tagsAsString(this.bookmark.getTags()));
        this.form.addComponent(this.tagsField);

        this.ratingDropdown = new LabeledDropdown("rating", "Rating", true, "5", "4", "3", "2", "1", "0");
        this.form.addComponent(this.ratingDropdown);

        this.descriptionArea = new LabeledTextArea("description", "Description");
        this.descriptionArea.setRequired(false);
        this.descriptionArea.setValue(this.bookmark.getDescription());
        this.form.addComponent(this.descriptionArea);

        this.saveButton = new SubmitButton("Save");
        this.saveButton.setAction(new FormAction(this.form) {

            @Override
            protected void onSubmit(PluginHTTPRequest request) {
                bookmark.setTitle(titleField.getValue());
                bookmark.setDescription(descriptionArea.getValue());
                bookmark.setRating(request.getIntParam(ratingDropdown.getName(), -1));
                bookmark.setPublished(false);
                bookmark.setLastModified(System.currentTimeMillis());
                bookmark.setTags(tagsAsSet(tagsField.getValue()));

                Store.instance().saveBookmark(bookmark);
                setBookmark(null);
            }

            @Override
            protected void onInvalidSubmission(PluginHTTPRequest request) {

            }

        });
        this.form.addComponent(this.saveButton);

    }

    private String tagsAsString(Set<String> tags) {
       StringBuffer buf = new StringBuffer();
       for(String tag : tags) {
           if (buf.length()>0) {
               buf.append(' ');
           }
           buf.append(tag);
       }
       return buf.toString();
    }

    public static Set<String> tagsAsSet(String tagString) {
        StringTokenizer tagTokenizer = new StringTokenizer(tagString, " ,;");
        SortedSet<String> tags = new TreeSet<String>();
        while (tagTokenizer.hasMoreTokens()) {
            tags.add(tagTokenizer.nextToken());
        }       
        return tags;
    }
    
    public Action getAction(String id) {
        if (this.form == null) {
            return null;
        } else {
            return this.form.getAction(id);
        }
    }

    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {

        if (this.bookmark != null) {
            out.beginDiv("bookmark_editor");

            this.form.renderHtml(out, contextPage);

            out.endDiv();
        }

    }

}
