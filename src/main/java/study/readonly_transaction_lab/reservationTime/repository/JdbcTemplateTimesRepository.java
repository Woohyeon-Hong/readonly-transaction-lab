package study.readonly_transaction_lab.reservationTime.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import study.readonly_transaction_lab.reservationTime.TimeEntity;

@Repository("jdbcTemplateTimesRepo")
public class JdbcTemplateTimesRepository implements TimesRepository {

    private static final String ID_COLUMN = "id";
    private static final String START_AT_COLUMN = "start_at";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcTemplateTimesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public TimeEntity saveTime(TimeEntity entity) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection ->
                        createPreparedStatement(entity, connection),
                keyHolder
        );

        long id = keyHolder.getKey().longValue();
        return entity.updateId(id);
    }

    private PreparedStatement createPreparedStatement(
            TimeEntity entity,
            Connection connection
    ) throws SQLException {
        String sql = "INSERT INTO reservation_time(start_at) VALUES (?)";

        PreparedStatement preparedStatement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        );

        preparedStatement.setTime(1, Time.valueOf(entity.startAt()));
        return preparedStatement;
    }

    @Override
    public Optional<TimeEntity> findTimeEntityById(Long id) {
        String sql = "select * FROM reservation_time WHERE id = ?";

        try {
            TimeEntity timeEntity = jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> new TimeEntity(
                            rs.getLong("id"),
                            rs.getTime("start_at").toLocalTime()
                    ),
                    id
            );

            return Optional.of(timeEntity);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<TimeEntity> findAllTimes() {
        String sql = "SELECT * from reservation_time";

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) ->
                        new TimeEntity(
                                rs.getLong(ID_COLUMN),
                                rs.getTime(START_AT_COLUMN).toLocalTime()
                        )
        );
    }
}
