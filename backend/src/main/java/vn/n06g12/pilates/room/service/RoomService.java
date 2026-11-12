package vn.n06g12.pilates.room.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.n06g12.pilates.common.exception.BusinessException;
import vn.n06g12.pilates.common.exception.ResourceNotFoundException;
import vn.n06g12.pilates.room.dto.CreateRoomRequest;
import vn.n06g12.pilates.room.dto.RoomResponse;
import vn.n06g12.pilates.room.dto.UpdateRoomRequest;
import vn.n06g12.pilates.room.entity.Room;
import vn.n06g12.pilates.room.entity.RoomStatus;
import vn.n06g12.pilates.room.repository.RoomRepository;
import vn.n06g12.pilates.session.repository.SessionRepository;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final SessionRepository sessionRepository;

    @Transactional(readOnly = true)
    public List<RoomResponse> getActiveRooms() {
        return roomRepository.findByStatus(RoomStatus.ACTIVE).stream()
                .map(RoomResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<RoomResponse> searchRooms(RoomStatus status, String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return roomRepository.searchRooms(status, pattern, pageable)
                    .map(RoomResponse::fromEntity);
        }
        if (status != null) {
            return roomRepository.findByStatus(status, pageable)
                    .map(RoomResponse::fromEntity);
        }
        return roomRepository.findAll(pageable)
                .map(RoomResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng tập với ID: " + id));
        return RoomResponse.fromEntity(room);
    }

    @Transactional
    public RoomResponse createRoom(CreateRoomRequest request) {
        if (roomRepository.existsByName(request.getName().trim())) {
            throw new BusinessException("Tên phòng tập đã tồn tại: " + request.getName());
        }

        Room room = Room.builder()
                .name(request.getName().trim())
                .capacity(request.getCapacity())
                .description(request.getDescription())
                .status(RoomStatus.ACTIVE)
                .build();

        Room saved = roomRepository.save(room);
        log.info("Admin đã tạo phòng tập mới: {} (ID: {})", saved.getName(), saved.getId());
        return RoomResponse.fromEntity(saved);
    }

    @Transactional
    public RoomResponse updateRoom(Long id, UpdateRoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng tập với ID: " + id));

        roomRepository.findByName(request.getName().trim()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("Tên phòng tập đã được sử dụng bởi phòng khác: " + request.getName());
            }
        });

        if (request.getCapacity() < room.getCapacity()) {
            long countExceeding = sessionRepository.countFutureSessionsExceedingCapacity(id, request.getCapacity(), Instant.now());
            if (countExceeding > 0) {
                throw new BusinessException("Không thể giảm sức chứa phòng xuống " + request.getCapacity() +
                        " vì đang có " + countExceeding + " buổi tập trong tương lai có sức chứa hoặc số người đặt vượt quá mức này");
            }
        }

        room.setName(request.getName().trim());
        room.setCapacity(request.getCapacity());
        room.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }

        Room saved = roomRepository.save(room);
        log.info("Admin đã cập nhật phòng tập: {} (ID: {})", saved.getName(), saved.getId());
        return RoomResponse.fromEntity(saved);
    }

    @Transactional
    public RoomResponse updateRoomStatus(Long id, RoomStatus status) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy phòng tập với ID: " + id));

        room.setStatus(status);
        Room saved = roomRepository.save(room);
        log.info("Admin đã đổi trạng thái phòng tập {} thành {}", saved.getName(), status);
        return RoomResponse.fromEntity(saved);
    }
}
