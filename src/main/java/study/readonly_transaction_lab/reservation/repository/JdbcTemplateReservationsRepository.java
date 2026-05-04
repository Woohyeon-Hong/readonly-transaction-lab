package study.readonly_transaction_lab.reservation.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import study.readonly_transaction_lab.reservation.Reservation;
import study.readonly_transaction_lab.reservation.ReservationEntity;
import study.readonly_transaction_lab.reservationTime.TimeInfo;

@Repository("jdbcTemplateReservationsRepo")
public class JdbcTemplateReservationsRepository implements ReservationsRepository {

    private static final String RESERVATION_ID_COLUMN = "reservation_id";
    private static final String RESERVATION_NAME_COLUMN = "reservation_name";
    private static final String RESERVATION_DATE_COLUMN = "reservation_date";
    private static final String TIME_ID_COLUMN = "time_id";
    private static final String TIME_VALUE_COLUMN = "time_value";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcTemplateReservationsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public ReservationEntity saveReservation(ReservationEntity entity) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection ->
                        createPreparedStatement(entity, connection),
                keyHolder
        );

        long id = keyHolder.getKey().longValue();
        return entity.updateId(id);
    }

    private PreparedStatement createPreparedStatement(
            ReservationEntity reservationEntity,
            Connection connection
    ) throws SQLException {
        String sql = "INSERT INTO reservation(name, date, time_id) VALUES (?, ?, ?)";

        PreparedStatement preparedStatement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
        );

        preparedStatement.setString(1, reservationEntity.name());
        preparedStatement.setDate(2, Date.valueOf(reservationEntity.date()));
        preparedStatement.setLong(3, reservationEntity.timeId());

        return preparedStatement;
    }

    @Override
    public Optional<Reservation> findReservationWithTimeById(Long id) {
        String sql = "SELECT\n"
                + "    r.id as " + RESERVATION_ID_COLUMN + ",\n"
                + "    r.name as " + RESERVATION_NAME_COLUMN + ",\n"
                + "    r.date as " + RESERVATION_DATE_COLUMN + ",\n"
                + "    t.id as " + TIME_ID_COLUMN + ",\n"
                + "    t.start_at as " + TIME_VALUE_COLUMN + "\n"
                + "FROM reservation as r\n"
                + "INNER JOIN reservation_time as t\n"
                + "  ON r.time_id = t.id\n"
                + " WHERE r.id = ?\n";

        try {
            Reservation reservation = jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> new Reservation(
                            rs.getLong(RESERVATION_ID_COLUMN),
                            rs.getString(RESERVATION_NAME_COLUMN),
                            rs.getDate(RESERVATION_DATE_COLUMN).toLocalDate(),
                            new TimeInfo(
                                    rs.getLong(TIME_ID_COLUMN),
                                    rs.getTime(TIME_VALUE_COLUMN).toLocalTime()
                            )
                    ),
                    id
            );

            return Optional.of(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Reservation> findAllReservationsWithTime() {

        String sql = "SELECT\n"
                + "    r.id as " + RESERVATION_ID_COLUMN + ",\n"
                + "    r.name as " + RESERVATION_NAME_COLUMN + ",\n"
                + "    r.date as " + RESERVATION_DATE_COLUMN + ",\n"
                + "    t.id as " + TIME_ID_COLUMN + ",\n"
                + "    t.start_at as " + TIME_VALUE_COLUMN + "\n"
                + "FROM reservation as r\n"
                + "INNER JOIN reservation_time as t\n"
                + "  ON r.time_id = t.id\n";


        return jdbcTemplate.query(sql, (rs, rowNum) ->
            new Reservation(
                    rs.getLong(RESERVATION_ID_COLUMN),
                    rs.getString(RESERVATION_NAME_COLUMN),
                    rs.getDate(RESERVATION_DATE_COLUMN).toLocalDate(),
                    new TimeInfo(
                            rs.getLong(TIME_ID_COLUMN),
                            rs.getTime(TIME_VALUE_COLUMN).toLocalTime()
                    )
            )
        );
    }
}
