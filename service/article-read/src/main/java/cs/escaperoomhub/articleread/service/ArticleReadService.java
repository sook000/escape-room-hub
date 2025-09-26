package cs.escaperoomhub.articleread.service;

import cs.escaperoomhub.articleread.client.ArticleClient;
import cs.escaperoomhub.articleread.client.CommentClient;
import cs.escaperoomhub.articleread.client.LikeClient;
import cs.escaperoomhub.articleread.client.ViewClient;
import cs.escaperoomhub.articleread.repository.ArticleQueryModel;
import cs.escaperoomhub.articleread.repository.ArticleQueryModelRepository;
import cs.escaperoomhub.articleread.service.event.handler.EventHandler;
import cs.escaperoomhub.articleread.service.response.ArticleReadResponse;
import cs.escaperoomhub.common.event.Event;
import cs.escaperoomhub.common.event.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleReadService {
    private final ArticleClient articleClient;
    private final CommentClient commentClient;
    private final LikeClient likeClient;
    private final ViewClient viewClient;
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final List<EventHandler> eventHandlers;


    public void handleEvent(Event<EventPayload> event) {
        for (EventHandler eventHandler : eventHandlers) {
            if (eventHandler.supports(event)) {
                eventHandler.handle(event);
            }
        }
    }

    public ArticleReadResponse read(Long articleId) {
        ArticleQueryModel articleQueryModel = articleQueryModelRepository.read(articleId)
                .or(() -> fetch(articleId)) // 데이터 없으면 command 서비스에서 데이터를 가져옴
                .orElseThrow();

        return ArticleReadResponse.from(
                articleQueryModel,
                viewClient.count(articleId)
        );
    }

    private Optional<ArticleQueryModel> fetch(Long articleId) {
        Optional<ArticleQueryModel> articleQueryModelOptional = articleClient.read(articleId)
                .map(article -> ArticleQueryModel.create(
                        article,
                        commentClient.count(articleId),
                        likeClient.count(articleId)
                ));
        articleQueryModelOptional // 데이터 있다면 하루동안 저장
                .ifPresent(articleQueryModel -> articleQueryModelRepository.create(articleQueryModel, Duration.ofDays(1)));
        log.info("[ArticleReadService.fetch] fetch data. articleId={}, isPresent={}", articleId, articleQueryModelOptional.isPresent());
        return articleQueryModelOptional;
    }

}