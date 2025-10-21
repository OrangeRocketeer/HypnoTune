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
public final class StageConfigManager_Factory implements Factory<StageConfigManager> {
  @Override
  public StageConfigManager get() {
    return newInstance();
  }

  public static StageConfigManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static StageConfigManager newInstance() {
    return new StageConfigManager();
  }

  private static final class InstanceHolder {
    static final StageConfigManager_Factory INSTANCE = new StageConfigManager_Factory();
  }
}
