import org.junit.Assert.*
import org.junit.Test
import org.junit.Before

class ChatServiceTest {
    private lateinit var service: ChatService

    @Before
    fun setUp() {
        service = ChatService()
    }

    // Сколько чатов не прочитано
    @Test
    fun unreadChats() {
        service.createMessage(userId = 1, text = "Сообщение для 1")
        service.createMessage(userId = 2, text = "Сообщение для 2")
        assertEquals(2, service.getUnreadChatsCount())

        service.getMessagesFromChat(userId = 1, count = 1)
        assertEquals(1, service.getUnreadChatsCount())
    }

    // Получить список чатов
    @Test
    fun getChats() {
        service.createMessage(userId = 1, text = "Сообщение для 1")
        service.createMessage(userId = 2, text = "Сообщение для 2")
        assertEquals(2, service.getChats().size)
    }

    //Получить список последних сообщений из чата
    @Test
    fun getMessagesFromChat() {
        service.createMessage(userId = 10, text = "Раз")
        service.createMessage(userId = 10, text = "Два")
        service.createMessage(userId = 10, text = "Три")

        val messages = service.getMessagesFromChat(userId = 10, count = 1)

        assertEquals(1, messages.size)
        assertEquals("Три", messages[0].text)
        assertTrue(messages[0].isRead)
    }

    //Получить список последних сообщений - «нет сообщений»
    @Test(expected = TargetIsDeletedException::class)
    fun getMessagesFromChatError() {
        service.createMessage(userId = 10, text = "Раз")
        service.createMessage(userId = 10, text = "Два")
        service.createMessage(userId = 10, text = "Три")

        val messages = service.getMessagesFromChat(userId = 11, count = 1)
    }

    // Первое сообщение создает чат
    @Test
    fun createСhat() {
        val msg = service.createMessage(userId = 999, text = "Привет")
        assertEquals("Привет", msg.text)
        assertEquals(1, msg.chatId)
    }

    // Редактировать сообщение
    @Test
    fun editMsg() {
        val msg = service.createMessage(userId = 999, text = "Привет")
        assertEquals("Привет", msg.text)
        service.editMessage(msg.id, "Пока")
        assertEquals("Пока", msg.text)
    }

    // Удалить сообщение
    @Test
    fun deleteMsg() {
        val msg1 = service.createMessage(userId = 10, text = "Раз")
        service.createMessage(userId = 10, text = "Два")
        service.createMessage(userId = 10, text = "Три")

        service.deleteMessage(msg1.id)

        val activeMessages = service.getMessagesFromChat(userId = 10, count = 3)
        assertEquals(2, activeMessages.size)
    }

    // Удалить сообщение (ERROR)
    @Test(expected = TargetIsDeletedException::class)
    fun deleteMsgError() {
        val msg1 = service.createMessage(userId = 10, text = "Раз")
        service.createMessage(userId = 10, text = "Два")
        service.createMessage(userId = 10, text = "Три")

        service.deleteMessage(999)
    }

    // Удалить чат
    @Test
    fun deleteChat() {
        val msg0 = service.createMessage(userId = 9, text = "Ноль")
        val msg1 = service.createMessage(userId = 10, text = "Раз")
        val msg2 = service.createMessage(userId = 10, text = "Два")
        val msg3 = service.createMessage(userId = 10, text = "Три")

        service.deleteChat(10)

        assertEquals(1, service.getUnreadChatsCount())
    }
}