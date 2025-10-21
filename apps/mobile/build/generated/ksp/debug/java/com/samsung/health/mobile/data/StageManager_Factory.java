package com.samsung.health.mobile.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class StageManager_Factory implements Factory<StageManager> {
  private final Provider<StageConfigManager> stageConfigManagerProvider;

  public StageManager_Factory(Provider<StageConfigManager> stageConfigManagerProvider) {
    this.stageConfigManagerProvider = stageConfigManagerProvider;
  }

  @Override
  public StageManager get() {
    return newInstance(stageConfigManagerProvider.get());
  }

  public static StageManager_Factory create(
      Provider<StageConfigManager> stageConfigManagerProvider) {
    return new StageManager_Factory(stageConfigManagerProvider);
  }

  public static StageManager newInstance(StageConfigManager stageConfigManager) {
    return new StageManager(stageConfigManager);
  }
}
