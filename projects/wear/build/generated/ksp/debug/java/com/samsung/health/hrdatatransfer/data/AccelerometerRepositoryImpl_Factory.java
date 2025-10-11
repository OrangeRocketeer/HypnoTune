package com.samsung.health.hrdatatransfer.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class AccelerometerRepositoryImpl_Factory implements Factory<AccelerometerRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public AccelerometerRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AccelerometerRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static AccelerometerRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new AccelerometerRepositoryImpl_Factory(contextProvider);
  }

  public static AccelerometerRepositoryImpl newInstance(Context context) {
    return new AccelerometerRepositoryImpl(context);
  }
}
