package edu.handong.csee.histudy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BannerImageCleanupListener {

  private final BannerImageStorage bannerImageStorage;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  void handleAfterCommit(BannerImageDeleteAfterCommitEvent event) {
    bannerImageStorage.deleteQuietly(event.imagePath());
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
  void handleAfterRollback(BannerImageDeleteAfterRollbackEvent event) {
    bannerImageStorage.deleteQuietly(event.imagePath());
  }
}
