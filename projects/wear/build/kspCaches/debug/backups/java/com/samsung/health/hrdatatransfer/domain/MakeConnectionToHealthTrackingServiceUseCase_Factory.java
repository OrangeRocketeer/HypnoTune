package com.samsung.health.hrdatatransfer.domain;

import com.samsung.health.hrdatatransfer.data.HealthTrackingServiceConnection;
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
public final class MakeConnectionToHealthTrackingServiceUseCase_Factory implements Factory<MakeConnectionToHealthTrackingServiceUseCase> {
  private final Provider<HealthTrackingServiceConnection> healthTrackingServiceConnectionProvider;

  public MakeConnectionToHealthTrackingServiceUseCase_Factory(
      Provider<HealthTrackingServiceConnection> healthTrackingServiceConnectionProvider) {
    this.healthTrackingServiceConnectionProvider = healthTrackingServiceConnectionProvider;
  }

  @Override
  public MakeConnectionToHealthTrackingServiceUseCase get() {
    return newInstance(healthTrackingServiceConnectionProvider.get());
  }

  public static MakeConnectionToHealthTrackingServiceUseCase_Factory create(
      Provider<HealthTrackingServiceConnection> healthTrackingServiceConnectionProvider) {
    return new MakeConnectionToHealthTrackingServiceUseCase_Factory(healthTrackingServiceConnectionProvider);
  }

  public static MakeConnectionToHealthTrackingServiceUseCase newInstance(
      HealthTrackingServiceConnection healthTrackingServiceConnection) {
    return new MakeConnectionToHealthTrackingServiceUseCase(healthTrackingServiceConnection);
  }
}
