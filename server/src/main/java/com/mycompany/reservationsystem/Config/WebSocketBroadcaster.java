package com.mycompany.reservationsystem.Config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketBroadcaster {

    private static SimpMessagingTemplate template;

    @Autowired
    public WebSocketBroadcaster(SimpMessagingTemplate template) {
        WebSocketBroadcaster.template = template;
    }
    public void broadcast(String destination, Object message) {
        template.convertAndSend(destination, message);
    }
    public void sendToUser(String account, String destination, Object message) {
        template.convertAndSendToUser(account, destination, message);
    }


}
