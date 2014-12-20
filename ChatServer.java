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


//                           |
//                         \ ' /
//                       -- (*) --
//                          >*<
//                         >0<@<
//                        >>>@<<*
//                       >@>*<0<<<
//                      >*>>@<<<@<<
//                     >@>>0<<<*<<@<
//                    >*>>0<<@<<<@<<<
//                   >@>>*<<@<>*<<0<*<
//     \*/          >0>>*<<@<>0><<*<@<<
// ___\\U//___     >*>>@><0<<*>>@><*<0<<
// |\\ | | \\|    >@>>0<*<0>>@<<0<<<*<@<<  
// | \\| | _(UU)_ >((*))_>0><*<0><@<<<0<*<
// |\ \| || / //||.*.*.*.|>>@<<*<<@>><0<<<
// |\\_|_|&&_// ||*.*.*.*|_\\||//_               
// """"|'.'.'.|~~|.*.*.*|     ____|_
//     |'.'.'.|   ^^^^^^|____|>>>>>>|
//     ~~~~~~~~         '""""`------'


public class ChatServer {

  // Buffer for the received data
  static private final ByteBuffer inBuffer = ByteBuffer.allocate( 16384 );

  // Decoder/Encoder for text transmission
  static private final Charset charset = Charset.forName("UTF8");
  static private final CharsetEncoder encoder = charset.newEncoder();
  static private final CharsetDecoder decoder = charset.newDecoder();

  // Users + Rooms vars
  static private HashMap<SocketChannel, ChatUser> users = new HashMap<SocketChannel, ChatUser>();
  static private HashMap<String, ChatUser> nicks = new HashMap<String, ChatUser>();
  static private HashMap<String, ChatRoom> rooms = new HashMap<String, ChatRoom>();

  // TCP Port validator
  public static Boolean validatePort(final String port) {
    String portPattern = "(\\d+)";
    Pattern pattern = Pattern.compile(portPattern);
    Matcher matcher = pattern.matcher(port);
    if (!matcher.matches())
      return false;
    Integer iport = Integer.parseInt(port);
    return (iport >= 0 && iport <= 65535);
  }  

  // Close connection to client
  public static void closeClient(SocketChannel socketChannel) throws IOException {
    
    Socket socket = socketChannel.socket();

    try {
      System.out.println("Closing connection to " + socket);
      socketChannel.close();
    } catch (IOException ex) {
      System.err.println("Error closing socket " + socket + "! (" + ex + ")");
    }

    // If closed before adding it to users
    if (!users.containsKey(socketChannel))
      return;

    ChatUser user = users.get(socketChannel);

    // May need to leave room
    if (user.getState() == UserState.INSIDE) {
      
      ChatRoom userRoom = user.getRoom();
      userRoom.userLeft(user);
      ChatUser[] usersSameRoom = userRoom.getUsers();
      ChatMessage chatMessage = new ChatMessage(MessageType.LEFT, user.getNick(), "");
      for (ChatUser to : usersSameRoom)
        sendMessage(to.getSocketChannel(), chatMessage);
      
      // Room can become empty
      if (usersSameRoom.length == 0)
        rooms.remove(userRoom.getName());
      
    }

    // Remove user from users and nicks info
    nicks.remove(user.getNick());
    users.remove(socketChannel);
  }
  
