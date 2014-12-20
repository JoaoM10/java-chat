import java.io.*;
import java.util.*;
import java.util.regex.*;


   //                  .------,
   //    .\/.          |______|
   //  _\_}{_/_       _|_Ll___|_
   //   / }{ \       [__________]          .\/.
   //    '/\'        /          \        _\_\/_/_
   //               ()  o  o    ()        / /\ \
   //                \ ~~~   .  /          '/\'
   //           _\/   \ '...'  /    \/_
   //            \\   {`------'}    //
   //             \\  /`---/',`\\  //
   //              \/'  o  | |\ \`//
   //              /'      | | \/ /\
   // __,. -- ~~ ~|    o   `\|      |~ ~~ -- . __
   //             |                 |
   //             \    o            /
   //              `._           _.'
   //                 ^~- . -  ~^ 


enum MessageType { OK, ERROR, MESSAGE, NEWNICK, JOINED, LEFT, BYE, PRIVATE }

public class ChatMessage {
  private MessageType messageType;
  private String message;
  private String author;

  public ChatMessage(MessageType _messageType, String _message, String _author) {
    this.messageType = _messageType;
    this.message = _message;
    this.author = _author;
  }

  public String toString() {
    String finalMsg = "";
    finalMsg = this.author + ": " + this.message;
    return finalMsg;
  }
  
  public static ChatMessage parseString(String unparsedMessage) {
    MessageType _messageType = MessageType.MESSAGE;
    String _message = "Some text";
    String _author = "Someone";
    return (new ChatMessage(MessageType.MESSAGE, _message, _author));
  }
}

