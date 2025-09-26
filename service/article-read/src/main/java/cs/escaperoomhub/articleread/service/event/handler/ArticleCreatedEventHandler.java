package cs.escaperoomhub.articleread.service.event.handler;

import cs.escaperoomhub.articleread.repository.ArticleQueryModel;
import cs.escaperoomhub.articleread.repository.ArticleQueryModelRepository;
import cs.escaperoomhub.common.event.Event;
import cs.escaperoomhub.common.event.EventType;
import cs.escaperoomhub.common.event.payload.ArticleCreatedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        ArticleCreatedEventPayload payload = event.getPayload();
        articleQueryModelRepository.create(
                ArticleQueryModel.create(payload),
                Duration.ofDays(1)
        );
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType();
    }
}
