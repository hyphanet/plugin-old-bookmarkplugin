package falimat.freenet.network;

import java.util.List;

import falimat.freenet.bookmarkplugin.model.AbstractSendable;
import falimat.freenet.bookmarkplugin.model.Slot;
import falimat.freenet.bookmarkplugin.model.User;

public interface SlotWriter {

    String getContentType();
    
    void setKeypair(User inserter);
    
    void  writeObjects(List<AbstractSendable> sendables);

    byte[] toByteArray();

    void setSlot(Slot slot);

    String getInsertUri();
    
}
