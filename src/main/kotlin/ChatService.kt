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

    private fun getActiveMessagesForChat(chatId:Int): List<Message> =
       messages.filter {it.chatId == chatId && !it.isDeleted}

    //1. Сколько чатов не прочитано
    fun getUnreadChatsCount(): Int =
        chats.filter {!it.isDeleted}.count{chat -> getActiveMessagesForChat(chat.id).any{!it.isRead}
    }

    //2. Получить список чатов
    fun getChats(): List<Chat> = chats.filter {!it.isDeleted}

    //3. Получить список последних сообщений из чатов
    fun getLastMessagesFromChats(): List<String> {
        val result = mutableListOf<String>()
        chats.filter {!it.isDeleted}.forEach { chat ->
            val activeMsgs = getActiveMessagesForChat(chat.id)

            if (activeMsgs.isEmpty()) {
                result.add("Chat with id ${chat.userId}: no messages")
            } else {
                result.add("Chat with id ${chat.userId}: ${activeMsgs.last().text}")
            }
        }
        return result
    }

   //4. Получить список сообщений из чата по ID собеседника, количество сообщений
   // (все отданные сообщения автоматически считаются прочитанными)
   fun getMessagesFromChat (userId: Int, count: Int): List<Message> {
       val chat = chats.find{it.userId == userId && !it.isDeleted}
           ?: throw TargetIsDeletedException("Chat not found")
       val activeMsgs = getActiveMessagesForChat(chat.id)
       val neededMsgs = activeMsgs.takeLast(count)
       neededMsgs.forEach { it.isRead=true }
       return neededMsgs
   }

   //5. Создать новое сообщение + Создать чат, когда пользователю отправляется первое сообщение
   fun createMessage(userId: Int, text:String): Message {
       var chat = chats.find {it.userId == userId}

       if (chat == null) {
           chat = Chat(id = nextChatId++, userId = userId)
           chats.add(chat)
       } else if (chat.isDeleted) {
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
    fun editMessage (messageId:Int, newText: String) {
        val msg = messages.find {it.id == messageId && !it.isDeleted}
            ?: throw TargetIsDeletedException("Message hasn't been found")
        msg.text = newText
    }

    //7. Удалить сообщение
    fun deleteMessage (messageId: Int) {
        val msg = messages.find {it.id == messageId && !it.isDeleted}
            ?: throw TargetIsDeletedException("Message has been deleted yet")
        msg.isDeleted = true
    }

    //8. Удалить чат
    fun deleteChat (userId: Int) {
        val chat = chats.find{it.userId == userId && !it.isDeleted}
            ?: throw TargetIsDeletedException("Chat hasn't been found")
        chat.isDeleted = true
        messages.filter{it.chatId == chat.id}.forEach { it.isDeleted = true }
    }
}