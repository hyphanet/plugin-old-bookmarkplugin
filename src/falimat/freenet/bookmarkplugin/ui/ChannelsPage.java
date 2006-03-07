/**
 * 
 */
package falimat.freenet.bookmarkplugin.ui;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import falimat.freenet.bookmarkplugin.LoginCredentials;
import falimat.freenet.bookmarkplugin.model.Channel;
import falimat.freenet.bookmarkplugin.model.Slot;
import falimat.freenet.bookmarkplugin.model.User;
import falimat.freenet.bookmarkplugin.storage.Store;
import falimat.freenet.network.RegExes;
import falimat.freenet.webplugin.FormAction;
import falimat.freenet.webplugin.HtmlPage;
import falimat.freenet.webplugin.components.BeanTable;
import falimat.freenet.webplugin.components.BeanTableDataSource;
import falimat.freenet.webplugin.components.Form;
import falimat.freenet.webplugin.components.Heading;
import falimat.freenet.webplugin.components.LabeledTextField;
import falimat.freenet.webplugin.components.SubmitButton;
import freenet.pluginmanager.PluginHTTPRequest;

public class ChannelsPage extends HtmlPage {
    final BeanTable channelsTable = new BeanTable();

    final BeanTable slotsTable = new BeanTable();
    
    final BeanTable toBeFetchedTable = new BeanTable();

    final Form form = new Form();

    final LabeledTextField addChannelUri = new LabeledTextField("uri", "Add Channel with URI");

    final SubmitButton addChannelButton = new SubmitButton("add");

    final Form channelSettingsForm = new Form();
    
    public ChannelsPage(String name) {
        super(name);

    }

    @Override
    public void constructComponents() {

        this.addChannelUri.setRequired(true);
        this.addChannelUri.setValidation("URIs of channels must be formatted like SSK@abc.../basename-1",
                RegExes.CHANNEL_URI);

        this.addChannelButton.setAction(new FormAction(this.form) {

            @Override
            protected void onSubmit(PluginHTTPRequest request) {
                try {
                    String channelUri = addChannelUri.getValue();
                    Channel channel = new Channel();
                    String ssk = StringUtils.substringBefore(channelUri, "/");
                    String baseName = StringUtils.substringBetween(channelUri, "/", "-");
                    int index = Integer.parseInt(StringUtils.substringAfterLast(channelUri, "-"));
                    channel.setSender(ssk);
                    channel.setBasename(baseName);
                    channel.setLastSlot(index);

                    Store.instance().saveChannel(channel);
                    
                    for (int i=0; i<=index; i++) {
                        Slot slot = new Slot(channel, i);
                        if (!Store.instance().slotIsAvailable(slot.getChannel(), i)) {
                            slot.setRequestTime(System.currentTimeMillis());
                            Store.instance().saveSlot(slot);
                        }
                    }
                    
                    form.resetFormValues();
                } catch (RuntimeException e) {
                    addChannelUri.showValidationMessage("This channel URI could not be parsed. <!--\n\n"
                            + HtmlPage.getStacktrace(e) + "-->");
                }

            }

            @Override
            protected void onInvalidSubmission(PluginHTTPRequest request) {
                // TODO Auto-generated method stub

            }

        });

        this.form.addComponent(this.addChannelUri);
        this.form.addComponent(this.addChannelButton);
        super.addComponent(this.form);

        super.addComponent(new Heading(2, "All known channels"));
        channelsTable.addColumn("sender", BeanTable.PUBLIC_SSK_REND);
        channelsTable.addColumn("basename");
        channelsTable.addColumn("lastSlot");
        channelsTable.addColumn("lastInsertTime", BeanTable.DATE_TIME_RENDERER);
        channelsTable.addColumn("insertInterval", BeanTable.DURATION_RENDERER);

        channelsTable.setDataProvider(new BeanTableDataSource() {

            public Object[] getRows() {
                return Store.instance().getAllChannels().toArray();
            }

        });
        super.addComponent(channelsTable);

        super.addComponent(new Heading(2, "Slots to be fetched"));
        toBeFetchedTable.addColumn("uri", BeanTable.FREENET_LINK_RENDERER);
        toBeFetchedTable.addColumn("expectedTime", BeanTable.DATE_TIME_RENDERER);
        toBeFetchedTable.addColumn("available");
        toBeFetchedTable.addColumn("failureCount");
        toBeFetchedTable.addColumn("requestTime", BeanTable.DATE_TIME_RENDERER);
        

        toBeFetchedTable.setDataProvider(new BeanTableDataSource() {

            public Object[] getRows() {
                User currentUser = LoginCredentials.instance().getCurrentUser();
                List<Slot> slots = Store.instance().getSlotsToBeFetched(currentUser);
                if (slots==null) {
                    return new Object[0];
                } else {
                    return slots.toArray();
                }
            }

        });
        super.addComponent(toBeFetchedTable);
        
        super.addComponent(new Heading(2, "All Slots"));
        slotsTable.addColumn("uri", BeanTable.FREENET_LINK_RENDERER);
        slotsTable.addColumn("expectedTime", BeanTable.DATE_TIME_RENDERER);
        slotsTable.addColumn("available");
        slotsTable.addColumn("failureCount");
        slotsTable.addColumn("requestTime", BeanTable.DATE_TIME_RENDERER);

        slotsTable.setDataProvider(new BeanTableDataSource() {

            public Object[] getRows() {
                return Store.instance().getAllSlots().toArray();
            }

        });
        super.addComponent(slotsTable);
        
        
        this.channelSettingsForm.addComponent(new Heading(2, "Publishing preferences"));
        
        final LabeledTextField insertIntervalField = new LabeledTextField("intervalMinutes", "How many minutes should pass between two inserts on your channel?");
        insertIntervalField.setRequired(true);
        insertIntervalField.setValidation("the number of minutes must be entered as integer", RegExes.INTEGER);
        this.channelSettingsForm.addComponent(insertIntervalField);
        
        final SubmitButton submitButton = new SubmitButton("Apply Changes");
        submitButton.setAction(new FormAction(this.channelSettingsForm) {

            @Override
            protected void onSubmit(PluginHTTPRequest request) {
                User user = LoginCredentials.instance().getCurrentUser();
                if (user==null) {
                    insertIntervalField.showValidationMessage("You must be logged in to change publish preferences");
                    return;
                }
                Channel channel = Store.instance().getChannelBySender(user.getPublicSSK());
                if (channel==null) {
                    insertIntervalField.showValidationMessage("There doesn't exist a channel for the loged in user");
                    return;
                }
                int insertIntervalMinutes = request.getIntParam(insertIntervalField.getName(), channel.getInsertInterval());
                channel.setInsertInterval((int)DateUtils.MILLIS_PER_MINUTE * insertIntervalMinutes);
                channel.setLastInsertTime(System.currentTimeMillis() - channel.getInsertInterval());
                channel.setLastModified(System.currentTimeMillis());
                channel.setPublished(false);
                Store.instance().saveChannel(channel);
            }

            @Override
            protected void onInvalidSubmission(PluginHTTPRequest request) {
                // TODO Auto-generated method stub
                
            }
            
        });
        this.channelSettingsForm.addComponent(submitButton);
        
        super.addComponent(this.channelSettingsForm);

    }

}