  // Server Main  
  public static void main(String args[]) throws Exception {

    if(args.length < 1) {
      System.out.println("Usage: chatServer <server port>");
      return;
    }

    String portStr = args[0];

    if (!validatePort(portStr)) {
      System.out.println("Invalid TCP port!");
      return;
    }

    Integer port  = Integer.parseInt(portStr);
    
    try {
      // Setup server
      ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.configureBlocking(false);
      ServerSocket serverSocket = serverSocketChannel.socket();
      InetSocketAddress isa = new InetSocketAddress(port);
      serverSocket.bind(isa);

      Selector selector = Selector.open();
      serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
      System.out.println("Server listening on port " + port);

      while (true) {
        int num = selector.select();

        if (num == 0)
          continue;

        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> it = keys.iterator();
        while (it.hasNext()) {
          
          SelectionKey key = it.next();

          if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {

            // Received a new incoming connection
            Socket socket = serverSocket.accept();
            System.out.println("Got connection from " + socket);

            SocketChannel socketChannel = socket.getChannel();
            socketChannel.configureBlocking(false);

            socketChannel.register(selector, SelectionKey.OP_READ);
            users.put(socketChannel, new ChatUser(socketChannel));
            
          } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {

            SocketChannel socketChannel = null;

            try {

              // Reveived data on a connection
              socketChannel = (SocketChannel) key.channel();
              boolean ok = processInput(socketChannel);

              // If the connection is dead, remove it from the selector and close it, and remove user also
              if (!ok) {
                key.cancel();
                closeClient(socketChannel);
              }

            } catch(IOException ex) {

              // On exception, remove this channel from the selector and remove user
              key.cancel();
              closeClient(socketChannel);
            }
          }
        }

        keys.clear();
      }
    } catch(IOException ex) {
      System.err.println(ex);
    }
  }

  // Send message on socket channel
  private static void sendMessage(SocketChannel socketChannel, ChatMessage message) throws IOException {
    socketChannel.write(encoder.encode(CharBuffer.wrap(message.toString(false))));
  }

  // Send error message to user
  private static void sendError(ChatUser to, String message) throws IOException {
    //ChatMessage chatMessage = new ChatMessage(MessageType.ERROR, message, "");
    ChatMessage chatMessage = new ChatMessage(MessageType.ERROR, "", "");
    sendMessage(to.getSocketChannel(), chatMessage);
  }

  // Send OK message to user
  private static void sendOk(ChatUser to) throws IOException  {
    ChatMessage chatMessage = new ChatMessage(MessageType.OK, "", "");
    sendMessage(to.getSocketChannel(), chatMessage);
  }

  // Send Bye message to user
  private static void sendBye(ChatUser to) throws IOException  {
    ChatMessage chatMessage = new ChatMessage(MessageType.BYE, "", "");
    sendMessage(to.getSocketChannel(), chatMessage);
  }
  
