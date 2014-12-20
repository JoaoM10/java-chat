JAVA = javac
JAVAFLAGS= -g

RM = /bin/rm -f

chat: 
	${JAVA} ${JAVAFLAGS} ChatMessage.java
	${JAVA} ${JAVAFLAGS} ChatClient.java

clean:
	${RM} -f *.class
