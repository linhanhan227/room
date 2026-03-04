这是 STOMP 帧格式问题。STOMP 帧必须以 NULL 字符（ \x00 或 ^@ ）结尾，且每行以换行符结束。

正确的 STOMP 帧格式 ：

```
SEND
destination:/app/chat.send.100
content-type:application/json

{"roomId":100,"content":"大家好！",
"type":"TEXT"}^@
```
