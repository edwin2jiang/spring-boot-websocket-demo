package com.yyj.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyj.pojo.Message;
import com.yyj.util.MessageUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Z09418208_蒋伟伟
 * @Description
 * @create 2021-07-04 21:14
 */
@ServerEndpoint(value = "/chat", configurator = GetHttpSessionConfigurator.class)
@Component
public class ChatEndpoint {
    // 用来存储每一个客户端对应的ChatEndpoint对象
    private static Map<String, ChatEndpoint> onlineUsers = new ConcurrentHashMap<>();

    // 声明session对象，通过该对象可以发送消息给指定的用户
    private Session session;

    // 声明一个HttpSession对象，我们之前在HttpSession对象中存储了用户名
    private HttpSession httpSession;


    /**
     * 连接建立时被调用
     *
     * @param session
     * @param config
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        this.session = session;
        this.httpSession = httpSession;

        System.out.println("onOpen执行了");

        // 从httpSession对象中获取用户名
        String username = (String) httpSession.getAttribute("username");

        System.out.println(username);
        System.out.println(this);
        System.out.println(this.onlineUsers);

        // 将当前对象存储到容器中
        this.onlineUsers.put(username, this);

        // 将当前用户推送给所有用户
        // 1, 获取消息
        String names = MessageUtils.getMessage(true, null, getNames());
        // 2. 调用方法进行系统消息的推送
        broadcastAllUsers(names);
    }

    private Set<String> getNames() {
        return onlineUsers.keySet();
    }

    private void broadcastAllUsers(String message) {
        // 要将该消息推送给所有的客户端
        Set<String> names = onlineUsers.keySet();

        for (String name : names) {

            ChatEndpoint chatEndpoint = onlineUsers.get(name);
            try {
                chatEndpoint.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 接收到客户端消息时被调用
     *
     * @param message
     * @param session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println(message);

        try {
            ObjectMapper mapper = new ObjectMapper();
            Message msg = mapper.readValue(message, Message.class);

            // 获取发送用户

            String toName = msg.getToName();
            String text = msg.getMessage();

            String resultMsg = MessageUtils.getMessage(false, toName, text);

            onlineUsers.get(toName).session.getBasicRemote().sendText(resultMsg);

        } catch (Exception e) {

            e.printStackTrace();
            
        }

    }


    /**
     * 连接关闭时被调用
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
        String username = (String) httpSession.getAttribute("username");


        onlineUsers.remove(username);
        Set<String> names = onlineUsers.keySet();

        String msg = MessageUtils.getMessage(true,null,names);

        broadcastAllUsers(msg);
    }
}
