package com.samsung.health.hrdatatransfer.domain;

import com.samsung.health.hrdatatransfer.data.MessageRepository;
import com.samsung.health.hrdatatransfer.data.TrackingRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class SendMessageUseCase_Factory implements Factory<SendMessageUseCase> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<TrackingRepository> trackingRepositoryProvider;

  private final Provider<GetCapableNodes> getCapableNodesProvider;

  public SendMessageUseCase_Factory(Provider<MessageRepository> messageRepositoryProvider,
      Provider<TrackingRepository> trackingRepositoryProvider,
      Provider<GetCapableNodes> getCapableNodesProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.trackingRepositoryProvider = trackingRepositoryProvider;
    this.getCapableNodesProvider = getCapableNodesProvider;
  }

  @Override
  public SendMessageUseCase get() {
    return newInstance(messageRepositoryProvider.get(), trackingRepositoryProvider.get(), getCapableNodesProvider.get());
  }

  public static SendMessageUseCase_Factory create(
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<TrackingRepository> trackingRepositoryProvider,
      Provider<GetCapableNodes> getCapableNodesProvider) {
    return new SendMessageUseCase_Factory(messageRepositoryProvider, trackingRepositoryProvider, getCapableNodesProvider);
  }

  public static SendMessageUseCase newInstance(MessageRepository messageRepository,
      TrackingRepository trackingRepository, GetCapableNodes getCapableNodes) {
    return new SendMessageUseCase(messageRepository, trackingRepository, getCapableNodes);
  }
}
