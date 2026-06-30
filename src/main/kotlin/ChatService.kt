data class Message (
    val id: Int,
    val chatId: Int,
    var text: String,
    var isRead: Boolean = false,
    var isDeleted: Boolean = false
)

data class Chat (
    val id: Int,
    val userId: Int,
    var isDeleted: Boolean = false
)

class TargetIsDeletedException (message: String) : RuntimeException (message)

class ChatService {
    private val messages = mutableListOf<Message>()
    private val chats = mutableListOf<Chat>()

    private var nextMessageId = 1
    private var nextChatId = 1

    private fun getActiveMessagesForChat(chatId:Int): Sequence<Message> =
       messages.asSequence()
           .filter {it.chatId == chatId && !it.isDeleted}

    //1. Сколько чатов не прочитано
    fun getUnreadChatsCount(): Int =
        chats.asSequence()
            .filter {!it.isDeleted}
            .count{chat -> getActiveMessagesForChat(chat.id).any{!it.isRead}
    }

    //2. Получить список чатов
    fun getChats(): List<Chat> = chats.asSequence()
        .filter {!it.isDeleted}
        .toList()

    //3. Получить список последних сообщений из чатов
    fun getLastMessagesFromChats(): List<String> =
        chats.asSequence()
            .filter {!it.isDeleted}
            .map { chat ->
            val activeMsgs = getActiveMessagesForChat(chat.id).lastOrNull()
            if (activeMsgs == null) {
                "Chat with id ${chat.userId}: no messages"
            } else {
               "Chat with id ${chat.userId}: ${activeMsgs.text}"
            }
        }
        .toList()

   //4. Получить список сообщений из чата по ID собеседника, количество сообщений
   // (все отданные сообщения автоматически считаются прочитанными)
   fun getMessagesFromChat (userId: Int, count: Int): List<Message> {
       val chat = chats.asSequence()
           .find{it.userId == userId && !it.isDeleted}
           ?: throw TargetIsDeletedException("Chat not found")

       return getActiveMessagesForChat(chat.id)
           .toList()
           .takeLast(count)
           .onEach { it.isRead = true }
   }

   //5. Создать новое сообщение + Создать чат, когда пользователю отправляется первое сообщение
   fun createMessage(userId: Int, text:String): Message {
       val chat = chats.asSequence()
           .find {it.userId == userId}
           ?: Chat(id = nextChatId++, userId = userId).also { chats.add(it) }

       if (chat.isDeleted) {
           chat.isDeleted = false
       }
       val message = Message(
           id = nextMessageId++,
           chatId = chat.id,
           text = text
       )
       messages.add(message)
       return message
   }

    //6. Отредактировать сообщение
    fun editMessage (messageId:Int, newText: String): Message {
        val msg = messages.asSequence()
            .find {it.id == messageId && !it.isDeleted}
            ?: throw TargetIsDeletedException("Message hasn't been found")
        msg.text = newText
        return msg
    }

    //7. Удалить сообщение
    fun deleteMessage (messageId: Int): Boolean {
        val msg = messages.asSequence()
            .find {it.id == messageId && !it.isDeleted}
            ?: throw TargetIsDeletedException("Message has been deleted yet")
        msg.isDeleted = true
        return true
    }

    //8. Удалить чат
    fun deleteChat (userId: Int): Boolean {
        val chat = chats.asSequence()
            .find{it.userId == userId && !it.isDeleted}
            ?: throw TargetIsDeletedException("Chat hasn't been found")
        chat.isDeleted = true

        messages.asSequence()
            .filter{it.chatId == chat.id}
            .forEach {it.isDeleted = true}
        return true
    }
}