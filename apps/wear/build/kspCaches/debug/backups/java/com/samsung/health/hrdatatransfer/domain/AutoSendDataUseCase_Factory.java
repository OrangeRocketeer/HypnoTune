package com.samsung.health.hrdatatransfer.domain;

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
public final class AutoSendDataUseCase_Factory implements Factory<AutoSendDataUseCase> {
  private final Provider<TrackingRepository> trackingRepositoryProvider;

  private final Provider<SendMessageUseCase> sendMessageUseCaseProvider;

  public AutoSendDataUseCase_Factory(Provider<TrackingRepository> trackingRepositoryProvider,
      Provider<SendMessageUseCase> sendMessageUseCaseProvider) {
    this.trackingRepositoryProvider = trackingRepositoryProvider;
    this.sendMessageUseCaseProvider = sendMessageUseCaseProvider;
  }

  @Override
  public AutoSendDataUseCase get() {
    return newInstance(trackingRepositoryProvider.get(), sendMessageUseCaseProvider.get());
  }

  public static AutoSendDataUseCase_Factory create(
      Provider<TrackingRepository> trackingRepositoryProvider,
      Provider<SendMessageUseCase> sendMessageUseCaseProvider) {
    return new AutoSendDataUseCase_Factory(trackingRepositoryProvider, sendMessageUseCaseProvider);
  }

  public static AutoSendDataUseCase newInstance(TrackingRepository trackingRepository,
      SendMessageUseCase sendMessageUseCase) {
    return new AutoSendDataUseCase(trackingRepository, sendMessageUseCase);
  }
}
