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
public final class TrackHeartRateUseCase_Factory implements Factory<TrackHeartRateUseCase> {
  private final Provider<TrackingRepository> trackingRepositoryProvider;

  public TrackHeartRateUseCase_Factory(Provider<TrackingRepository> trackingRepositoryProvider) {
    this.trackingRepositoryProvider = trackingRepositoryProvider;
  }

  @Override
  public TrackHeartRateUseCase get() {
    return newInstance(trackingRepositoryProvider.get());
  }

  public static TrackHeartRateUseCase_Factory create(
      Provider<TrackingRepository> trackingRepositoryProvider) {
    return new TrackHeartRateUseCase_Factory(trackingRepositoryProvider);
  }

  public static TrackHeartRateUseCase newInstance(TrackingRepository trackingRepository) {
    return new TrackHeartRateUseCase(trackingRepository);
  }
}
