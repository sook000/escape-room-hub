package cs.escaperoomhub.common.event;

import cs.escaperoomhub.common.event.payload.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    ARTICLE_CREATED(ArticleCreatedEventPayload.class, Topic.ADVANCED_BOARD_ARTICLE),
    ARTICLE_UPDATED(ArticleUpdatedEventPayload.class, Topic.ADVANCED_BOARD_ARTICLE),
    ARTICLE_DELETED(ArticleDeletedEventPayload.class, Topic.ADVANCED_BOARD_ARTICLE),
    COMMENT_CREATED(CommentCreatedEventPayload.class, Topic.ADVANCED_BOARD_COMMENT),
    COMMENT_DELETED(CommentDeletedEventPayload.class, Topic.ADVANCED_BOARD_COMMENT),
    ARTICLE_LIKED(ArticleLikedEventPayload.class, Topic.ADVANCED_BOARD_LIKE),
    ARTICLE_UNLIKED(ArticleUnlikedEventPayload.class, Topic.ADVANCED_BOARD_LIKE),
    ARTICLE_VIEWED(ArticleViewedEventPayload.class, Topic.ADVANCED_BOARD_VIEW),
    COMPENSATION_REQUIRED(CompensationRequiredEventPayload.class, Topic.RESERVATION_COMPENSATION);

    private final Class<? extends EventPayload> payloadClass;
    private final String topic;

    public static EventType from(String type) {
        try {
            return valueOf(type);
        } catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    public static class Topic {
        public static final String ADVANCED_BOARD_ARTICLE = "advanced-board-article";
        public static final String ADVANCED_BOARD_COMMENT = "advanced-board-comment";
        public static final String ADVANCED_BOARD_LIKE = "advanced-board-like";
        public static final String ADVANCED_BOARD_VIEW = "advanced-board-view";

        public static final String RESERVATION_COMPENSATION = "reservation-compensation";
    }
}
