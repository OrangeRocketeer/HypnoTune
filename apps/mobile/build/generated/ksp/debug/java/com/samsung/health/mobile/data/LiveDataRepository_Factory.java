package com.samsung.health.mobile.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class LiveDataRepository_Factory implements Factory<LiveDataRepository> {
  @Override
  public LiveDataRepository get() {
    return newInstance();
  }

  public static LiveDataRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LiveDataRepository newInstance() {
    return new LiveDataRepository();
  }

  private static final class InstanceHolder {
    static final LiveDataRepository_Factory INSTANCE = new LiveDataRepository_Factory();
  }
}