  // Process command
  private static void processCommand(String message, ChatUser sender) throws IOException  {
    String[] msgParts = message.split(" ");

    if(msgParts[0].equals("/nick")) {

      // Check command syntax
      if (msgParts.length != 2) {
        sendError(sender, "Invalid usage of this command: You need to provide a nick!");
        return;
      }

      String newNick = msgParts[1];

      // Check if the nick is already in use
      if (nicks.containsKey(newNick)){
        sendError(sender, "This nick is already in use!");
        return;
      }

      // Update
      String oldNick = sender.getNick();
      nicks.remove(oldNick);
      sender.setNick(newNick);
      nicks.put(newNick, sender);
      sendOk(sender);

      if(sender.getState() == UserState.INIT)
        sender.setState(UserState.OUTSIDE);
      else if (sender.getState() == UserState.INSIDE) {
        ChatUser[] usersSameRoom = sender.getRoom().getUsers();
        ChatMessage chatMessage = new ChatMessage(MessageType.NEWNICK, oldNick, newNick);
        
        for (ChatUser to : usersSameRoom)
          if (sender != to)
            sendMessage(to.getSocketChannel(), chatMessage);
      }
      
    } else if(msgParts[0].equals("/join")) {

      // Check command syntax
      if (msgParts.length != 2) {
        sendError(sender, "Invalid usage of this command: You need to provide a room name!");
        return;
      }

      // Make sure user is not in INIT state
      if (sender.getState() == UserState.INIT){
        sendError(sender, "You need to define a nick before joining a room!");
        return;
      }
      
      // If already on a room, leave it
      if (sender.getState() == UserState.INSIDE) {
        ChatRoom senderRoom = sender.getRoom();
        senderRoom.userLeft(sender);
        
        ChatUser[] usersSameRoom = senderRoom.getUsers();
        ChatMessage chatMessage = new ChatMessage(MessageType.LEFT, sender.getNick(), "");
        
        for (ChatUser to : usersSameRoom)
          sendMessage(to.getSocketChannel(), chatMessage);   

        if (usersSameRoom.length == 0)
          rooms.remove(senderRoom.getName());

        sender.leftRoom();
        sender.setState(UserState.OUTSIDE);
      }
      
      // Join room
      String roomName = msgParts[1];

      // Create room if necessary
      if (!rooms.containsKey(roomName))
        rooms.put(roomName, new ChatRoom(roomName));

      ChatRoom senderRoom = rooms.get(roomName);

      ChatMessage chatMessage = new ChatMessage(MessageType.JOINED, sender.getNick(), "");
      ChatUser[] usersSameRoom = senderRoom.getUsers();
      for (ChatUser to : usersSameRoom)
          sendMessage(to.getSocketChannel(), chatMessage);  

      sender.joinRoom(senderRoom);
      senderRoom.userJoin(sender);
      sender.setState(UserState.INSIDE);
      sendOk(sender);
      
    } else if(msgParts[0].equals("/leave")) {

      if (sender.getState() == UserState.INSIDE) {
        ChatRoom senderRoom = sender.getRoom();
        senderRoom.userLeft(sender);
        
        ChatUser[] usersSameRoom = senderRoom.getUsers();
        ChatMessage chatMessage = new ChatMessage(MessageType.LEFT, sender.getNick(), "");
        
        for (ChatUser to : usersSameRoom)
          sendMessage(to.getSocketChannel(), chatMessage);   

        if (usersSameRoom.length == 0)
          rooms.remove(senderRoom.getName());

        sender.leftRoom();
        sender.setState(UserState.OUTSIDE);
        sendOk(sender);
        
      } else {
        sendError(sender, "You are not inside any room!");
      }
      
    } else if(msgParts[0].equals("/bye")) {
      
      sendBye(sender);
      closeClient(sender.getSocketChannel());

    } else if(msgParts[0].equals("/priv")) {

      // Check command syntax
      if (msgParts.length < 2) {
        sendError(sender, "Invalid usage of this command: You need to provide a destinary nick to send a PM!");
        return;
      }

      // Check if user exists
      String toNick = msgParts[1];
      if (!nicks.containsKey(toNick)) {
        sendError(sender, "The nick you provided does not exist!");
        return;
      }

      // Send message
      String finalMessage = "";
      for (int i = 2; i < msgParts.length; i ++) {
        if (i > 2)
          finalMessage += " ";
        finalMessage += msgParts[i];
      }
      ChatMessage chatMessage = new ChatMessage(MessageType.PRIVATE, sender.getNick(), finalMessage);
      sendMessage(nicks.get(toNick).getSocketChannel(), chatMessage);
      sendOk(sender);
      
    } else {
      sendError(sender, "Invalid command!");
    }
    
  }
  
  // Process message (not a command)
  private static void processMessage(String message, ChatUser sender) throws IOException  {
    if (sender.getState() == UserState.INSIDE) {
      ChatRoom senderRoom = sender.getRoom();
      ChatUser[] usersSameRoom = senderRoom.getUsers();
      for (ChatUser to : usersSameRoom){
        ChatMessage chatMessage = new ChatMessage(MessageType.MESSAGE, sender.getNick(), message);
        sendMessage(to.getSocketChannel(), chatMessage);
      }
    }
    else
      sendError(sender, "You need to be in a room to send messages!");
  }
  
  // Process input
  private static boolean processInput(SocketChannel socketChannel) throws IOException {

    // Read the message to the buffer
    inBuffer.clear();
    socketChannel.read(inBuffer);
    inBuffer.flip();

    // If no data, close the connection
    if (inBuffer.limit() == 0)
      return false;

    // Decode message
    String message = decoder.decode(inBuffer).toString().trim();
    ChatUser sender = users.get(socketChannel);

    if(message.length() > 0 && message.charAt(0) == '/')
      processCommand(message, sender);
    else
      processMessage(message, sender);
  
    return true;
  }
}
