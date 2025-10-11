package com.samsung.health.hrdatatransfer;

import android.content.Context;
import com.samsung.health.hrdatatransfer.data.HealthTrackingServiceConnection;
import com.samsung.health.hrdatatransfer.data.TrackingRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class MainModule_ProvideTrackingRepositoryFactory implements Factory<TrackingRepository> {
  private final MainModule module;

  private final Provider<CoroutineScope> coroutineScopeProvider;

  private final Provider<HealthTrackingServiceConnection> healthTrackingServiceConnectionProvider;

  private final Provider<Context> contextProvider;

  public MainModule_ProvideTrackingRepositoryFactory(MainModule module,
      Provider<CoroutineScope> coroutineScopeProvider,
      Provider<HealthTrackingServiceConnection> healthTrackingServiceConnectionProvider,
      Provider<Context> contextProvider) {
    this.module = module;
    this.coroutineScopeProvider = coroutineScopeProvider;
    this.healthTrackingServiceConnectionProvider = healthTrackingServiceConnectionProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public TrackingRepository get() {
    return provideTrackingRepository(module, coroutineScopeProvider.get(), healthTrackingServiceConnectionProvider.get(), contextProvider.get());
  }

  public static MainModule_ProvideTrackingRepositoryFactory create(MainModule module,
      Provider<CoroutineScope> coroutineScopeProvider,
      Provider<HealthTrackingServiceConnection> healthTrackingServiceConnectionProvider,
      Provider<Context> contextProvider) {
    return new MainModule_ProvideTrackingRepositoryFactory(module, coroutineScopeProvider, healthTrackingServiceConnectionProvider, contextProvider);
  }

  public static TrackingRepository provideTrackingRepository(MainModule instance,
      CoroutineScope coroutineScope,
      HealthTrackingServiceConnection healthTrackingServiceConnection, Context context) {
    return Preconditions.checkNotNullFromProvides(instance.provideTrackingRepository(coroutineScope, healthTrackingServiceConnection, context));
  }
}
