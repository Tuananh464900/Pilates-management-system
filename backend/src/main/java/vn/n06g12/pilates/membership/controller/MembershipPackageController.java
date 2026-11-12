package vn.n06g12.pilates.membership.controller;

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
import vn.n06g12.pilates.membership.dto.PackageRequest;
import vn.n06g12.pilates.membership.dto.PackageResponse;
import vn.n06g12.pilates.membership.service.MembershipPackageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Gói tập Pilates (UC3.1)", description = "Quản lý danh mục gói tập và xem thông tin gói")
public class MembershipPackageController {

    private final MembershipPackageService packageService;

    @GetMapping("/api/v1/packages")
    @Operation(summary = "UC3.1 - Danh sách gói tập đang mở bán", description = "Lấy danh sách các gói tập Pilates ACTIVE công khai")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getAllActivePackages() {
        List<PackageResponse> list = packageService.getAllActivePackages();
        return ResponseEntity.ok(ApiResponse.ok(list, "Lấy danh sách gói tập thành công"));
    }

    @GetMapping("/api/v1/packages/{id}")
    @Operation(summary = "UC3.1 - Chi tiết gói tập", description = "Xem chi tiết một gói tập Pilates theo ID")
    public ResponseEntity<ApiResponse<PackageResponse>> getPackageById(@PathVariable Long id) {
        PackageResponse response = packageService.getPackageById(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Lấy thông tin gói tập thành công"));
    }

    @GetMapping("/api/v1/admin/packages")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC3.1 - Admin danh sách gói tập", description = "Admin tra cứu danh sách toàn bộ gói tập có phân trang")
    public ResponseEntity<ApiResponse<PageResponse<PackageResponse>>> adminGetAllPackages(
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<PackageResponse> page = PageResponse.from(packageService.adminGetAllPackages(pageable));
        return ResponseEntity.ok(ApiResponse.ok(page, "Lấy danh sách gói tập thành công"));
    }

    @PostMapping("/api/v1/admin/packages")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC3.1 - Admin tạo gói tập mới", description = "Admin thêm mới một gói tập Pilates vào hệ thống")
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(@Valid @RequestBody PackageRequest request) {
        PackageResponse response = packageService.createPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Tạo gói tập mới thành công"));
    }

    @PutMapping("/api/v1/admin/packages/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC3.1 - Admin cập nhật gói tập", description = "Admin chỉnh sửa thông tin gói tập Pilates")
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackage(@PathVariable Long id,
                                                                     @Valid @RequestBody PackageRequest request) {
        PackageResponse response = packageService.updatePackage(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật gói tập thành công"));
    }

    @PatchMapping("/api/v1/admin/packages/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "UC3.1 - Admin bật/tắt trạng thái gói tập", description = "Chuyển đổi trạng thái giữa ACTIVE và INACTIVE")
    public ResponseEntity<ApiResponse<PackageResponse>> togglePackageStatus(@PathVariable Long id) {
        PackageResponse response = packageService.togglePackageStatus(id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Cập nhật trạng thái gói tập thành công"));
    }
}
