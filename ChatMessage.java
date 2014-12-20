import java.io.*;
import java.util.*;


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


enum MessageType { OK, ERROR, MESSAGE, NEWNICK, JOINED, LEFT, BYE, PRIVATE, UNDEFINED }

public class ChatMessage {

  // Message parts
  private MessageType messageType;
  private String messageFirstPart;
  private String messageSecondPart;

  // Initialize message
  public ChatMessage(MessageType _messageType, String _messageFirstPart, String _messageSecondPart) {
    this.messageType = _messageType;
    this.messageFirstPart = _messageFirstPart;
    this.messageSecondPart = _messageSecondPart;
  }
  
  // Prepare message to print
  public String toString(Boolean stylized) {
    String finalMsg = "";

    if(stylized){
      switch(this.messageType){
      case OK:
        finalMsg = "Command processed with success!";
        break;
      case ERROR:
        finalMsg = "There was an error processing your command!";
        break;
      case MESSAGE:
        finalMsg = this.messageFirstPart + ": " + this.messageSecondPart;
        break;
      case NEWNICK:                      
        finalMsg = this.messageFirstPart + " changed his nick to " + this.messageSecondPart + "!";
        break;
      case JOINED:
        finalMsg = this.messageFirstPart + " joined the room!";
        break;
      case LEFT:
        finalMsg = this.messageFirstPart + " left the room!";      
        break;
      case BYE:
        finalMsg = "Leaving is not always easy but it has to be done sometimes. Bye!";
        break;
      case PRIVATE:
        finalMsg = "(Private) " + this.messageFirstPart + ": " + this.messageSecondPart;      
        break;
      }
    }
    else{
      switch(this.messageType){
      case OK:
        finalMsg = "OK";
        break;
      case ERROR:
        finalMsg = "ERROR";      
        break;
      case MESSAGE:
        finalMsg = "MESSAGE " + this.messageFirstPart + " " + this.messageSecondPart;
        break;
      case NEWNICK:                      
        finalMsg = "NEWNICK " + this.messageFirstPart + " " + this.messageSecondPart;
        break;
      case JOINED:
        finalMsg = "JOINED " + this.messageFirstPart;
        break;
      case LEFT:
        finalMsg = "LEFT " + this.messageFirstPart;
        break;
      case BYE:
        finalMsg = "BYE";
        break;
      case PRIVATE:
        finalMsg = "PRIVATE " + this.messageFirstPart + " " + this.messageSecondPart;
        break;
      }
    }

    finalMsg += "\n";
    
    return finalMsg;
  }

  // Parse message
  public static ChatMessage parseString(String unparsedMessage) {
    MessageType _messageType = MessageType.UNDEFINED;
    String _messageFirstPart = "";
    String _messageSecondPart = "";

    String[] msgParts = unparsedMessage.split(" ");

    if (msgParts[0].equals("OK")) {
      _messageType = MessageType.OK;
    } else if (msgParts[0].equals("ERROR")) {
      _messageType = MessageType.ERROR;
    } else if (msgParts[0].equals("MESSAGE")) {
      _messageType = MessageType.MESSAGE;
      _messageFirstPart = msgParts[1];
      _messageSecondPart = msgParts[2];
    } else if (msgParts[0].equals("NEWNICK")) {
      _messageType = MessageType.NEWNICK;
      _messageFirstPart = msgParts[1];
      _messageSecondPart = msgParts[2];
    } else if (msgParts[0].equals("JOINED")) {
      _messageType = MessageType.JOINED;
      _messageFirstPart = msgParts[1];
    } else if (msgParts[0].equals("LEFT")) {
      _messageType = MessageType.LEFT;
      _messageFirstPart = msgParts[1];
    } else if (msgParts[0].equals("BYE")) {
      _messageType = MessageType.BYE;
    } else if (msgParts[0].equals("PRIVATE")) {    
      _messageType = MessageType.PRIVATE;
      _messageFirstPart = msgParts[1];
      _messageSecondPart = msgParts[2];
    }
    
    return (new ChatMessage(_messageType, _messageFirstPart, _messageSecondPart));
  }
}

