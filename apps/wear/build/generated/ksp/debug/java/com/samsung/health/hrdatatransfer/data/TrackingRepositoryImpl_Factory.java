package com.samsung.health.hrdatatransfer.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import kotlinx.coroutines.CoroutineScope;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class TrackingRepositoryImpl_Factory implements Factory<TrackingRepositoryImpl> {
  private final Provider<CoroutineScope> coroutineScopeProvider;

  private final Provider<HealthTrackingServiceConnection> healthTrackingServiceConnectionProvider;

  private final Provider<Context> contextProvider;

  public TrackingRepositoryImpl_Factory(Provider<CoroutineScope> coroutineScopeProvider,
      Provider<HealthTrackingServiceConnection> healthTrackingServiceConnectionProvider,
      Provider<Context> contextProvider) {
    this.coroutineScopeProvider = coroutineScopeProvider;
    this.healthTrackingServiceConnectionProvider = healthTrackingServiceConnectionProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public TrackingRepositoryImpl get() {
    return newInstance(coroutineScopeProvider.get(), healthTrackingServiceConnectionProvider.get(), contextProvider.get());
  }

  public static TrackingRepositoryImpl_Factory create(
      Provider<CoroutineScope> coroutineScopeProvider,
      Provider<HealthTrackingServiceConnection> healthTrackingServiceConnectionProvider,
      Provider<Context> contextProvider) {
    return new TrackingRepositoryImpl_Factory(coroutineScopeProvider, healthTrackingServiceConnectionProvider, contextProvider);
  }

  public static TrackingRepositoryImpl newInstance(CoroutineScope coroutineScope,
      HealthTrackingServiceConnection healthTrackingServiceConnection, Context context) {
    return new TrackingRepositoryImpl(coroutineScope, healthTrackingServiceConnection, context);
  }
}
