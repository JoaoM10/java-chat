import java.io.*;
import java.util.*;


   //              ___,@
   //             /  <
   //        ,_  /    \  _,
   //    ?    \`/______\`/
   // ,_(_).  |; (e  e) ;|
   //  \___ \ \/\   7  /\/    _\8/_
   //      \/\   \'=='/      | /| /|
   //       \ \___)--(_______|//|//|
   //        \___  ()  _____/|/_|/_|
   //           /  ()  \    `----'
   //          /   ()   \
   //         '-.______.-'
   //       _    |_||_|    _
   //      (@____) || (____@)
   //       \______||______/


public class ChatRoom {

  // Room info
  private String name;
  private Set<ChatUser> users;

  // Initialize room
  public ChatRoom(String _name) {
    this.name = _name;
    this.users = new TreeSet<ChatUser>();
  }

  public ChatUser[] getUsers() {
    return this.users.toArray(new ChatUser[this.users.size()]);
  }

  public String getName() {
    return this.name;
  }

  // User entered this room
  public void userJoin(ChatUser user) {
    this.users.add(user);
  }

  // User left this room
  public void userLeft(ChatUser user) {
    this.users.remove(user);
  }

}
