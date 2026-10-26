package com.minhhai.newsmanagement.dto;

import com.minhhai.newsmanagement.enums.ArticleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class WorkflowRequests {
    private WorkflowRequests() {}

    public record RequestChanges(@NotBlank(message = "Feedback là bắt buộc") String feedback) {}

    /** scheduledPublishDate rỗng = đăng ngay, thời điểm tương lai = hẹn giờ. */
    public record Approve(Instant scheduledPublishDate) {}

    public record Archive(String reason) {}

    public record Override(
            @NotNull(message = "Trạng thái đích là bắt buộc") ArticleStatus targetStatus,
            @NotBlank(message = "Lý do override là bắt buộc") String reason,
            Instant scheduledPublishDate) {}
}
