import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.regex.*;


//     /`   `'.
//    /   _..---;
//    |  /__..._/  .--.-.
//    |.'  e e | ___\_|/____
//   (_)'--.o.--|    | |    |
//  .-( `-' = `-|____| |____|
// /  (         |____   ____|
// |   (        |_   | |  __|
// |    '-.--';/'/__ | | (  `|
// |      '.   \    )"";--`\ /
// \        ;   |--'    `;.-'
// |`-.__ ..-'--'`;..--'`


enum UserState { INIT, OUTSIDE, INSIDE }

public class ChatUser {

  // User info
  private String nick;
  private UserState userState;
  private SocketChannel socketChannel;
  private ChatRoom room;

  // Initialize user
  public ChatUser(SocketChannel _socketChannel) {
    this.userState = UserState.INIT;
    this.socketChannel = _socketChannel;
    this.nick = "";
    this.room = null;
  }


}
