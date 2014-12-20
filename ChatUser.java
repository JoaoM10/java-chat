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

public class ChatUser implements Comparable<ChatUser> {

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

  // Overrides compareTo to implement comparable (needed because of HashMap)
  @Override
  public int compareTo(ChatUser a) {
    return this.nick.compareTo(a.nick);
  }
  
  public UserState getState() {
    return this.userState;
  }

  public String getNick() {
    return this.nick;
  }

  public ChatRoom getRoom() {
    return this.room;
  }

  public void leftRoom() {
    this.room = null;
  }

  public void joinRoom(ChatRoom newRoom) {
    this.room = newRoom;
  }

  public SocketChannel getSocketChannel() {
    return this.socketChannel;
  }

  public void setState(UserState newState) {
    this.userState = newState;
  }
  
  public void setNick(String newNick) {
    this.nick = newNick;
  }
  
}
