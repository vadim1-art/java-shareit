package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.enumClasses.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Запросы для того, кто бронирует (Booker)
    // ALL
    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    // CURRENT (Началось в прошлом/сейчас, а закончится в будущем)
    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end);

    // PAST (Уже закончилось)
    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime end);

    // FUTURE (Еще не началось)
    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start);

    // WAITING или REJECTED
    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(
            Long bookerId, BookingStatus status);


    // Запросы для Владельца вещей (Owner)
    // ALL для всех вещей владельца
    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    // CURRENT для владельца
    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end);

    // PAST для владельца
    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime end);

    // FUTURE для владельца
    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start);

    // WAITING или REJECTED для владельца
    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(
            Long ownerId, BookingStatus status);


    // Вспомогательные методы для ItemService
    // Найти последнее бронирование вещи (до текущего момента)
    Booking findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
            Long itemId, LocalDateTime now, BookingStatus status);

    // Найти следующее бронирование вещи (после текущего момента)
    Booking findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
            Long itemId, LocalDateTime now, BookingStatus status);

    // Проверка для комментариев: брал ли пользователь эту вещь в аренду? (Статус APPROVED, дата конца уже прошла)
    boolean existsByBookerIdAndItemIdAndEndBeforeAndStatus(
            Long bookerId, Long itemId, LocalDateTime end, BookingStatus status);

    List<Booking> findByItemIdAndStatus(Long itemId, BookingStatus status);

    List<Booking> findByItemIdInAndStatus(List<Long> itemIds, BookingStatus status);

    // Проверка пересечения бронирований
    @Query("select count(b) > 0 from Booking b " +
            "where b.item.id = :itemId " +
            "and b.status != 'REJECTED' " +
            "and b.start < :end " +
            "and b.end > :start")
    boolean hasOverlappingBooking(@Param("itemId") Long itemId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end);
}