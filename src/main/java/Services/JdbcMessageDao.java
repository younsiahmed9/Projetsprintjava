package Services;

import Models.Message;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JdbcMessageDao implements MessageDao {

    private final Connection connection;

    public JdbcMessageDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public long insert(Message message) throws SQLException {
        String sql = "INSERT INTO messages(sender_id, receiver_id, content, timestamp, is_read) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, message.getSenderId());
            ps.setLong(2, message.getReceiverId());
            ps.setString(3, message.getContent());
            ps.setTimestamp(4,
                    Timestamp.valueOf(message.getTimestamp() != null ? message.getTimestamp() : LocalDateTime.now()));
            ps.setBoolean(5, message.isRead());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    message.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Insertion message: aucun id généré");
    }

    @Override
    public List<Message> getConversation(long user1Id, long user2Id) throws SQLException {
        String sql = "SELECT id, sender_id, receiver_id, content, timestamp, is_read FROM messages " +
                "WHERE (sender_id = ? AND receiver_id = ?) " +
                "   OR (sender_id = ? AND receiver_id = ?) " +
                "ORDER BY timestamp ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, user1Id);
            ps.setLong(2, user2Id);
            ps.setLong(3, user2Id);
            ps.setLong(4, user1Id);

            List<Message> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRow(rs));
                }
            }
            return out;
        }
    }

    @Override
    public void markAsRead(long senderId, long receiverId) throws SQLException {
        String sql = "UPDATE messages SET is_read = TRUE WHERE sender_id = ? AND receiver_id = ? AND is_read = FALSE";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, senderId);
            ps.setLong(2, receiverId);
            ps.executeUpdate();
        }
    }

    /**
     * Retourne la liste des IDs des utilisateurs ayant déjà discuté avec `userId`.
     * (Contact actif = soit il a envoyé un message, soit il en a reçu un)
     */
    public List<Long> getActiveContactIds(long userId) throws SQLException {
        String sql = "SELECT DISTINCT contact_id FROM (" +
                "  SELECT receiver_id AS contact_id FROM messages WHERE sender_id = ? " +
                "  UNION " +
                "  SELECT sender_id AS contact_id FROM messages WHERE receiver_id = ? " +
                ") AS contacts";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, userId);

            List<Long> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getLong("contact_id"));
                }
            }
            return out;
        }
    }

    private Message mapRow(ResultSet rs) throws SQLException {
        Message msg = new Message();
        msg.setId(rs.getLong("id"));
        msg.setSenderId(rs.getLong("sender_id"));
        msg.setReceiverId(rs.getLong("receiver_id"));
        msg.setContent(rs.getString("content"));
        msg.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
        msg.setRead(rs.getBoolean("is_read"));
        return msg;
    }
}
