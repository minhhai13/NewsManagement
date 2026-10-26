export const STATUS_LABEL = {
   DRAFT: "Bản nháp",
   PENDING_REVIEW: "Chờ duyệt",
   CHANGES_REQUESTED: "Cần sửa",
   APPROVED_SCHEDULED: "Đã duyệt - hẹn giờ",
   PUBLISHED: "Đã đăng",
   ARCHIVED: "Đã gỡ",
};

export const STATUS_VARIANT = {
   DRAFT: "secondary",
   PENDING_REVIEW: "warning",
   CHANGES_REQUESTED: "danger",
   APPROVED_SCHEDULED: "info",
   PUBLISHED: "success",
   ARCHIVED: "dark",
};

export const ROLE_LABEL = {
   ADMIN: "Admin",
   EDITOR: "Editor",
   CONTRIBUTOR: "Contributor",
};

export const ACTION_LABEL = {
   CREATE_DRAFT: "Tạo bản nháp",
   SUBMIT: "Gửi duyệt",
   RESUBMIT: "Gửi lại",
   REQUEST_CHANGES: "Yêu cầu sửa",
   APPROVE_NOW: "Duyệt - đăng ngay",
   SCHEDULE: "Duyệt - hẹn giờ",
   AUTO_PUBLISH: "Tự động đăng",
   ARCHIVE: "Gỡ bài",
   OVERRIDE: "Admin override",
};

// Thời gian lưu dạng Instant (UTC), hiển thị theo giờ Việt Nam.
export function formatDate(value) {
   if (!value) return "-";
   return new Date(value).toLocaleString("vi-VN", { timeZone: "Asia/Ho_Chi_Minh" });
}

// Giá trị của <input type="datetime-local"> (giờ máy người dùng) -> Instant ISO có timezone.
export function localInputToIso(value) {
   return value ? new Date(value).toISOString() : null;
}
