JAVA = javac
JAVAFLAGS= -g

RM = /bin/rm -f

chat: 
	${JAVA} ${JAVAFLAGS} ChatMessage.java
	${JAVA} ${JAVAFLAGS} ChatClient.java
	${JAVA} ${JAVAFLAGS} ChatUser.java
	${JAVA} ${JAVAFLAGS} ChatRoom.java
	${JAVA} ${JAVAFLAGS} ChatServer.java

clean:
	${RM} -f *.class
