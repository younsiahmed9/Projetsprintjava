
package Controllers;

import Models.Role;
import Services.JdbcUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    @Autowired
    private JdbcUserDao userDao;

    @Autowired
    private Connection connection;

    @GetMapping("/users")
    public Map<String, Long> getUserStats() throws SQLException {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", userDao.findAll().stream().count());
        stats.put("admins", userDao.countByRole(Role.ADMIN));
        stats.put("clients", userDao.countByRole(Role.CLIENT));
        return stats;
    }

    @GetMapping("/roles")
    public Map<String, Double> getRoleDistribution() throws SQLException {
        long total = userDao.findAll().stream().count();
        Map<String, Double> dist = new HashMap<>();
        if (total == 0) {
            dist.put("ADMIN", 0.0);
            dist.put("CLIENT", 0.0);
        } else {
            dist.put("ADMIN", userDao.countByRole(Role.ADMIN) * 100.0 / total);
            dist.put("CLIENT", userDao.countByRole(Role.CLIENT) * 100.0 / total);
        }
        return dist;
    }

    @GetMapping("/registrations")
    public List<JdbcUserDao.DailyCount> getRegistrations() throws SQLException {
        // 7 derniers jours
        return userDao.countCreatedUsersLastDays(7);
    }

    @GetMapping("/active")
    public Map<String, Long> getActiveStats() throws SQLException {
        long active = userDao.findAll().stream().filter(u -> u.isActive()).count();
        long inactive = userDao.findAll().stream().filter(u -> !u.isActive()).count();
        Map<String, Long> stats = new HashMap<>();
        stats.put("active", active);
        stats.put("inactive", inactive);
        return stats;
    }
}
