package falimat.freenet.bookmarkplugin.model;

import java.util.Set;
import java.util.TreeSet;

public class Bookmark  extends AbstractSendable{
    private String uri; 

    private long size;

    private String contentType;

    private String title;


    private String description;

    private int rating;
 
    private Set<String> tags = new TreeSet<String>();

    public String getId() {
        return this.uri + this.getSender(); 
    }

    public String getContentType() {
        return this.contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getRating() {
        return this.rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Set<String> getTags() {
        return this.tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
