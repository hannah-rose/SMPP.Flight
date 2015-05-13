/*
 * Copyright 2002-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package smpp.networking;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompDecoder;
import org.springframework.messaging.simp.stomp.StompEncoder;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;



/**
 * A simple STOMP over WebSocket client
 * <p>
 * Only covers connect, subscribe & receive, as well as the sending of messages.
 * It is not robust and not very well protected against errors. However, it
 * provides just enough for the purpose of testing the stock portfolio sample.
 * <p>
 * The client can be extended and adapted for more advanced testing scenarios.
 * 
 * @author Rossen Stoyanchev
 */
@WebSocket
public class SimpleStompClient {

    private static Log logger = LogFactory.getLog(SimpleStompClient.class);

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    public static final int CONNECT_TIMEOUT = 10;

    private final URI handshakeUri;

    private final WebSocketHttpHeaders handshakeHeaders;

    private Session session;
    private WebSocketClient client;

    private final Map<String, MessageHandler> subscriptionHandlers = new ConcurrentHashMap<String, MessageHandler>();

    private final AtomicInteger subscriptionIndex = new AtomicInteger(0);

    private final StompEncoder encoder = new StompEncoder();

    private final StompDecoder decoder = new StompDecoder();

    private MessageConverter messageConverter = new MappingJackson2MessageConverter();


    public SimpleStompClient(URI handshakeUri, WebSocketHttpHeaders handshakeHeaders) {
        this.handshakeUri = handshakeUri;
        this.handshakeHeaders = handshakeHeaders;
    }


    public void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }


    public MessageConverter getMessageConverter() {
        return this.messageConverter;
    }


    public void connect(MessageHandler handler) throws Exception {
        StompWebSocketHandler wsHandler = new StompWebSocketHandler(handler);
        /* 
         *  //need to handle those big
         * tracking frames //wscfb.setMaxTextMessageBufferSize(8192*4);
         * //StandardWebSocketClient client = new
         * StandardWebSocketClient(wscfb.getObject());
         */
        client = new WebSocketClient();
        client.start();
        ClientUpgradeRequest handShakeReq = new ClientUpgradeRequest();
        handShakeReq.setHeaders(handshakeHeaders);
//        List<HttpCookie> cookies = new ArrayList<HttpCookie>();
//        String jsesId = handshakeHeaders.getFirst("Cookie").split("=")[1];
//        cookies.add(new HttpCookie("JSESSIONID", jsesId));
//        handShakeReq.setCookies(cookies);
        try {
            Future<Session> f = client.connect(wsHandler, handshakeUri, handShakeReq);
            session = f.get(CONNECT_TIMEOUT, TimeUnit.SECONDS);
            // this.session = client.doHandshake(wsHandler,
            // this.handshakeHeaders, this.handshakeUri).get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


    public void subscribe(String destination, MessageHandler messageHandler) {

        String id = String.valueOf(this.subscriptionIndex.getAndIncrement());
        this.subscriptionHandlers.put(id, messageHandler);

        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        headers.setSubscriptionId(id);
        headers.setDestination(destination);

        Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).setHeaders(headers).build();
        byte[] bytes = encoder.encode(message);
        try {
            this.session.getRemote().sendString(new String(bytes, DEFAULT_CHARSET));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public void send(String destination, Object payload) {

        StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.SEND);
        headers.setDestination(destination);

        Message<byte[]> message =
            (Message<byte[]>) this.messageConverter.toMessage(payload, new MessageHeaders(headers.toMap()));

        byte[] bytes = this.encoder.encode(message);

        try {
            this.session.getRemote().sendString(new String(bytes, DEFAULT_CHARSET));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }


    public void disconnect() {
        if (this.session != null) {

            this.session.close();

        }
    }
    @WebSocket
    public class StompWebSocketHandler {

        private final MessageHandler connectedHandler;


        private StompWebSocketHandler(MessageHandler connectedHandler) {
            this.connectedHandler = connectedHandler;
        }



        @OnWebSocketConnect
        public void afterConnectionEstablished(Session session) throws IOException {
            logger.debug("Connection established");
            StompHeaderAccessor headers = StompHeaderAccessor.create(StompCommand.CONNECT);
            headers.setAcceptVersion("1.1,1.2");
            headers.setHeartbeat(0, 0);
            Message<byte[]> message = MessageBuilder.withPayload(new byte[0]).setHeaders(headers).build();

            String msgString = new String(encoder.encode(message), DEFAULT_CHARSET);
            TextMessage textMessage = new TextMessage(msgString);
            session.getRemote().sendString(msgString);
        }

        //dead code
        protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
            logger.error("got binary message: capacity " + message.getPayload().capacity());
        }


        @OnWebSocketClose
        public void afterConnectionClosed(Session session, int status, String string) throws Exception {
            logger.warn("connection closed. Status: " + status + " string: " + string);
        };


        @OnWebSocketMessage
        public void handleTextMessage(Session session, String sMessage) throws Exception {
            logger.debug("handleTextMessage");
            TextMessage textMessage = new TextMessage(sMessage);
            ByteBuffer payload = ByteBuffer.wrap(textMessage.getPayload().getBytes(DEFAULT_CHARSET));
            Message<byte[]> message = (Message<byte[]>) decoder.decode(payload);
            if (message == null) {
                logger.error("Invalid message: " + message);
            }

            StompHeaderAccessor headers = StompHeaderAccessor.wrap(message);
            if (StompCommand.CONNECTED.equals(headers.getCommand())) {
                this.connectedHandler.handleMessage(message);
            } else if (StompCommand.MESSAGE.equals(headers.getCommand())) {
                String subscriptionId = headers.getSubscriptionId();
                MessageHandler subscriptionHandler = SimpleStompClient.this.subscriptionHandlers.get(subscriptionId);
                if (subscriptionHandler == null) {
                    logger.error("No subscribed handler for message: " + message);
                }
                subscriptionHandler.handleMessage(message);
            }
        }
    }
    
    public void close() throws Exception{
    	session.close(CloseStatus.NORMAL.getCode(), "SimpleStompClient.close() called");
    	client.stop();
    }
}