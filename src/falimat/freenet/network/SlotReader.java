package falimat.freenet.network;

import java.util.List;

import xomat.util.ParamException;

import falimat.freenet.bookmarkplugin.model.Bookmark;

public interface SlotReader {

    void setKeypair(String bobPrivateSSK, String bobPublicSSK);

    void readMessages(byte[] slotBytes) throws ParamException;

    List<Bookmark> getBookmarks();

}
