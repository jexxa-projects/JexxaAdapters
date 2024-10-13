package io.jexxa.common.drivingadapter.messaging.jms.listener;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Arrays;

import static io.jexxa.common.facade.logger.SLF4jLogger.getLogger;


@SuppressWarnings("unused")
public abstract class StringMessageListener implements MessageListener
{
    private Message currentMessage;
    private String currentMessageText;

    public abstract void onMessage(String message);

    @Override
    public final void onMessage(Message message)
    {
        try
        {
            this.currentMessage = message;
            if (message instanceof TextMessage)
            {
                TextMessage textMessage = (TextMessage)currentMessage;
                this.currentMessageText = textMessage.getText();
            } else if ( message instanceof BytesMessage) {
                BytesMessage byteMessage = (BytesMessage) currentMessage;
                byte[] payload = new byte[(int) byteMessage.getBodyLength()];
                byteMessage.readBytes(payload);
                this.currentMessageText = Arrays.toString(payload);
            } else {
                getLogger(getClass()).error("Received message is neither of type Text message nor Byte message -> Discard it. Reason: Invalid Message type");
            }

            onMessage( currentMessageText );
        }
        catch (JMSException exception)
        {
            //In case of a JMS exception, we assume that data cannot be read due to some internal JMS issues and discard the message
            getLogger(getClass()).error("Could not process received message as text or byte message -> Discard it. Reason: {}", exception.getMessage());
        }
        currentMessage = null;
        currentMessageText = null;
    }

    protected final Message getCurrentMessage()
    {
        return currentMessage;
    }
    protected final String getCurrentTextMessage()
    {
        return currentMessageText;
    }

}

