package vn.n06g12.pilates.pilatesclass.controller;

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
import vn.n06g12.pilates.pilatesclass.dto.ClassResponse;
import vn.n06g12.pilates.pilatesclass.dto.CreateClassRequest;
import vn.n06g12.pilates.pilatesclass.dto.UpdateClassRequest;
import vn.n06g12.pilates.pilatesclass.entity.ClassStatus;
import vn.n06g12.pilates.pilatesclass.service.PilatesClassService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Lớp Pilates (UC2.1)", description = "Quản lý danh mục và cấu hình lớp Pilates mẫu")
public class PilatesClassController {

    private final PilatesClassService classService;

    @GetMapping("/api/v1/classes")
    @Operation(summary = "UC2.1 - Danh sách lớp Pilates đang hoạt động", description = "Lấy danh sách các lớp Pilates có trạng thái ACTIVE")
    public ResponseEntity<ApiResponse<List<ClassResponse>>> getActiveClasses() {
        List<ClassResponse> classes = classService.getActiveClasses();
        return ResponseEntity.ok(ApiResponse.ok(classes, "Lấy danh sách lớp Pilates thành công"));
    }

    @GetMapping("/api/v1/classes/{id}")
    @Operation(summary = "UC2.1 - Chi tiết lớp Pilates", description = "Xem chi tiết một lớp Pilates")
    public ResponseEntity<ApiResponse<ClassResponse>> getClassById(@PathVariable Long id) {
        ClassResponse response = classService.getClassById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy chi tiết lớp Pilates thành công"));
    }

    @GetMapping("/api/v1/admin/classes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.1 - Admin tra cứu danh sách lớp Pilates", description = "Admin xem toàn bộ danh sách lớp kèm tìm kiếm và phân trang")
    public ResponseEntity<ApiResponse<PageResponse<ClassResponse>>> adminSearchClasses(
            @RequestParam(required = false) ClassStatus status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<ClassResponse> page = PageResponse.from(classService.searchClasses(status, search, pageable));
        return ResponseEntity.ok(ApiResponse.ok(page, "Lấy danh sách lớp Pilates thành công"));
    }

    @PostMapping("/api/v1/admin/classes")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.1 - Tạo lớp Pilates mới", description = "Admin tạo lớp Pilates mới với tên, mô tả và sức chứa mặc định")
    public ResponseEntity<ApiResponse<ClassResponse>> createClass(@Valid @RequestBody CreateClassRequest request) {
        ClassResponse response = classService.createClass(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Tạo lớp Pilates mới thành công"));
    }

    @PutMapping("/api/v1/admin/classes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.1 - Cập nhật lớp Pilates", description = "Admin cập nhật thông tin lớp Pilates")
    public ResponseEntity<ApiResponse<ClassResponse>> updateClass(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdateClassRequest request) {
        ClassResponse response = classService.updateClass(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật lớp Pilates thành công"));
    }

    @PatchMapping("/api/v1/admin/classes/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC2.1 - Ngừng / Kích hoạt lại lớp Pilates", description = "Thay đổi trạng thái ACTIVE / INACTIVE của lớp")
    public ResponseEntity<ApiResponse<ClassResponse>> updateClassStatus(@PathVariable Long id,
                                                                       @RequestParam ClassStatus status) {
        ClassResponse response = classService.updateClassStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật trạng thái lớp Pilates thành công"));
    }
}
