package falimat.freenet.bookmarkplugin.components;

import java.text.NumberFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import falimat.freenet.bookmarkplugin.model.Bookmark;
import falimat.freenet.bookmarkplugin.storage.Store;
import falimat.freenet.webplugin.AbstractHtmlComponent;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.HtmlWriter;

public class BookmarkList extends AbstractHtmlComponent {

    public final static String FREENET_LINK_WARNING = "WARNING: This is a link to freenet content. Follow it on your own risk.";

    private String title = "Search results";

    private Map<String, List<Bookmark>> uriBookmarkMap = new TreeMap<String, List<Bookmark>>();

    public void renderHtml(HtmlWriter out, HtmlPage contextPage) {
        out.beginDiv("bookmark_list");

        out.write("<h2>" + this.title + "</h2>");

        out.write("<ul class=\"results\">");

        for (String key : this.uriBookmarkMap.keySet()) {
            out.write("<li>");
            this.writeBookmarkTeaser(out, contextPage, this.uriBookmarkMap.get(key));
            out.write("</li>");
        }

        out.write("</ul>");

        out.endDiv();
    }

    private Map<String, StringBuffer> createTagMap(List<Bookmark> bookmarks) {
        Map<String, StringBuffer> map = new TreeMap<String, StringBuffer>();
        for (Bookmark b : bookmarks) {
            for (String tag : b.getTags()) {
                if (map.containsKey(tag)) {
                    map.get(tag).append(", " + Store.instance().getNick(b.getSender()));
                } else {
                    StringBuffer buf = new StringBuffer();
                    buf.append("tagged '"+tag+"' by " + Store.instance().getNick(b.getSender()));
                    map.put(tag, buf);
                }
            }
        }
        return map;
    }

    private void writeBookmarkTeaser(HtmlWriter out, HtmlPage contextPage, List<Bookmark> groupedBookmarks) {
        out.beginDiv("teaser");

        boolean isFirstTitle = true;
        Set<String> titlesDisplayed = new TreeSet<String>();

        int ratingCount = 0;
        double averageRating = 0;
        StringBuffer ratingTooltip = new StringBuffer("rated by ");
        for (Bookmark b : groupedBookmarks) {
            if (titlesDisplayed.contains(b.getTitle())) {
                continue;
            }
            if (isFirstTitle) {
                out.write("<h3>");
                out.writeTooltipItem("Title chosen by " + Store.instance().getNick(b.getSender()), b.getTitle(), null);
                out.write("</h3>");
                isFirstTitle = false;
            } else {
                out.write("<h4>");
                out.writeTooltipItem("Title chosen by " + Store.instance().getNick(b.getSender()), "aka '" + b.getTitle()
                        + "'", null);
                out.write("</h4>");

            }
            titlesDisplayed.add(b.getTitle());
            if (b.getRating() != -1) {
                ratingTooltip.append(Store.instance().getNick(b.getSender()) + ", ");
                ratingCount++;
                averageRating += b.getRating();
            }
            
        }
        if (ratingCount > 0) {
            averageRating /= ratingCount;
        }
        Bookmark b = groupedBookmarks.get(0);

        out.beginDiv("metadata");

        out.beginSpan("attributes");
        out.write(b.getContentType());
        out.write(", ");
        out.write(NumberFormat.getNumberInstance().format(b.getSize()) + " bytes");
        out.endSpan();
        out.write(" ");

        Map<String, StringBuffer> tagMap = this.createTagMap(groupedBookmarks);

        if (ratingCount > 0) {
            out.beginSpan("rating");
            String ratingString = this.createRatingString(averageRating);
            out.writeTooltipItem(ratingTooltip.toString(), ratingString, null);
            out.endSpan();
            out.write(" ");
        }

        out.beginSpan("tags");
        for (String tag : tagMap.keySet()) {
            String tooltip = tagMap.get(tag).toString();
            out.beginSpan("tag");
            out.writeTooltipItem(tooltip, tag, null);
            out.endSpan();
            out.write(" ");
        }
        out.endSpan();

        out.endDiv();

        out.write("<p class=\"description\">");

        for(Bookmark bo : groupedBookmarks) {
            if (bo.getDescription()!=null && bo.getDescription().length()>0) {
                String nick = Store.instance().getNick(bo.getSender());
                out.writeTooltipItem(bo.getSender(), nick+": ", "nick");
                out.append(bo.getDescription());
                out.write(" ");
            }
        }
        
        out.write("</p>");

        out.beginDiv("freenet_link");
        out.writeLink("/" + groupedBookmarks.get(0).getUri(), b.getUri(), FREENET_LINK_WARNING);
        out.endDiv();

        out.endDiv();
    }

    private String createRatingString(double averageRating) {
        StringBuffer out = new StringBuffer();

        String ratingString = NumberFormat.getNumberInstance().format(averageRating);
        if (ratingString.length() > 3) {
            ratingString = ratingString.substring(0, 3);
        }
        out.append("" + ratingString + "/5");
        return out.toString();
    }

    public void setBookmarks(List<Bookmark> bookmarks) {
        this.uriBookmarkMap.clear();

        for (Bookmark b : bookmarks) {
            String uri = b.getUri();
            List<Bookmark> bookmarksForUri = this.uriBookmarkMap.get(uri);
            if (bookmarksForUri == null) {
                bookmarksForUri = new LinkedList<Bookmark>();
                this.uriBookmarkMap.put(uri, bookmarksForUri);
            }
            bookmarksForUri.add(b);
        }
    }

}
