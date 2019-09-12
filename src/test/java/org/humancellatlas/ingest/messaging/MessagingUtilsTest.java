package org.humancellatlas.ingest.messaging;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
public class MessagingUtilsTest {

    @Test
    public void testBasicAck() throws Exception {
        Channel mockChannel = mock(Channel.class);
        doNothing().when(mockChannel).basicAck(anyLong(), anyBoolean());

        MessagingUtils.basicAck(mockChannel, 1L, false, false);
        verify(mockChannel).basicAck(1L, false);

        MessagingUtils.basicAck(mockChannel, 1L, true, false);
        verify(mockChannel).basicNack(1L, false, false);

        MessagingUtils.basicAck(mockChannel, 1L, true, true);
        verify(mockChannel).basicNack(1L, false, true);
    }
}
