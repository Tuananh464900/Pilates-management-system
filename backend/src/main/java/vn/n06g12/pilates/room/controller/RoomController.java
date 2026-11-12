package vn.n06g12.pilates.room.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.n06g12.pilates.common.response.ApiResponse;
import vn.n06g12.pilates.common.response.PageResponse;
import vn.n06g12.pilates.room.dto.CreateRoomRequest;
import vn.n06g12.pilates.room.dto.RoomResponse;
import vn.n06g12.pilates.room.dto.UpdateRoomRequest;
import vn.n06g12.pilates.room.entity.RoomStatus;
import vn.n06g12.pilates.room.service.RoomService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Phòng tập luyện (UC2.2)", description = "Quản lý phòng tập luyện phục vụ xếp lịch buổi tập")
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/api/v1/rooms")
    @Operation(summary = "UC2.2 - Danh sách phòng tập đang hoạt động", description = "Lấy danh sách các phòng tập có trạng thái ACTIVE")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getActiveRooms() {
        List<RoomResponse> rooms = roomService.getActiveRooms();
        return ResponseEntity.ok(ApiResponse.ok(rooms, "Lấy danh sách phòng tập thành công"));
    }

    @GetMapping("/api/v1/rooms/{id}")
    @Operation(summary = "UC2.2 - Chi tiết phòng tập", description = "Xem chi tiết một phòng tập")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        RoomResponse response = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy chi tiết phòng tập thành công"));
    }

    @GetMapping("/api/v1/admin/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.2 - Admin tra cứu danh sách phòng tập", description = "Admin xem toàn bộ danh sách phòng kèm tìm kiếm và phân trang")
    public ResponseEntity<ApiResponse<PageResponse<RoomResponse>>> adminSearchRooms(
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<RoomResponse> page = PageResponse.from(roomService.searchRooms(status, search, pageable));
        return ResponseEntity.ok(ApiResponse.ok(page, "Lấy danh sách phòng tập thành công"));
    }

    @PostMapping("/api/v1/admin/rooms")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.2 - Tạo phòng tập mới", description = "Admin tạo phòng tập mới với tên, sức chứa và mô tả")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Tạo phòng tập mới thành công"));
    }

    @PutMapping("/api/v1/admin/rooms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.2 - Cập nhật thông tin phòng tập", description = "Admin cập nhật thông tin phòng tập")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateRoomRequest request) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật phòng tập thành công"));
    }

    @PatchMapping("/api/v1/admin/rooms/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.2 - Ngừng / Kích hoạt lại phòng tập", description = "Thay đổi trạng thái ACTIVE / INACTIVE của phòng")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoomStatus(@PathVariable Long id,
                                                                     @RequestParam RoomStatus status) {
        RoomResponse response = roomService.updateRoomStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật trạng thái phòng tập thành công"));
    }
}
