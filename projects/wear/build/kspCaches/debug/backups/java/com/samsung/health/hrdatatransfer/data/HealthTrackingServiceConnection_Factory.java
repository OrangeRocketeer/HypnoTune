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
public final class HealthTrackingServiceConnection_Factory implements Factory<HealthTrackingServiceConnection> {
  private final Provider<Context> contextProvider;

  private final Provider<CoroutineScope> coroutineScopeProvider;

  public HealthTrackingServiceConnection_Factory(Provider<Context> contextProvider,
      Provider<CoroutineScope> coroutineScopeProvider) {
    this.contextProvider = contextProvider;
    this.coroutineScopeProvider = coroutineScopeProvider;
  }

  @Override
  public HealthTrackingServiceConnection get() {
    return newInstance(contextProvider.get(), coroutineScopeProvider.get());
  }

  public static HealthTrackingServiceConnection_Factory create(Provider<Context> contextProvider,
      Provider<CoroutineScope> coroutineScopeProvider) {
    return new HealthTrackingServiceConnection_Factory(contextProvider, coroutineScopeProvider);
  }

  public static HealthTrackingServiceConnection newInstance(Context context,
      CoroutineScope coroutineScope) {
    return new HealthTrackingServiceConnection(context, coroutineScope);
  }
}
