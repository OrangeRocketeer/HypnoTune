package com.samsung.health.hrdatatransfer.presentation;

import com.samsung.health.hrdatatransfer.domain.TrackHeartRateUseCase;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;

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
public final class MainViewModel_MembersInjector implements MembersInjector<MainViewModel> {
  private final Provider<TrackHeartRateUseCase> trackHeartRateUseCaseProvider;

  public MainViewModel_MembersInjector(
      Provider<TrackHeartRateUseCase> trackHeartRateUseCaseProvider) {
    this.trackHeartRateUseCaseProvider = trackHeartRateUseCaseProvider;
  }

  public static MembersInjector<MainViewModel> create(
      Provider<TrackHeartRateUseCase> trackHeartRateUseCaseProvider) {
    return new MainViewModel_MembersInjector(trackHeartRateUseCaseProvider);
  }

  @Override
  public void injectMembers(MainViewModel instance) {
    injectTrackHeartRateUseCase(instance, trackHeartRateUseCaseProvider.get());
  }

  @InjectedFieldSignature("com.samsung.health.hrdatatransfer.presentation.MainViewModel.trackHeartRateUseCase")
  public static void injectTrackHeartRateUseCase(MainViewModel instance,
      TrackHeartRateUseCase trackHeartRateUseCase) {
    instance.trackHeartRateUseCase = trackHeartRateUseCase;
  }
}
