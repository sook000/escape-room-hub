package cs.escaperoomhub.point.service.eventHandler;

import cs.escaperoomhub.common.event.Event;
import cs.escaperoomhub.common.event.EventType;
import cs.escaperoomhub.common.event.payload.ArticleCreatedEventPayload;
import cs.escaperoomhub.common.exceptionstarter.CommonErrors;
import cs.escaperoomhub.point.entity.Point;
import cs.escaperoomhub.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload>{
    private final PointRepository pointRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();

        if (payload.getBoardId() == 1) { // 특정 게시판에 글을 쓸 때(리뷰 게시판)
            Point point = pointRepository.findByUserId(payload.getWriterId())
                    .orElseThrow(() -> CommonErrors.notFound("포인트가 존재하지 않습니다."));

            point.cancel(100L); // 100원 적립
            pointRepository.save(point);
        }

    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleCreatedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}
