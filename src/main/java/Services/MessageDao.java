package Services;

import Models.Message;
import java.sql.SQLException;
import java.util.List;

public interface MessageDao {
    long insert(Message message) throws SQLException;

    List<Message> getConversation(long user1Id, long user2Id) throws SQLException;

    void markAsRead(long senderId, long receiverId) throws SQLException;
}
