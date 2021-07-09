//聊天室主人
var username;
// 消息接收者
var toName;

var ws;


//登录后显示用户名和状态
$(function () {
    $.ajax({
        //是否异步,此项目此处必须是false
        async: false,
        //请求方式
        type: 'GET',
        //请求url
        url: "/getUsername",
        success: function (res) {
            console.log("读取到了用户信息");
            username = res;
            //$('#chatMeu').html('<p>用户：' + res + "<span style='float: right;color: greenyellow; height: 20px'>在线</span></p>")
        }
    });

    //创建websocket对象
    ws = new WebSocket("ws://localhost:8081/chat");


    // ws建立连接后
    ws.onopen = function () {
        console.log(username, "建立了连接！");
        $(".user-header-text").html(" 用户：" + username + "<span  style='color: green; float:right;'>在线</span>");
    }

    // 接收到信息后
    ws.onmessage = function (evt) {
        var dataStr = evt.data;
        var res = JSON.parse(dataStr);

        console.log(res);

        // 判断是否是信息消息
        if (res.systemMsgFlag) {
            // 系统消息
            // 1, 好友列表展示
            // 2, 系统广播展示

            var userListStr = "";
            var broadcastListStr = "";

            var names = res.message;

            for (var name of names) {

                if (name !== username) {
                    userListStr += "<li><a style='text-decoration: underline' onclick='chatWith(\"" + name + "\")'>" + name + "</a></li>";
                    broadcastListStr += "<li>您的好友 " + name + " 已上线！</li>";
                }

            }
            // 渲染信息
            $("#friendsList").html(userListStr);
            $("#systemMsg").html(broadcastListStr);


        } else {
            // 不是系统消息

            if (username != res.toName) {
                var cnt = "<div  class=\"atalk\"><span id=\"asay\">" + res.message + "</span></div>";
                $("#chatCnt").append(cnt);

                // 存储聊天消息
                var chatData = localStorage.getItem(toName);

                if (chatData != null) {
                    chatData += cnt;
                } else {
                    chatData = cnt;
                }

                localStorage.setItem(toName, chatData);

            }

        }

    }


    ws.onclose = function () {
        $(".user-header-text").html(" 用户：" + username + "<span  style='color: red; float:right;'>离线</span>");
    }

    $('#submit').click(function () {
        var data = $("#tex_content").val();
        $("#tex_content").val("")

        var msg = {
            'toName': toName,
            'message': data
        }

        var cnt = "<div  class=\"btalk\"><span id=\"bsay\">" + data + "</span></div>";
        $("#chatCnt").append(cnt);
        console.log(msg);
        ws.send(JSON.stringify(msg))

        // 存储聊天消息
        var chatData = localStorage.getItem(toName);

        if (chatData != null) {
            chatData += cnt;
        } else {
            chatData = cnt;
        }

        localStorage.setItem(toName, chatData);

    })

});

//点击好友列表后，执行的动作
function chatWith(name) {
    $("#chatMain")[0].style.display = "block";
    $(".user-chat-with").html("正在和 " + name + " 聊天");
    toName = name;
    $("#chatCnt").html("");

    let beforeChatContent = localStorage.getItem(toName);
    if (beforeChatContent != null) {
        $("#chatCnt").html(beforeChatContent);
    }
}

