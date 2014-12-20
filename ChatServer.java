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

              // If the connection is dead, remove it from the selector and close it
              if (!ok) {
                key.cancel();

                Socket socket = null;
                try {
                  socket = socketChannel.socket();
                  System.out.println("Closing connection to " + socket);
                  socket.close();
                } catch(IOException ex) {
                  System.err.println("Error closing socket " + socket + ": " + ex);
                }
              }

            } catch(IOException ex) {

              // On exception, remove this channel from the selector
              key.cancel();

              try {
                socketChannel.close();
              } catch(IOException ex2) {
                System.err.println("Error closing socket channel " + ex2);
              }

              System.out.println("Closed " + socketChannel);
            }
          }
        }

        keys.clear();
      }
    } catch(IOException ex) {
      System.err.println(ex);
    }
  }

  // Process command
  private static void processCommand(String message, ChatUser sender) {
    String[] msgParts = message.split(" ");

    if(msgParts[0].equals("/nick")) {
      // check name
    } else if(msgParts[0].equals("/join")) {
      // check room
    } else if(msgParts[0].equals("/leave")) {

    } else if(msgParts[0].equals("/bye")) {

    } else {

    }
    
  }
  
  // Process message (not a command)
  private static void processMessage(String message, ChatUser sender) {

    if (sender.getState() == UserState.INSIDE) {

      ChatUser[] usersSameRoom = sender.getRoom().getUsers();
      for (ChatUser user : usersSameRoom)
        ;//sendMessage(user, sender.getNick(), message);
      
    }
    else
      ;//sendError(sender, "You need to be in a room to send messages!");
    
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

    if(message.charAt(0) == '/')
      processCommand(message, sender);
    else
      processMessage(message, sender);
  
    return true;
  }
}
