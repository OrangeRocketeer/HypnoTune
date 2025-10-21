package com.samsung.health.hrdatatransfer.presentation;

import com.samsung.health.hrdatatransfer.data.AccelerometerRepository;
import com.samsung.health.hrdatatransfer.domain.AreTrackingCapabilitiesAvailableUseCase;
import com.samsung.health.hrdatatransfer.domain.AutoSendDataUseCase;
import com.samsung.health.hrdatatransfer.domain.MakeConnectionToHealthTrackingServiceUseCase;
import com.samsung.health.hrdatatransfer.domain.SendMessageUseCase;
import com.samsung.health.hrdatatransfer.domain.StopTrackingUseCase;
import com.samsung.health.hrdatatransfer.domain.TrackHeartRateUseCase;
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
public final class MainViewModel_Factory implements Factory<MainViewModel> {
  private final Provider<MakeConnectionToHealthTrackingServiceUseCase> makeConnectionToHealthTrackingServiceUseCaseProvider;

  private final Provider<SendMessageUseCase> sendMessageUseCaseProvider;

  private final Provider<StopTrackingUseCase> stopTrackingUseCaseProvider;

  private final Provider<AreTrackingCapabilitiesAvailableUseCase> areTrackingCapabilitiesAvailableUseCaseProvider;

  private final Provider<AccelerometerRepository> accelerometerRepositoryProvider;

  private final Provider<AutoSendDataUseCase> autoSendDataUseCaseProvider;

  private final Provider<TrackHeartRateUseCase> trackHeartRateUseCaseProvider;

  public MainViewModel_Factory(
      Provider<MakeConnectionToHealthTrackingServiceUseCase> makeConnectionToHealthTrackingServiceUseCaseProvider,
      Provider<SendMessageUseCase> sendMessageUseCaseProvider,
      Provider<StopTrackingUseCase> stopTrackingUseCaseProvider,
      Provider<AreTrackingCapabilitiesAvailableUseCase> areTrackingCapabilitiesAvailableUseCaseProvider,
      Provider<AccelerometerRepository> accelerometerRepositoryProvider,
      Provider<AutoSendDataUseCase> autoSendDataUseCaseProvider,
      Provider<TrackHeartRateUseCase> trackHeartRateUseCaseProvider) {
    this.makeConnectionToHealthTrackingServiceUseCaseProvider = makeConnectionToHealthTrackingServiceUseCaseProvider;
    this.sendMessageUseCaseProvider = sendMessageUseCaseProvider;
    this.stopTrackingUseCaseProvider = stopTrackingUseCaseProvider;
    this.areTrackingCapabilitiesAvailableUseCaseProvider = areTrackingCapabilitiesAvailableUseCaseProvider;
    this.accelerometerRepositoryProvider = accelerometerRepositoryProvider;
    this.autoSendDataUseCaseProvider = autoSendDataUseCaseProvider;
    this.trackHeartRateUseCaseProvider = trackHeartRateUseCaseProvider;
  }

  @Override
  public MainViewModel get() {
    MainViewModel instance = newInstance(makeConnectionToHealthTrackingServiceUseCaseProvider.get(), sendMessageUseCaseProvider.get(), stopTrackingUseCaseProvider.get(), areTrackingCapabilitiesAvailableUseCaseProvider.get(), accelerometerRepositoryProvider.get(), autoSendDataUseCaseProvider.get());
    MainViewModel_MembersInjector.injectTrackHeartRateUseCase(instance, trackHeartRateUseCaseProvider.get());
    return instance;
  }

  public static MainViewModel_Factory create(
      Provider<MakeConnectionToHealthTrackingServiceUseCase> makeConnectionToHealthTrackingServiceUseCaseProvider,
      Provider<SendMessageUseCase> sendMessageUseCaseProvider,
      Provider<StopTrackingUseCase> stopTrackingUseCaseProvider,
      Provider<AreTrackingCapabilitiesAvailableUseCase> areTrackingCapabilitiesAvailableUseCaseProvider,
      Provider<AccelerometerRepository> accelerometerRepositoryProvider,
      Provider<AutoSendDataUseCase> autoSendDataUseCaseProvider,
      Provider<TrackHeartRateUseCase> trackHeartRateUseCaseProvider) {
    return new MainViewModel_Factory(makeConnectionToHealthTrackingServiceUseCaseProvider, sendMessageUseCaseProvider, stopTrackingUseCaseProvider, areTrackingCapabilitiesAvailableUseCaseProvider, accelerometerRepositoryProvider, autoSendDataUseCaseProvider, trackHeartRateUseCaseProvider);
  }

  public static MainViewModel newInstance(
      MakeConnectionToHealthTrackingServiceUseCase makeConnectionToHealthTrackingServiceUseCase,
      SendMessageUseCase sendMessageUseCase, StopTrackingUseCase stopTrackingUseCase,
      AreTrackingCapabilitiesAvailableUseCase areTrackingCapabilitiesAvailableUseCase,
      AccelerometerRepository accelerometerRepository, AutoSendDataUseCase autoSendDataUseCase) {
    return new MainViewModel(makeConnectionToHealthTrackingServiceUseCase, sendMessageUseCase, stopTrackingUseCase, areTrackingCapabilitiesAvailableUseCase, accelerometerRepository, autoSendDataUseCase);
  }
}
