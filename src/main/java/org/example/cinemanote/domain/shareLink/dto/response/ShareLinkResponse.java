package org.example.cinemanote.domain.shareLink.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.example.cinemanote.domain.shareLink.entity.ShareLink;

import java.time.LocalDateTime;

@Getter
public class ShareLinkResponse {

    private final String shareToken;
    private final String shareUrl;
    private final LocalDateTime expiresAt;
    @JsonProperty("isActive")
    private final boolean isActive;

    private ShareLinkResponse(ShareLink shareLink) {
        this.shareToken = shareLink.getShareToken();
        this.shareUrl = "/api/share/" + shareLink.getShareToken();
        this.expiresAt = shareLink.getExpiresAt();
        this.isActive = shareLink.isActive();
    }

    public static ShareLinkResponse from(ShareLink shareLink) {
        return new ShareLinkResponse(shareLink);
    }
}
